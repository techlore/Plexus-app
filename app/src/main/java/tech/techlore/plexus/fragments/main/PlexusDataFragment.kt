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
import kotlinx.coroutines.launch
import me.stellarsand.android.fastscroll.FastScrollerBuilder
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.fragments.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork

class PlexusDataFragment :
    Fragment(),
    MainDataItemAdapter.OnItemClickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var appManager: ApplicationManager
    private lateinit var mainActivity: MainActivity
    private lateinit var plexusDataItemAdapter: MainDataItemAdapter
    private lateinit var plexusDataList: ArrayList<MainDataMinimal>
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var miniRepository: MainDataMinimalRepository
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        appManager = requireContext().applicationContext as ApplicationManager
        preferenceManager = appManager.preferenceManager
        mainActivity = requireActivity() as MainActivity
        miniRepository = appManager.miniRepository
        
        lifecycleScope.launch{
            plexusDataList =
                miniRepository.miniPlexusDataListFromDB(statusToggleBtnPref = preferenceManager.getInt(STATUS_TOGGLE),
                                                        orderPref = preferenceManager.getInt(A_Z_SORT))
            
            if (plexusDataList.isEmpty()) {
                fragmentBinding.emptyListViewStub.inflate()
            }
            else {
                plexusDataItemAdapter = MainDataItemAdapter(plexusDataList,
                                                            this@PlexusDataFragment,
                                                            lifecycleScope)
                fragmentBinding.recyclerView.apply {
                    addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
                    adapter = plexusDataItemAdapter
                    FastScrollerBuilder(this).build() // Fast scroll
                }
            }
            
            // Swipe refresh layout
            fragmentBinding.swipeRefreshLayout.apply {
                setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_background, requireContext().theme))
                setColorSchemeColors(resources.getColor(R.color.color_secondary, requireContext().theme))
                setOnRefreshListener { refreshData() }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (appManager.isDataUpdated) {
            lifecycleScope.launch{
                plexusDataItemAdapter
                    .updateList(miniRepository
                                    .miniPlexusDataListFromDB(statusToggleBtnPref = preferenceManager.getInt(STATUS_TOGGLE),
                                                              orderPref = preferenceManager.getInt(A_Z_SORT)))
                appManager.isDataUpdated = false
            }
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val plexusData = plexusDataList[position]
        startDetailsActivity(mainActivity, plexusData.packageName)
    }
    
    private fun refreshData() {
        
        lifecycleScope.launch{
            if (hasNetwork(requireContext()) && hasInternet()) {
                val mainRepository = appManager.mainRepository
                mainRepository.plexusDataIntoDB(requireContext())
                plexusDataItemAdapter
                    .updateList(miniRepository
                                    .miniPlexusDataListFromDB(statusToggleBtnPref = preferenceManager.getInt(STATUS_TOGGLE),
                                                              orderPref = preferenceManager.getInt(A_Z_SORT)))
                fragmentBinding.swipeRefreshLayout.isRefreshing = false
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveButtonClickListener = { refreshData() },
                                     negativeButtonClickListener = {
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