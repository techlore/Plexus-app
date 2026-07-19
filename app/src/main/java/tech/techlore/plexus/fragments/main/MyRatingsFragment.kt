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

package tech.techlore.plexus.fragments.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.activities.MyRatingsDetailsActivity
import tech.techlore.plexus.adapters.main.MyRatingsItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.interfaces.main.SortPrefsChangeListener
import tech.techlore.plexus.interfaces.main.ViewTypeChangeListener
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.getViewStyle
import kotlin.getValue

class MyRatingsFragment :
    Fragment(),
    MyRatingsItemAdapter.OnItemClickListener,
    SortPrefsChangeListener,
    ViewTypeChangeListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private val prefManager by inject<PreferenceManager>()
    private val myRatingsRepository by inject<MyRatingsRepository>()
    private lateinit var myRatingsItemAdapter: MyRatingsItemAdapter
    private var ascDescChipId = 0
    private var pagingJob: Job? = null
    private var shouldScrollToTop = false
    private var isViewStubInflated = false
    private var clickedItemPackageName = ""
    private var isMyRatingCountChanged = false
    private var myRatingsNewCount = -1
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        mainActivity = requireActivity() as MainActivity
        setSortPrefs()
        
        // Adjust UI components for edge to edge
        fragmentBinding.recyclerView.adjustEdgeToEdge(requireContext())
        ViewCompat.setOnApplyWindowInsetsListener(mainActivity.activityBinding.newRatingFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left + convertDpToPx(requireContext(), 16f)
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 80f)
                // 80 = 64 + 16
                // Docked toolbar height = 64dp
                // Space between FAB & docked toolbar = 16dp
            }
            
            WindowInsetsCompat.CONSUMED
        }
        
        myRatingsItemAdapter =
            MyRatingsItemAdapter(clickListener = this)
                .apply { isGridViewLayout = mainActivity.isGridView }
        
        fragmentBinding.recyclerView.apply {
            mainActivity.activityBinding.mainAppBar.liftOnScrollTargetViewId = this.id
            layoutManager = getViewStyle(requireContext(), mainActivity.isGridView)
            adapter = myRatingsItemAdapter
            FastScrollerBuilder(this).build()
        }
        
        // New rating FAB
        mainActivity.activityBinding.newRatingFab.apply {
            show()
            setOnClickListener {
                mainActivity.onNavViewItemSelected(R.id.nav_installed_apps, false)
            }
        }
        
        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.isEnabled = false
        
        loadPagedData()
        
    }
    
    private val startResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let {
                    isMyRatingCountChanged = it.getBooleanExtra("isMyRatingCountChanged", false)
                    myRatingsNewCount = it.getIntExtra("myRatingsNewCount", -1)
                    
                }
            }
        }
    
    private fun showEmptyListView() {
        if (!isViewStubInflated) {
            fragmentBinding.emptyListViewStub.inflate()
            isViewStubInflated = true
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_my_ratings)
            fragmentBinding.root.findViewById<MaterialTextView>(R.id.emptyListViewText).apply {
                setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                text = getString(R.string.submit_first_rating)
            }
        }
        else {
            fragmentBinding.emptyListViewStub.isVisible = true
        }
    }
    
    private fun hideEmptyListView() {
        if (isViewStubInflated) {
            fragmentBinding.emptyListViewStub.isVisible = false
            // Don't do isViewStubInflated = false
            // ViewStub is only inflated once & then reference changes to the actual view
            // If it is inflated again, app will crash with the following:
            // "ViewStub must have a non-null ViewGroup viewParent"
        }
    }
    
    private fun setSortPrefs() {
        ascDescChipId = prefManager.getInt(A_Z_SORT)
    }
    
    private fun loadPagedData() {
        pagingJob?.cancel()
        pagingJob =
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        myRatingsItemAdapter.onPagesUpdatedFlow.collect {
                            if (myRatingsItemAdapter.itemCount == 0) showEmptyListView()
                            else {
                                hideEmptyListView()
                                
                                // Scroll to top only when sort prefs changed,
                                // not when items inserted/deleted/updated
                                if (shouldScrollToTop) {
                                    fragmentBinding.recyclerView.apply {
                                        // Use scrollToPosition(0)
                                        // instead of smoothScrollToPosition(0)
                                        // or else appbar & recycler view will start having issues
                                        scrollToPosition(0)
                                        post {
                                            mainActivity.activityBinding.mainAppBar.isLifted = false
                                        }
                                    }
                                    shouldScrollToTop = false
                                }
                            }
                        }
                    }
                    
                    launch {
                        myRatingsRepository
                            .getSortedMyRatingsByName(orderPref = ascDescChipId)
                            .collectLatest { pagingData ->
                                myRatingsItemAdapter.submitData(pagingData)
                            }
                    }
                }
            }
    }
    
    override fun onItemClick(position: Int) {
        myRatingsItemAdapter.peek(position)?.let {
            clickedItemPackageName = it.packageName
            startResultLauncher.launch(
                Intent(requireContext(), MyRatingsDetailsActivity::class.java)
                    .putExtra("packageName", it.packageName),
                ActivityOptionsCompat.makeSceneTransitionAnimation(mainActivity)
            )
        }
    }
    
    override fun onViewTypeChanged() {
        myRatingsItemAdapter.isGridViewLayout = mainActivity.isGridView
        fragmentBinding.recyclerView.apply {
            TransitionManager.beginDelayedTransition(this, Fade())
            layoutManager = getViewStyle(requireContext(), mainActivity.isGridView)
            myRatingsItemAdapter.notifyItemRangeChanged(0, myRatingsItemAdapter.itemCount)
            smoothScrollToPosition(0) // Scroll to top
        }
    }
    
    override fun onSortPrefsChanged() {
        // For MyRatings, only asc/desc sort is available,
        // hence there would be nothing to sort, if list is already empty.
        // This is NOT the case for other fragments
        if (myRatingsItemAdapter.itemCount != 0) {
            setSortPrefs()
            loadPagedData()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (isMyRatingCountChanged) {
            lifecycleScope.launch {
                if (myRatingsNewCount == 0)
                    myRatingsRepository.deleteSingleMyRating(clickedItemPackageName)
                
                get<MainDataRepository>().updateSingleApp(clickedItemPackageName)
                
                isMyRatingCountChanged = false
                myRatingsNewCount = -1
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mainActivity.activityBinding.newRatingFab.apply {
            if (isShown) hide()
        }
    }
}