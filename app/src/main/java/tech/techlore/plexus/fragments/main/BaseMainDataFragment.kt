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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.transition.Fade
import androidx.transition.TransitionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.interfaces.main.FavToggleListener
import tech.techlore.plexus.interfaces.main.SortPrefsChangeListener
import tech.techlore.plexus.interfaces.main.ViewTypeChangeListener
import tech.techlore.plexus.models.mini.MainDataMini
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.INSTALLED_FROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge
import tech.techlore.plexus.utils.UiUtils.Companion.getViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar
import kotlin.getValue

abstract class BaseMainDataFragment
    : Fragment(),
    MainDataItemAdapter.OnItemClickListener,
    SortPrefsChangeListener,
    ViewTypeChangeListener,
    FavToggleListener {
    
    private var _binding: RecyclerViewBinding? = null
    protected val fragmentBinding get() = _binding!!
    protected lateinit var mainActivity: MainActivity
    protected lateinit var mainDataItemAdapter: MainDataItemAdapter
    protected val prefManager by inject<PreferenceManager>()
    protected val mainRepository by inject<MainDataRepository>()
    protected var ascDescChipId = 0
    protected var installedFromChipId = 0
    protected var statusToggleBtnId = 0
    private var pagingJob: Job? = null
    private var isViewStubInflated = false
    
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
        
        // Adjust recycler view for edge to edge
        fragmentBinding.recyclerView.adjustEdgeToEdge(requireContext())
        
        mainDataItemAdapter =
            MainDataItemAdapter(
                clickListener = this@BaseMainDataFragment,
                favToggleListener = this@BaseMainDataFragment
            ).apply {
                isGridViewLayout = mainActivity.isGridView
            }
        
        fragmentBinding.recyclerView.apply {
            mainActivity.activityBinding.mainAppBar.liftOnScrollTargetViewId = this.id
            layoutManager = getViewStyle(requireContext(), mainActivity.isGridView)
            adapter = mainDataItemAdapter
            FastScrollerBuilder(this).build()
        }
        
        // Swipe refresh layout
        if (isSwipeRefreshEnabled())
            fragmentBinding.swipeRefreshLayout.setOnRefreshListener { onSwipeRefresh() }
        else
            fragmentBinding.swipeRefreshLayout.isEnabled = false
        
        loadPagedData()
    }
    
    protected abstract fun getDataFromDB(): Flow<PagingData<MainDataMini>>
    
    protected open fun isSwipeRefreshEnabled(): Boolean = true
    
    protected open fun onSwipeRefresh() {}
    
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
    
    private fun setSortPrefs() {
        ascDescChipId = prefManager.getInt(A_Z_SORT)
        installedFromChipId = prefManager.getInt(INSTALLED_FROM_SORT)
        statusToggleBtnId = prefManager.getInt(STATUS_TOGGLE)
    }
    
    private fun loadPagedData() {
        pagingJob?.cancel()
        pagingJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    getDataFromDB().collectLatest { pagingData ->
                        mainDataItemAdapter.submitData(pagingData)
                    }
                }
                
                launch {
                    mainDataItemAdapter.loadStateFlow.collect { loadStates ->
                        if (loadStates.refresh is LoadState.NotLoading) {
                            if (mainDataItemAdapter.itemCount == 0) showEmptyListView()
                            else hideEmptyListView()
                        }
                    }
                }
            }
        }
    }
    
    override fun onItemClick(position: Int) {
        mainDataItemAdapter.peek(position)?.let {
            mainActivity.startDetailsActivity(it.packageName)
        }
    }
    
    override fun onViewTypeChanged() {
        mainDataItemAdapter.isGridViewLayout = mainActivity.isGridView
        fragmentBinding.recyclerView.apply {
            TransitionManager.beginDelayedTransition(this, Fade())
            layoutManager = getViewStyle(requireContext(), mainActivity.isGridView)
            mainDataItemAdapter.notifyItemRangeChanged(0, mainDataItemAdapter.itemCount)
            smoothScrollToPosition(0) // Scroll to top
        }
    }
    
    override fun onSortPrefsChanged() {
        setSortPrefs()
        loadPagedData()
        fragmentBinding.recyclerView.smoothScrollToPosition(0) // Scroll to top
    }
    
    override fun onFavToggled(name: String, packageName: String, isChecked: Boolean) {
        lifecycleScope.launch {
            mainRepository.updateFav(packageName, isChecked)
            showSnackbar(
                coordinatorLayout = mainActivity.activityBinding.mainCoordLayout,
                message =
                    if (isChecked) getString(R.string.added_to_fav, name)
                    else getString(R.string.removed_from_fav, name),
                anchorView = mainActivity.activityBinding.mainDockedToolbar
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (DataState.isSingleAppUpdated) {
            mainDataItemAdapter.refresh()
            DataState.isSingleAppUpdated = false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}