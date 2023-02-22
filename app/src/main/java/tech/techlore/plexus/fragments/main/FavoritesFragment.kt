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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.adapters.FavoriteItemAdapter
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.fragments.bottomsheets.LongClickBottomSheet
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.utils.IntentUtils
import kotlin.coroutines.CoroutineContext

class FavoritesFragment:
    Fragment(),
    FavoriteItemAdapter.OnItemClickListener,
    FavoriteItemAdapter.OnItemLongCLickListener,
    CoroutineScope {
    
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainFavList: ArrayList<MainDataMinimal>
    private lateinit var favFinalList: ArrayList<MainDataMinimal>
    
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
        var favTempList: ArrayList<MainDataMinimal> = ArrayList()
        favFinalList = ArrayList()
        val playStoreInstallers: List<String?> = ArrayList(listOf("com.android.vending", "com.aurora.store"))
        val repository = (requireContext().applicationContext as ApplicationManager).miniRepository
        runBlocking {
            launch {
                mainFavList = repository.miniFavoritesListFromDB()
            }
        }
        val favItemAdapter = FavoriteItemAdapter(favFinalList,
                                                 this,
                                                 this,
                                                 coroutineScope)
        
        /*########################################################################################*/
        
        fragmentBinding.recyclerView.addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
        
        // Filter based on installers (play store, aurora etc.)
        if (preferenceManager.getInt(PreferenceManager.FILTER) == 0
            || preferenceManager.getInt(PreferenceManager.FILTER) == R.id.menu_all_apps) {
            favTempList = mainFavList
        }
        else if (preferenceManager.getInt(PreferenceManager.FILTER) == R.id.menu_play_apps) {
            for (installedApp in mainFavList) {
                if (playStoreInstallers.contains(installedApp.installedFrom)) {
                    favTempList.add(installedApp)
                }
            }
        }
        else {
            for (installedApp in mainFavList) {
                if (! playStoreInstallers.contains(installedApp.installedFrom)) {
                    favTempList.add(installedApp)
                }
            }
        }
        
        // Status sort
        for (installedApp in favTempList) {
            if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO) == 0
                || preferenceManager.getInt(PreferenceManager.STATUS_RADIO) == R.id.radio_any_status) {
                favFinalList.add(installedApp)
            }
            else if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO) == R.id.radio_dg_status) {
                /*installedAppsStatusSort(preferenceManager.getInt(PreferenceManager.DG_STATUS_SORT_PREF),
                                        installedApp,
                                        installedApp.dgRating,
                                        installedAppsFinalList)*/
            }
            else if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO) == R.id.radio_mg_status) {
                /*installedAppsStatusSort(preferenceManager.getInt(PreferenceManager.MG_STATUS_SORT_PREF),
                                        installedApp,
                                        installedApp.mgRating,
                                        installedAppsFinalList)*/
            }
        }
        
        // Alphabetical sort
        if (preferenceManager.getInt(PreferenceManager.A_Z_SORT) == 0
            || preferenceManager.getInt(PreferenceManager.A_Z_SORT) == R.id.sort_a_z) {
            favFinalList.sortWith { ai1: MainDataMinimal, ai2: MainDataMinimal ->
                ai1.name.compareTo(ai2.name) } // A-Z
        }
        else {
            favFinalList.sortWith { ai1: MainDataMinimal, ai2: MainDataMinimal ->
                ai2.name.compareTo(ai1.name) } // Z-A
        }
        
        if (favFinalList.size == 0) {
            fragmentBinding.emptyListViewStub.inflate()
        }
        else {
            fragmentBinding.recyclerView.adapter = favItemAdapter
            FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build() // Fast scroll
        }
        
        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(
            R.color.color_background, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.color_secondary, requireContext().theme))
        /*fragmentBinding.swipeRefreshLayout.setOnRefreshListener {
            launch {
                val db = DbUtils.getDatabase(requireContext())
                val plexusDataDao = db.mainDataDao()
                mainActivity.installedList.clear()
                DbUtils.installedAppsIntoDB(requireContext(), plexusDataDao)
                mainActivity.installedList = DbUtils.installedAppsListFromDB(plexusDataDao)
                fragmentBinding.swipeRefreshLayout.isRefreshing = false
                IntentUtils.refreshFragment(mainActivity.navController)
            }
        }*/
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val fav = favFinalList[position]
        IntentUtils.startDetailsActivity(mainActivity, fav.packageName, "installed")
    }
    
    // On long click
    override fun onItemLongCLick(position: Int) {
        val fav = favFinalList[position]
        LongClickBottomSheet(mainActivity,
                                     fav.name, fav.packageName,  /*installedApp.getPlexusVersion(),
                                 installedApp.getDgRating(), installedApp.getMgRating(),
                                 installedApp.getDgNotes(), installedApp.getMgNotes(),*/
                                     mainActivity.activityBinding.mainCoordinatorLayout,
                                     mainActivity.activityBinding.bottomNavContainer)
            .show(parentFragmentManager, "LongClickBottomSheet")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
        _binding = null
    }
    
}