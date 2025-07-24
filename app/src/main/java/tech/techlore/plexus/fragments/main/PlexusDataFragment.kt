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
import tech.techlore.plexus.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.interfaces.FavToggleListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.adjustEdgeToEdge
import kotlin.getValue

class PlexusDataFragment :
    Fragment(),
    MainDataItemAdapter.OnItemClickListener,
    FavToggleListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var plexusDataItemAdapter: MainDataItemAdapter
    private lateinit var plexusDataList: ArrayList<MainDataMinimal>
    private val prefManager by inject<PreferenceManager>()
    private val miniRepository by inject<MainDataMinimalRepository>()
    
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
        
        lifecycleScope.launch{
            plexusDataList =
                miniRepository.miniPlexusDataListFromDB(
                    statusToggleBtnPref = prefManager.getInt(STATUS_TOGGLE),
                    orderPref = prefManager.getInt(A_Z_SORT)
                )
            
            if (plexusDataList.isEmpty()) {
                fragmentBinding.emptyListViewStub.inflate()
            }
            else {
                plexusDataItemAdapter =
                    MainDataItemAdapter(clickListener = this@PlexusDataFragment,
                                        favToggleListener = this@PlexusDataFragment,
                                        isGridView = mainActivity.isGridView)
                fragmentBinding.recyclerView.apply {
                    mainActivity.activityBinding.mainAppBar.liftOnScrollTargetViewId = this.id
                    layoutManager =
                        if (!mainActivity.isGridView)
                            LinearLayoutManager(requireContext())
                        else
                            GridLayoutManager(requireContext(), 2)
                    adapter = plexusDataItemAdapter
                    FastScrollerBuilder(this).build() // Fast scroll
                }
                plexusDataItemAdapter.submitList(plexusDataList)
            }
            
            // Swipe refresh layout
            fragmentBinding.swipeRefreshLayout.setOnRefreshListener { refreshData() }
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (DataState.isDataUpdated) {
            lifecycleScope.launch{
                plexusDataItemAdapter.submitList(
                    miniRepository.miniPlexusDataListFromDB(
                        statusToggleBtnPref = prefManager.getInt(STATUS_TOGGLE),
                        orderPref = prefManager.getInt(A_Z_SORT)
                    )
                )
                DataState.isDataUpdated = false
            }
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val plexusData = plexusDataList[position]
        mainActivity.startDetailsActivity(plexusData.packageName)
    }
    
    override fun onFavToggled(item: MainDataMinimal, isChecked: Boolean) {
        item.isFav = isChecked
        lifecycleScope.launch {
            get<MainDataMinimalRepository>().updateFav(item)
        }
    }
    
    private fun refreshData() {
        lifecycleScope.launch{
            if (hasNetwork(requireContext()) && hasInternet()) {
                try {
                    get<MainDataRepository>().plexusDataIntoDB()
                    plexusDataItemAdapter.submitList(
                        miniRepository.miniPlexusDataListFromDB(
                            statusToggleBtnPref = prefManager.getInt(STATUS_TOGGLE),
                            orderPref = prefManager.getInt(A_Z_SORT)
                        )
                    )
                    fragmentBinding.swipeRefreshLayout.isRefreshing = false
                }
                catch (e: Exception) {
                    NoNetworkBottomSheet(isNoNetworkError = false,
                                         exception = e,
                                         negativeButtonText = getString(R.string.exit),
                                         positiveBtnClickAction = { refreshData() },
                                         negativeBtnClickAction = {
                                             fragmentBinding.swipeRefreshLayout.isRefreshing = false
                                         })
                        .show(parentFragmentManager, "NoNetworkBottomSheet")
                }
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveBtnClickAction = { refreshData() },
                                     negativeBtnClickAction = {
                                         fragmentBinding.swipeRefreshLayout.isRefreshing = false
                                     })
                    .show(parentFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}