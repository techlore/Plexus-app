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
import tech.techlore.plexus.adapters.PlexusDataItemAdapter
import tech.techlore.plexus.databinding.RecyclerViewBinding

class SearchDataFragment :
    Fragment(),
    PlexusDataItemAdapter.OnItemClickListener,
    PlexusDataItemAdapter.OnItemLongCLickListener {
    
    private var _binding: RecyclerViewBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var searchActivity: SearchActivity
    private lateinit var plexusDataItemAdapter: PlexusDataItemAdapter
    //private lateinit var searchDataList: ArrayList<PlexusData>
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
        //searchDataList = searchActivity.dataList
        //plexusDataItemAdapter = PlexusDataItemAdapter(searchDataList, this, this)
        
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
                            plexusDataItemAdapter.filter.filter(searchString)
                            fragmentBinding.recyclerView.adapter = plexusDataItemAdapter
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
        //val plexusData = searchDataList[position]
        //appDetails(searchActivity, plexusData.name, plexusData.packageName, null)
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