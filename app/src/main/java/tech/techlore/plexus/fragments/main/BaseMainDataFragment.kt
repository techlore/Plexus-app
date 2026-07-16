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
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.TransitionManager
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.interfaces.main.FavToggleListener
import tech.techlore.plexus.interfaces.main.SortPrefsChangeListener
import tech.techlore.plexus.interfaces.main.ViewStyleChangeListener
import tech.techlore.plexus.models.mini.MainDataMini
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.PreferenceManager
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
    ViewStyleChangeListener,
    FavToggleListener {
    
    private var _binding: RecyclerViewBinding? = null
    protected val fragmentBinding get() = _binding!!
    protected lateinit var mainActivity: MainActivity
    protected lateinit var mainDataItemAdapter: MainDataItemAdapter
    protected lateinit var mainDataList: ArrayList<MainDataMini>
    protected val prefManager by inject<PreferenceManager>()
    protected val mainRepository by inject<MainDataRepository>()
    private var isViewStubInflated = false
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
        mainActivity = requireActivity() as MainActivity
        
        // Adjust recycler view for edge to edge
        fragmentBinding.recyclerView.adjustEdgeToEdge(requireContext())
        
        mainDataItemAdapter =
            MainDataItemAdapter(
                clickListener = this@BaseMainDataFragment,
                favToggleListener = this@BaseMainDataFragment,
                isFavFrag = !isSwipeRefreshEnabled()
            )
        
        fragmentBinding.recyclerView.apply {
            mainActivity.activityBinding.mainAppBar.liftOnScrollTargetViewId = this.id
            layoutManager = getViewStyle(requireContext(), mainActivity.isGridView)
            adapter = mainDataItemAdapter
            FastScrollerBuilder(this).build()
        }
        
        lifecycleScope.launch {
            mainDataList = getDataFromDB()
            
            if (mainDataList.isEmpty()) showEmptyListView()
            else {
                mainDataItemAdapter.apply {
                    isGridViewLayout = mainActivity.isGridView
                    submitList(mainDataList)
                }
            }
            
            // Swipe refresh layout
            if (isSwipeRefreshEnabled())
                fragmentBinding.swipeRefreshLayout.setOnRefreshListener {
                    lifecycleScope.launch { onSwipeRefresh() }
                }
            else fragmentBinding.swipeRefreshLayout.isEnabled = false
        }
    }
    
    protected abstract suspend fun getDataFromDB(): ArrayList<MainDataMini>
    
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
    
    override fun onItemClick(position: Int) {
        clickedItemPos = position
        clickedItemPackageName = mainDataList[position].packageName
        mainActivity.startDetailsActivity(clickedItemPackageName)
    }
    
    override fun onViewStyleChanged() {
        mainDataItemAdapter.isGridViewLayout = mainActivity.isGridView
        fragmentBinding.recyclerView.apply {
            TransitionManager.beginDelayedTransition(this, Fade())
            layoutManager = getViewStyle(requireContext(), mainActivity.isGridView)
            recycledViewPool.clear()
            smoothScrollToPosition(0) // Scroll to top
        }
    }
    
    override fun onSortPrefsChanged(isAsc: Boolean, onlyAzChanged: Boolean) {
        if (onlyAzChanged) {
            if (mainDataList.isNotEmpty()) {
                mainDataList =
                    ArrayList(
                        if (isAsc) mainDataList.sortedBy { it.name }
                        else mainDataList.sortedByDescending { it.name }
                    )
                mainDataItemAdapter.submitList(null)
                fragmentBinding.recyclerView.apply {
                    post {
                        TransitionManager.beginDelayedTransition(
                            this,
                            Fade().apply { duration = 500 }
                        )
                        mainDataItemAdapter.submitList(mainDataList)
                        smoothScrollToPosition(0) // Scroll to top
                    }
                }
            }
        }
        else {
            lifecycleScope.launch {
                getDataFromDB().let {
                    mainDataItemAdapter.submitList(null)
                    if (it.isEmpty()) showEmptyListView()
                    else {
                        hideEmptyListView()
                        mainDataItemAdapter.submitList(it)
                    }
                    mainDataList = it
                    fragmentBinding.recyclerView.smoothScrollToPosition(0)
                }
            }
        }
    }
    
    override fun onFavToggled(item: MainDataMini, isChecked: Boolean) {
        item.isFav = isChecked
        lifecycleScope.launch {
            mainRepository.updateFav(item)
        }
        showSnackbar(
            coordinatorLayout = mainActivity.activityBinding.mainCoordLayout,
            message =
                if (isChecked) getString(R.string.added_to_fav, item.name)
                else getString(R.string.removed_from_fav, item.name),
            anchorView = mainActivity.activityBinding.mainDockedToolbar
        )
    }
    
    override fun onResume() {
        super.onResume()
        if (DataState.isSingleAppUpdated) {
            lifecycleScope.launch{
                ArrayList(mainDataList)
                    .apply {
                        this[clickedItemPos] =
                            mainRepository.getMiniAppByPackage(clickedItemPackageName)
                    }
                    .let {
                        mainDataItemAdapter.submitList(it)
                        mainDataList = it
                    }
                
                clickedItemPos = -1
                clickedItemPackageName = ""
                DataState.isSingleAppUpdated = false
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}