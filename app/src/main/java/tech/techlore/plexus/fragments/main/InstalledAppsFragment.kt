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
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.INSTALLED_FROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.UiUtils.Companion.adjustRecyclerView
import kotlin.collections.ArrayList

class InstalledAppsFragment :
    Fragment(),
    MainDataItemAdapter.OnItemClickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var appManager: ApplicationManager
    private lateinit var mainActivity: MainActivity
    private lateinit var installedAppItemAdapter: MainDataItemAdapter
    private lateinit var installedAppsList: ArrayList<MainDataMinimal>
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
        
        // Adjust recycler view for edge to edge
        adjustRecyclerView(requireContext(), fragmentBinding.recyclerView)
        
        lifecycleScope.launch {
            
            // Forcefully set toolbar title
            // to avoid issues when navigating from "My ratings" using fab
            mainActivity.activityBinding.toolbarBottom.title = getString(R.string.installed_apps)
            
            installedAppsList =
                miniRepository.miniInstalledAppsListFromDB(installedFromPref = preferenceManager.getInt(INSTALLED_FROM_SORT),
                                                           statusToggleBtnPref = preferenceManager.getInt(STATUS_TOGGLE),
                                                           orderPref = preferenceManager.getInt(A_Z_SORT))
            
            if (installedAppsList.isEmpty()) {
                fragmentBinding.emptyListViewStub.inflate()
            }
            else {
                installedAppItemAdapter = MainDataItemAdapter(installedAppsList,
                                                              this@InstalledAppsFragment,
                                                              lifecycleScope)
                fragmentBinding.recyclerView.apply {
                    addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
                    adapter = installedAppItemAdapter
                    FastScrollerBuilder(this).build() // Fast scroll
                }
            }
            
            // Swipe refresh layout
            fragmentBinding.swipeRefreshLayout.apply {
                setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_background, requireContext().theme))
                setColorSchemeColors(resources.getColor(R.color.color_secondary, requireContext().theme))
                setOnRefreshListener { refreshInstalledApps() }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (appManager.isDataUpdated) {
            lifecycleScope.launch {
                installedAppItemAdapter
                    .updateList(miniRepository
                                    .miniInstalledAppsListFromDB(installedFromPref = preferenceManager.getInt(INSTALLED_FROM_SORT),
                                                                 statusToggleBtnPref = preferenceManager.getInt(STATUS_TOGGLE),
                                                                 orderPref = preferenceManager.getInt(A_Z_SORT)))
                appManager.isDataUpdated = false
            }
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val installedApp = installedAppsList[position]
        startDetailsActivity(mainActivity, installedApp.packageName)
    }
    
    private fun refreshInstalledApps() {
        lifecycleScope.launch {
            val mainRepository = appManager.mainRepository
            mainRepository.installedAppsIntoDB(requireContext())
            fragmentBinding.swipeRefreshLayout.isRefreshing = false
            installedAppItemAdapter
                .updateList(miniRepository
                                .miniInstalledAppsListFromDB(installedFromPref = preferenceManager.getInt(INSTALLED_FROM_SORT),
                                                             statusToggleBtnPref = preferenceManager.getInt(STATUS_TOGGLE),
                                                             orderPref = preferenceManager.getInt(A_Z_SORT)))
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}