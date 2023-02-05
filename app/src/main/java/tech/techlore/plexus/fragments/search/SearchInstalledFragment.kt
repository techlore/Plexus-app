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

package tech.techlore.plexus.fragments.search

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import tech.techlore.plexus.activities.SearchActivity
import tech.techlore.plexus.adapters.InstalledAppItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.utils.IntentUtils.Companion.appDetails

class SearchInstalledFragment :
    Fragment(),
    InstalledAppItemAdapter.OnItemClickListener,
    InstalledAppItemAdapter.OnItemLongCLickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var searchActivity: SearchActivity
    private lateinit var installedAppItemAdapter: InstalledAppItemAdapter
    private lateinit var searchInstalledList: ArrayList<InstalledApp>
    private var delayTimer: CountDownTimer? = null
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = RecyclerViewBinding.inflate(layoutInflater)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        searchActivity = requireActivity() as SearchActivity
        searchInstalledList = searchActivity.installedList
        installedAppItemAdapter = InstalledAppItemAdapter(searchInstalledList, this, this)
        
        /*########################################################################################*/
        
        fragmentBinding.swipeRefreshLayout.isEnabled = false
        fragmentBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Perform search
        searchActivity.searchViewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            
            override fun onQueryTextSubmit(searchString: String): Boolean {
                return true
            }
            
            override fun onQueryTextChange(searchString: String): Boolean {
                if (delayTimer != null) {
                    delayTimer !!.cancel()
                }
                
                // Search with a subtle delay
                delayTimer = object : CountDownTimer(350, 150) {
                    
                    override fun onTick(millisUntilFinished: Long) {}
                    
                    override fun onFinish() {
                        if (searchString.isNotEmpty()) {
                            installedAppItemAdapter.filter.filter(searchString)
                            fragmentBinding.recyclerView.adapter = installedAppItemAdapter
                        }
                        else {
                            fragmentBinding.recyclerView.adapter = null
                        }
                    }
                }.start()
                
                return true
            }
        })
        
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val installedApp = searchInstalledList[position]
        appDetails(searchActivity, installedApp.name, installedApp.packageName, null)
    }
    
    // On long click
    override fun onItemLongCLick(position: Int) {
        TODO("Not yet implemented")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}