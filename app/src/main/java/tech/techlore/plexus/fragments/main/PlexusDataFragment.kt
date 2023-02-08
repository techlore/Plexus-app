/*
 * Copyright (c) 2022 Techlore
 *
 *  This file is part of Plexus.
 *
 *  Plexus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plexus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plexus.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.fragments.main

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.PlexusDataItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.PlexusData
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.utils.DbUtils.Companion.getDatabase
import tech.techlore.plexus.utils.DbUtils.Companion.plexusDataIntoDB
import tech.techlore.plexus.utils.DbUtils.Companion.plexusDataListFromDB
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.IntentUtils.Companion.reloadFragment
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.longClickBottomSheet
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class PlexusDataFragment :
    Fragment(),
    PlexusDataItemAdapter.OnItemClickListener,
    PlexusDataItemAdapter.OnItemLongCLickListener,
    CoroutineScope {
    
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var plexusDataList: ArrayList<PlexusData>
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val preferenceManager = PreferenceManager(requireContext())
        mainActivity = requireActivity() as MainActivity
        plexusDataList = ArrayList()
        val plexusDataItemAdapter = PlexusDataItemAdapter(plexusDataList,
                                                          this,
                                                          this,
                                                          coroutineScope)
        
        /*########################################################################################*/
        
        fragmentBinding.recyclerView.addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
        
        // Status sort
        for (plexusData in mainActivity.dataList) {
            if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == 0
                || preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == R.id.radio_any_status) {
                plexusDataList.add(plexusData)
            }
            //            else if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_dg_status) {
//
//                PlexusDataStatusSort(preferenceManager.getInt(DG_STATUS_SORT_PREF), plexusData,
//                        plexusData.dgStatus, plexusDataList);
//            }
//            else if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_mg_status) {
//
//                PlexusDataStatusSort(preferenceManager.getInt(MG_STATUS_SORT_PREF), plexusData,
//                        plexusData.mgStatus, plexusDataList);
//            }
        }
        
        
        // Alphabetical sort
        if (preferenceManager.getInt(PreferenceManager.A_Z_SORT_PREF) == 0
            || preferenceManager.getInt(PreferenceManager.A_Z_SORT_PREF) == R.id.sort_a_z) {
            plexusDataList.sortWith { ai1: PlexusData, ai2: PlexusData ->
                ai1.name.compareTo(ai2.name) } // A-Z
        }
        else {
            plexusDataList.sortWith { ai1: PlexusData, ai2: PlexusData ->
                ai2.name.compareTo(ai1.name) } // Z-A
        }
        
        if (mainActivity.dataList.size == 0) {
            fragmentBinding.emptyListViewStub.inflate()
        }
        else {
            fragmentBinding.recyclerView.adapter = plexusDataItemAdapter
            FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build() // Fast scroll
        }
        
        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_background, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.color_secondary, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setOnRefreshListener { refreshData() }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val plexusData = plexusDataList[position]
        startDetailsActivity(mainActivity, plexusData.packageName, "plexus")
    }
    
    // On long click
    override fun onItemLongCLick(position: Int) {
        val plexusData = plexusDataList[position]
        longClickBottomSheet(mainActivity, plexusData.name, plexusData.packageName,  /*plexusData.version,
                                 plexusData.dgStatus, plexusData.mgStatus,
                                 plexusData.dgNotes, plexusData.mgNotes,*/
                             mainActivity.activityBinding.mainCoordinatorLayout,
                             mainActivity.activityBinding.bottomNavContainer)
    }
    
    private fun noNetworkDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
            
            .setTitle(R.string.dialog_title)
            
            .setMessage(R.string.dialog_subtitle)
            
            .setPositiveButton(R.string.retry) { _: DialogInterface?, _: Int ->
                refreshData() }
                
            .setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int ->
                fragmentBinding.swipeRefreshLayout.isRefreshing = false }
                
            .setCancelable(false)
            
            .show()
    }
    
    private fun refreshData() {
        
        launch {
            if (hasNetwork(requireContext()) && hasInternet()) {
                val db = getDatabase(requireContext())
                plexusDataIntoDB(db.plexusDataDao())
                mainActivity.dataList = plexusDataListFromDB(db.plexusDataDao())
                reloadFragment(parentFragmentManager, this@PlexusDataFragment)
                fragmentBinding.swipeRefreshLayout.isRefreshing = false
            }
            else {
                noNetworkDialog()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
        _binding = null
    }
}