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
import tech.techlore.plexus.adapters.PlexusDataItemAdapter
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.fragments.bottomsheets.LongClickBottomSheet
import tech.techlore.plexus.fragments.dialogs.NoNetworkDialog
import tech.techlore.plexus.listeners.RecyclerViewItemTouchListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT_PREF
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_RADIO_PREF
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment
import kotlin.coroutines.CoroutineContext

class PlexusDataFragment :
    Fragment(),
    PlexusDataItemAdapter.OnItemClickListener,
    PlexusDataItemAdapter.OnItemLongCLickListener,
    CoroutineScope {
    
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var plexusDataList: ArrayList<MainDataMinimal>
    
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
        val miniRepository = (requireContext().applicationContext as ApplicationManager).miniRepository
        runBlocking {
            launch {
                plexusDataList =
                    miniRepository.miniPlexusDataListFromDB(orderPref = preferenceManager.getInt(A_Z_SORT_PREF))
            }
        }
        
        /*########################################################################################*/
        
        fragmentBinding.recyclerView.addOnItemTouchListener(RecyclerViewItemTouchListener(mainActivity))
    
        if (plexusDataList.size == 0) {
            fragmentBinding.emptyListViewStub.inflate()
        }
        else {
            val plexusDataItemAdapter = PlexusDataItemAdapter(plexusDataList,
                                                              this,
                                                              this,
                                                              coroutineScope)
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
        LongClickBottomSheet(mainActivity, plexusData.name, plexusData.packageName,  /*plexusData.version,
                                 plexusData.dgStatus, plexusData.mgStatus,
                                 plexusData.dgNotes, plexusData.mgNotes,*/
                             mainActivity.activityBinding.mainCoordinatorLayout,
                             mainActivity.activityBinding.bottomNavContainer)
            .show(parentFragmentManager, "LongClickBottomSheet")
    }
    
    private fun refreshData() {
        
        launch {
            if (hasNetwork(requireContext()) && hasInternet()) {
                val repository = (requireContext().applicationContext as ApplicationManager).mainRepository
                repository.plexusDataIntoDB(requireContext())
                fragmentBinding.swipeRefreshLayout.isRefreshing = false
                refreshFragment(mainActivity.navController)
            }
            else {
                NoNetworkDialog(negativeButtonText = getString(R.string.cancel),
                                positiveButtonClickListener = {
                                    refreshData()
                                },
                                negativeButtonClickListener = {
                                    fragmentBinding.swipeRefreshLayout.isRefreshing = false
                                })
                    .show(parentFragmentManager, "NoNetworkDialog")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
        _binding = null
    }
}