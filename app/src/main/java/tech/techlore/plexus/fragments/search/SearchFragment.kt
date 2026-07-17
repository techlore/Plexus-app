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

package tech.techlore.plexus.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SearchActivity
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.interfaces.main.FavToggleListener
import tech.techlore.plexus.interfaces.main.SortPrefsChangeListener
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.GRID_VIEW
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge
import tech.techlore.plexus.utils.UiUtils.Companion.getViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar
import kotlin.getValue
import kotlin.time.Duration.Companion.milliseconds

class SearchFragment :
    Fragment(),
    MainDataItemAdapter.OnItemClickListener,
    SortPrefsChangeListener,
    FavToggleListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var searchActivity: SearchActivity
    private var searchQueryString = ""
    private val mainRepository by inject<MainDataRepository>()
    private lateinit var searchItemAdapter: MainDataItemAdapter
    private var pagingJob: Job? = null
    private var isViewStubInflated = false
    private var isGridView = false
    private var clickedItemPos = -1
    private var clickedItemPackageName = ""
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        searchActivity = requireActivity() as SearchActivity
        var clearResultsJob: Job? = null
        isGridView = get<PreferenceManager>().getBoolean(GRID_VIEW, false)
        
        // Adjust recycler view for edge to edge
        fragmentBinding.recyclerView.adjustEdgeToEdge(requireContext())
        
        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.isEnabled = false
        
        searchItemAdapter =
            MainDataItemAdapter(
                clickListener = this@SearchFragment,
                favToggleListener = this@SearchFragment
            ).apply {
                isGridViewLayout = isGridView
            }
        
        fragmentBinding.recyclerView.apply {
            searchActivity.activityBinding.searchAppBar.liftOnScrollTargetViewId = this.id
            layoutManager = getViewStyle(requireContext(), isGridView)
            adapter = searchItemAdapter
            FastScrollerBuilder(this).build() // Fast scroll
        }
        
        // Perform search
        searchActivity.activityBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            
            override fun onQueryTextSubmit(searchString: String): Boolean {
                if (searchString.isNotEmpty()) {
                    searchQueryString = searchString
                    loadPagedData()
                    fragmentBinding.recyclerView.smoothScrollToPosition(0) // Scroll to top
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
        
    }
    
    private fun showEmptyListView() {
        if (!isViewStubInflated) {
            fragmentBinding.emptyListViewStub.inflate()
            isViewStubInflated = true
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
    
    private fun loadPagedData() {
        pagingJob?.cancel()
        pagingJob =
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        mainRepository
                            .searchInDb(searchQueryString, searchActivity.ascDescChipId)
                            .collectLatest { pagingData ->
                                searchItemAdapter.submitData(pagingData)
                            }
                    }
                    
                    launch {
                        searchItemAdapter.loadStateFlow.collect { loadStates ->
                            if (loadStates.refresh is LoadState.NotLoading) {
                                if (searchItemAdapter.itemCount == 0) showEmptyListView()
                                else hideEmptyListView()
                            }
                        }
                    }
                }
            }
    }
    
    override fun onItemClick(position: Int) {
        clickedItemPos = position
        searchItemAdapter.peek(position)?.let {
            searchActivity.startDetailsActivity(it.packageName)
        }
    }
    
    override fun onSortPrefsChanged() {
        // For Search, only asc/desc sort is available,
        // hence there would be nothing to sort, if list is already empty.
        if (searchItemAdapter.itemCount != 0) {
            loadPagedData()
            fragmentBinding.recyclerView.smoothScrollToPosition(0) // Scroll to top
        }
    }
    
    override fun onFavToggled(name: String, packageName: String, isChecked: Boolean) {
        lifecycleScope.launch {
            mainRepository.updateFav(packageName, isChecked)
            showSnackbar(
                coordinatorLayout = searchActivity.activityBinding.searchCoordLayout,
                message =
                    if (isChecked) getString(R.string.added_to_fav, name)
                    else getString(R.string.removed_from_fav, name),
                anchorView = searchActivity.activityBinding.searchDockedToolbar
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (DataState.isSingleAppUpdated) {
            searchItemAdapter.refresh()
            clickedItemPos = -1
            clickedItemPackageName = ""
            DataState.isSingleAppUpdated = false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}