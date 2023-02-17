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
import tech.techlore.plexus.adapters.InstalledAppItemAdapter
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.fragments.bottomsheets.LongClickBottomSheet
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT_PREF
import tech.techlore.plexus.preferences.PreferenceManager.Companion.FILTER_PREF
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class InstalledAppsFragment :
    Fragment(),
    InstalledAppItemAdapter.OnItemClickListener,
    InstalledAppItemAdapter.OnItemLongCLickListener,
    CoroutineScope {
    
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var installedAppsList: ArrayList<MainDataMinimal>
    
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
        val repository = (requireContext().applicationContext as ApplicationManager).miniRepository
        runBlocking {
            launch {
                installedAppsList =
                    repository.miniInstalledAppsListFromDB(filterPref = preferenceManager.getInt(FILTER_PREF),
                                                           orderPref = preferenceManager.getInt(A_Z_SORT_PREF))
            }
        }
        
        /*########################################################################################*/
        
        fragmentBinding.recyclerView.addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
        
        if (installedAppsList.size == 0) {
            fragmentBinding.emptyListViewStub.inflate()
        }
        else {
            val installedAppItemAdapter = InstalledAppItemAdapter(installedAppsList,
                                                                  this ,
                                                                  this,
                                                                  coroutineScope)
            fragmentBinding.recyclerView.adapter = installedAppItemAdapter
            FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build() // Fast scroll
        }
        
        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_background, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.color_secondary, requireContext().theme))
        fragmentBinding.swipeRefreshLayout.setOnRefreshListener {
            launch {
                val mainRepository = (requireContext().applicationContext as ApplicationManager).mainRepository
                mainRepository.installedAppsIntoDB(requireContext())
                fragmentBinding.swipeRefreshLayout.isRefreshing = false
                refreshFragment(mainActivity.navController)
            }
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val installedApp = installedAppsList[position]
        startDetailsActivity(mainActivity, installedApp.packageName, "installed")
    }
    
    // On long click
    override fun onItemLongCLick(position: Int) {
        val installedApp = installedAppsList[position]
        LongClickBottomSheet(mainActivity,
                             installedApp.name, installedApp.packageName,  /*installedApp.getPlexusVersion(),
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