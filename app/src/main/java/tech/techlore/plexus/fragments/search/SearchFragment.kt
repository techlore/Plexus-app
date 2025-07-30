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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SearchActivity
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.interfaces.FavToggleListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.GRID_VIEW
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar
import kotlin.getValue

class SearchFragment :
    Fragment(),
    MainDataItemAdapter.OnItemClickListener,
    FavToggleListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var searchActivity: SearchActivity
    private val miniRepository by inject<MainDataMinimalRepository>()
    private lateinit var searchItemAdapter: MainDataItemAdapter
    private lateinit var searchDataList: ArrayList<MainDataMinimal>
    private var isGridView = false
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        searchActivity = requireActivity() as SearchActivity
        searchDataList = ArrayList()
        var job: Job? = null
        isGridView = get<PreferenceManager>().getBoolean(GRID_VIEW, false)
        
        // Adjust recycler view for edge to edge
        fragmentBinding.recyclerView.adjustEdgeToEdge(requireContext())
        
        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.isEnabled = false
        
        lifecycleScope.launch {
            searchItemAdapter =
                MainDataItemAdapter(clickListener = this@SearchFragment,
                                    favToggleListener = this@SearchFragment,
                                    isGridView = isGridView)
            FastScrollerBuilder(fragmentBinding.recyclerView).build() // Fast scroll
            performSearch(searchActivity.activityBinding.searchView.query.toString())
        }
        
        // Perform search
        searchActivity.activityBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            
            override fun onQueryTextSubmit(searchString: String): Boolean {
                return true
            }
            
            override fun onQueryTextChange(searchString: String): Boolean {
                job?.cancel()
                
                // Search with a subtle delay
                job = lifecycleScope.launch {
                    delay(350)
                    performSearch(searchString)
                }
                
                return true
            }
        })
        
    }
    
    private suspend fun performSearch(searchString: String) {
        if (searchString.isNotEmpty()) {
            searchDataList = miniRepository.searchInDb(searchString, searchActivity.orderChipId)
            if (searchDataList.isEmpty()) {
                fragmentBinding.recyclerView.adapter = null
                searchActivity.activityBinding.emptySearchView.isVisible = true
            }
            else {
                searchActivity.activityBinding.emptySearchView.isVisible = false
                fragmentBinding.recyclerView.apply {
                    searchActivity.activityBinding.searchAppBar.liftOnScrollTargetViewId = this.id
                    layoutManager =
                        if (!isGridView)
                            LinearLayoutManager(requireContext())
                        else
                            GridLayoutManager(requireContext(), 2)
                    adapter = searchItemAdapter
                }
                searchItemAdapter.submitList(searchDataList)
                
            }
        }
        else {
            fragmentBinding.recyclerView.adapter = null
            searchActivity.activityBinding.emptySearchView.isVisible = false
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val searchData = searchDataList[position]
        searchActivity.startDetailsActivity(searchData.packageName)
    }
    
    override fun onFavToggled(item: MainDataMinimal, isChecked: Boolean) {
        item.isFav = isChecked
        lifecycleScope.launch {
            get<MainDataMinimalRepository>().updateFav(item)
        }
        showSnackbar(
            coordinatorLayout = searchActivity.activityBinding.searchCoordLayout,
            message =
                if (isChecked) getString(R.string.added_to_fav, item.name)
                else getString(R.string.removed_from_fav, item.name),
            anchorView = searchActivity.activityBinding.searchDockedToolbar
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}