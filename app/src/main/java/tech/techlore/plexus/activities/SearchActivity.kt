/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.activities

import android.os.Bundle
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.bottomsheets.search.SearchSortBottomSheet
import tech.techlore.plexus.databinding.ActivitySearchBinding
import tech.techlore.plexus.interfaces.main.FavToggleListener
import tech.techlore.plexus.interfaces.main.SortPrefsChangeListener
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.GRID_VIEW
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.getViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar
import kotlin.time.Duration.Companion.milliseconds

class SearchActivity
    : AppCompatActivity(),
    MainDataItemAdapter.OnItemClickListener,
    SortPrefsChangeListener,
    FavToggleListener {
    
    private lateinit var activityBinding: ActivitySearchBinding
    var ascDescChipId = R.id.sortAZ
    private val sixteenDpToPx by lazy {
        convertDpToPx(this, 16f)
    }
    private var isKeyboardVisible = true
    private var searchQueryString = ""
    private val mainRepository by inject<MainDataRepository>()
    private lateinit var searchItemAdapter: MainDataItemAdapter
    private var pagingJob: Job? = null
    private var shouldScrollToTop = false
    private var isViewStubInflated = false
    private var isGridView = false
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.apply {
            setNavBarContrastEnforced()
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        var clearResultsJob: Job? = null
        isGridView = get<PreferenceManager>().getBoolean(GRID_VIEW, false)
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.searchDockedToolbar) { v, windowInsets ->
            // Views will be properly adjusted for edge to edge even without this code,
            // but this is done to get keyboard visibility status.
            // Root view could also theoretically be used instead of docked toolbar,
            // but when I tried that, there were UI imperfections.
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout()
                                                        or WindowInsetsCompat.Type.ime())
            v.updatePadding(
                left = insets.left + sixteenDpToPx,
                right = insets.right + sixteenDpToPx,
                bottom = insets.bottom
            )
            
            isKeyboardVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
            
            WindowInsetsCompat.CONSUMED
        }
        activityBinding.searchRecyclerViewRoot.recyclerView.adjustEdgeToEdge(this)
        
        activityBinding.searchRecyclerViewRoot.swipeRefreshLayout.isEnabled = false
        
        searchItemAdapter =
            MainDataItemAdapter(
                clickListener = this,
                favToggleListener = this
            ).apply {
                isGridViewLayout = isGridView
            }
        
        activityBinding.searchRecyclerViewRoot.recyclerView.apply {
            activityBinding.searchAppBar.liftOnScrollTargetViewId = this.id
            layoutManager = getViewStyle(this@SearchActivity, isGridView)
            adapter = searchItemAdapter
            FastScrollerBuilder(this).build() // Fast scroll
        }
        
        // Perform search
        activityBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            
            override fun onQueryTextSubmit(searchString: String): Boolean {
                if (searchString.isNotEmpty()) {
                    searchQueryString = searchString
                    activityBinding.searchRecyclerViewRoot.recyclerView.smoothScrollToPosition(0) // Scroll to top
                    loadPagedData()
                }
                return true
            }
            
            override fun onQueryTextChange(searchString: String): Boolean {
                clearResultsJob?.cancel()
                
                // Clear with a subtle delay
                clearResultsJob = lifecycleScope.launch {
                    delay(350.milliseconds)
                    if (searchString.isEmpty()) {
                        pagingJob?.cancel()
                        searchItemAdapter.submitData(PagingData.empty())
                        hideEmptyListView()
                    }
                }
                
                return true
            }
        })
        
        // Back
        activityBinding.searchBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Sort
        activityBinding.searchSortBtn.setOnClickListener {
            SearchSortBottomSheet(sortPrefsListener = this)
                .show(supportFragmentManager, "SearchSortBottomSheet")
        }
        
    }
    
    private fun keepKeyboardHidden() {
        // This will prevent the following scenario:
        // keyboard hidden by user > user taps on an item > comes back > keyboard visible
        if (!isKeyboardVisible && activityBinding.searchView.hasFocus())
            activityBinding.searchView.clearFocus()
    }
    
    private fun showEmptyListView() {
        if (!isViewStubInflated) {
            activityBinding.searchRecyclerViewRoot.emptyListViewStub.inflate()
            isViewStubInflated = true
        }
        else {
            activityBinding.searchRecyclerViewRoot.emptyListViewStub.isVisible = true
        }
    }
    
    private fun hideEmptyListView() {
        if (isViewStubInflated) {
            activityBinding.searchRecyclerViewRoot.emptyListViewStub.isVisible = false
            // Don't do isViewStubInflated = false
            // ViewStub is only inflated once & then reference changes to the actual view
            // If it is inflated again, app will crash with the following:
            // "ViewStub must have a non-null ViewGroup viewParent"
        }
    }
    
    private fun loadPagedData() {
        pagingJob?.cancel()
        pagingJob =
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        searchItemAdapter.onPagesUpdatedFlow.collect {
                            if (searchItemAdapter.itemCount == 0) showEmptyListView()
                            else hideEmptyListView()
                            
                            // Scroll to top only when sort prefs changed,
                            // not when items inserted/deleted/updated
                            if (shouldScrollToTop) {
                                activityBinding.searchRecyclerViewRoot.recyclerView.apply {
                                    // Use scrollToPosition(0)
                                    // instead of smoothScrollToPosition(0)
                                    // or else appbar & recycler view will start having issues
                                    scrollToPosition(0)
                                    post {
                                        activityBinding.searchAppBar.isLifted = false
                                    }
                                }
                                shouldScrollToTop = false
                            }
                        }
                    }
                    
                    launch {
                        mainRepository
                            .searchInDb(searchQueryString, ascDescChipId)
                            .collectLatest { pagingData ->
                                searchItemAdapter.submitData(pagingData)
                            }
                    }
                }
            }
    }
    
    override fun onItemClick(position: Int) {
        searchItemAdapter.peek(position)?.let {
            startDetailsActivity(it.packageName)
        }
    }
    
    override fun onFavToggled(name: String, packageName: String, isChecked: Boolean) {
        lifecycleScope.launch {
            mainRepository.updateFav(packageName, isChecked)
            showSnackbar(
                coordinatorLayout = activityBinding.searchCoordLayout,
                message =
                    if (isChecked) getString(R.string.added_to_fav, name)
                    else getString(R.string.removed_from_fav, name),
                anchorView = activityBinding.searchDockedToolbar
            )
        }
    }
    
    override fun onSortPrefsChanged() {
        activityBinding.searchAppBar.setExpanded(true, true)
        keepKeyboardHidden()
        
        // For Search, only asc/desc sort is available,
        // hence there would be nothing to sort, if list is already empty.
        if (searchItemAdapter.itemCount != 0) {
            shouldScrollToTop = true
            loadPagedData()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (DataState.isSingleAppUpdated) {
            searchItemAdapter.refresh()
            DataState.isSingleAppUpdated = false
        }
    }
    
    override fun onPause() {
        super.onPause()
        keepKeyboardHidden()
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isKeyboardVisible) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                currentFocus?.let {
                    imm?.hideSoftInputFromWindow(it.windowToken, 0)
                }
            }
            
            finishAfterTransition()
        }
    }
}