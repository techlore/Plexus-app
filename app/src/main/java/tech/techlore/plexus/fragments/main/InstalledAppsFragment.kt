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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.InstalledAppItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.utils.IntentUtils.Companion.appDetails
import tech.techlore.plexus.utils.IntentUtils.Companion.reloadFragment
import tech.techlore.plexus.utils.ListUtils.Companion.installedAppsStatusSort
import tech.techlore.plexus.utils.ListUtils.Companion.scanInstalledApps
import tech.techlore.plexus.utils.UiUtils.Companion.longClickBottomSheet
import kotlin.collections.ArrayList

class InstalledAppsFragment :
    Fragment(),
    InstalledAppItemAdapter.OnItemClickListener,
    InstalledAppItemAdapter.OnItemLongCLickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var installedAppsFinalList: ArrayList<InstalledApp>
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = RecyclerViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val preferenceManager = PreferenceManager(requireContext())
        mainActivity = requireActivity() as MainActivity
        var installedAppsTempList: ArrayList<InstalledApp> = ArrayList()
        installedAppsFinalList = ArrayList()
        val playStoreInstallers: List<String?> = ArrayList(listOf("com.android.vending", "com.aurora.store"))
        val installedAppItemAdapter = InstalledAppItemAdapter(installedAppsFinalList, this , this)
        
        /*########################################################################################*/
        
        fragmentBinding.recyclerView.addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
        
        // Filter based on installers (play store, aurora etc.)
        if (preferenceManager.getInt(PreferenceManager.FILTER_PREF) == 0
            || preferenceManager.getInt(PreferenceManager.FILTER_PREF) == R.id.menu_all_apps) {
            installedAppsTempList = mainActivity.installedList
        }
        else if (preferenceManager.getInt(PreferenceManager.FILTER_PREF) == R.id.menu_play_apps) {
            for (installedApp in mainActivity.installedList) {
                val installerName = requireContext().packageManager
                    .getInstallerPackageName(installedApp.packageName)
                if (playStoreInstallers.contains(installerName)) {
                    installedAppsTempList.add(installedApp)
                }
            }
        }
        else {
            for (installedApp in mainActivity.installedList) {
                val installerName = requireContext().packageManager
                    .getInstallerPackageName(installedApp.packageName)
                if (! playStoreInstallers.contains(installerName)) {
                    installedAppsTempList.add(installedApp)
                }
            }
        }
        
        // Status sort
        for (installedApp in installedAppsTempList) {
            if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == 0
                || preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == R.id.radio_any_status) {
                installedAppsFinalList.add(installedApp)
            }
            else if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == R.id.radio_dg_status) {
                installedAppsStatusSort(preferenceManager.getInt(PreferenceManager.DG_STATUS_SORT_PREF),
                                        installedApp,
                                        installedApp.dgRating,
                                        installedAppsFinalList)
            }
            else if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == R.id.radio_mg_status) {
                installedAppsStatusSort(preferenceManager.getInt(PreferenceManager.MG_STATUS_SORT_PREF),
                                        installedApp,
                                        installedApp.mgRating,
                                        installedAppsFinalList)
            }
        }
        
        // Alphabetical sort
        if (preferenceManager.getInt(PreferenceManager.A_Z_SORT_PREF) == 0
            || preferenceManager.getInt(PreferenceManager.A_Z_SORT_PREF) == R.id.sort_a_z) {
            installedAppsFinalList.sortWith { ai1: InstalledApp, ai2: InstalledApp ->
                ai1.name.compareTo(ai2.name) } // A-Z
        }
        else {
            installedAppsFinalList.sortWith { ai1: InstalledApp, ai2: InstalledApp ->
                ai2.name.compareTo(ai1.name) } // Z-A
        }
        
        if (installedAppsFinalList.size == 0) {
            fragmentBinding.emptyListViewStub.inflate()
        }
        else {
            fragmentBinding.recyclerView.adapter = installedAppItemAdapter
            FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build() // Fast scroll
        }
        
        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_background, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.color_secondary, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setOnRefreshListener {
            mainActivity.installedList.clear()
            scanInstalledApps(requireContext(), mainActivity.dataList, mainActivity.installedList)
            fragmentBinding.swipeRefreshLayout.isRefreshing = false
            reloadFragment(parentFragmentManager, this)
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val installedApp = installedAppsFinalList[position]
        appDetails(mainActivity,
                   installedApp.name,
                   installedApp.packageName /*installedApp.getPlexusVersion()*/,
                   installedApp.installedVersion /*installedApp.getDgNotes(), installedApp.getMgNotes(),
                       installedApp.getDgRating(), installedApp.getMgRating()*/
        )
    }
    
    // On long click
    override fun onItemLongCLick(position: Int) {
        val installedApp = installedAppsFinalList[position]
        longClickBottomSheet(mainActivity,
                             installedApp.name, installedApp.packageName,  /*installedApp.getPlexusVersion(),
                                 installedApp.getDgRating(), installedApp.getMgRating(),
                                 installedApp.getDgNotes(), installedApp.getMgNotes(),*/
                             mainActivity.activityBinding.mainCoordinatorLayout,
                             mainActivity.activityBinding.bottomNavContainer)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}