/*
 * Copyright (c) 2022-present Techlore
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
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
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_RADIO
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import kotlin.collections.ArrayList

class InstalledAppsFragment :
    Fragment(),
    MainDataItemAdapter.OnItemClickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
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
        
        preferenceManager = PreferenceManager(requireContext())
        mainActivity = requireActivity() as MainActivity
        miniRepository = (requireContext().applicationContext as ApplicationManager).miniRepository
        
        /*########################################################################################*/
        
        lifecycleScope.launch {
            installedAppsList =
                miniRepository.miniInstalledAppsListFromDB(context = requireContext(),
                                                           installedFromPref = preferenceManager.getInt(INSTALLED_FROM_SORT),
                                                           statusRadioPref = preferenceManager.getInt(STATUS_RADIO),
                                                           orderPref = preferenceManager.getInt(A_Z_SORT))
            
            fragmentBinding.recyclerView.addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
            
            if (installedAppsList.isEmpty()) {
                fragmentBinding.emptyListViewStub.inflate()
            }
            else {
                installedAppItemAdapter = MainDataItemAdapter(installedAppsList,
                                                              this@InstalledAppsFragment,
                                                              lifecycleScope)
                fragmentBinding.recyclerView.adapter = installedAppItemAdapter
                FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build() // Fast scroll
            }
            
            // Swipe refresh layout
            fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_background, requireContext().theme))
            fragmentBinding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.color_secondary, requireContext().theme))
            fragmentBinding.swipeRefreshLayout.setOnRefreshListener { refreshInstalledApps() }
        }
    }
    
    private fun refreshInstalledApps() {
        lifecycleScope.launch {
            val mainRepository = (requireContext().applicationContext as ApplicationManager).mainRepository
            mainRepository.installedAppsIntoDB(requireContext())
            fragmentBinding.swipeRefreshLayout.isRefreshing = false
            installedAppItemAdapter
                .updateList(miniRepository
                                .miniInstalledAppsListFromDB(context = requireContext(),
                                                             installedFromPref = preferenceManager.getInt(INSTALLED_FROM_SORT),
                                                             statusRadioPref = preferenceManager.getInt(STATUS_RADIO),
                                                             orderPref = preferenceManager.getInt(A_Z_SORT)))
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val installedApp = installedAppsList[position]
        startDetailsActivity(mainActivity, installedApp.packageName)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}