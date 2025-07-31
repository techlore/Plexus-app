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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.interfaces.FavToggleListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar
import kotlin.getValue

abstract class BaseMainDataFragment : Fragment(), MainDataItemAdapter.OnItemClickListener, FavToggleListener {
    
    private var _binding: RecyclerViewBinding? = null
    protected val fragmentBinding get() = _binding!!
    protected lateinit var mainActivity: MainActivity
    protected lateinit var mainDataItemAdapter: MainDataItemAdapter
    protected lateinit var mainDataList: ArrayList<MainDataMinimal>
    protected val prefManager by inject<PreferenceManager>()
    protected val miniRepository by inject<MainDataMinimalRepository>()
    
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
        
        lifecycleScope.launch {
            mainDataList = getDataFromDB()
            
            mainDataItemAdapter =
                MainDataItemAdapter(
                    clickListener = this@BaseMainDataFragment,
                    favToggleListener = this@BaseMainDataFragment,
                    isGridView = mainActivity.isGridView,
                    isFavFrag = !isSwipeRefreshEnabled()
                )
            
            if (mainDataList.isEmpty()) fragmentBinding.emptyListViewStub.inflate()
            else
                fragmentBinding.recyclerView.apply {
                    mainActivity.activityBinding.mainAppBar.liftOnScrollTargetViewId = this.id
                    layoutManager =
                        if (! mainActivity.isGridView) LinearLayoutManager(requireContext())
                        else GridLayoutManager(requireContext(), 2)
                    adapter = mainDataItemAdapter
                    FastScrollerBuilder(this).build() // Fast scroll
                }
            
            mainDataItemAdapter.submitList(mainDataList)
            
            // Swipe refresh layout
            if (isSwipeRefreshEnabled())
                fragmentBinding.swipeRefreshLayout.setOnRefreshListener {
                    lifecycleScope.launch { onSwipeRefresh() }
                }
            else fragmentBinding.swipeRefreshLayout.isEnabled = false
        }
    }
    
    protected abstract suspend fun getDataFromDB(): ArrayList<MainDataMinimal>
    
    protected open fun isSwipeRefreshEnabled(): Boolean = true
    
    protected open fun onSwipeRefresh() {}
    
    // On click
    override fun onItemClick(position: Int) {
        mainActivity.startDetailsActivity(mainDataList[position].packageName)
    }
    
    override fun onFavToggled(item: MainDataMinimal, isChecked: Boolean) {
        item.isFav = isChecked
        lifecycleScope.launch {
            get<MainDataMinimalRepository>().updateFav(item)
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
        if (DataState.isDataUpdated) {
            lifecycleScope.launch{
                mainDataItemAdapter.submitList(getDataFromDB())
                DataState.isDataUpdated = false
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}