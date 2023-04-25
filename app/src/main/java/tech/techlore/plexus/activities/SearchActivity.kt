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

package tech.techlore.plexus.activities

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import tech.techlore.plexus.R
import tech.techlore.plexus.adapters.main.MainDataItemAdapter
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivitySearchBinding
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity

class SearchActivity :
    AppCompatActivity(),
    MainDataItemAdapter.OnItemClickListener {
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mainDataItemAdapter: MainDataItemAdapter
    private lateinit var searchDataList: ArrayList<MainDataMinimal>
    private lateinit var miniRepository: MainDataMinimalRepository
    private var delayTimer: CountDownTimer? = null
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        miniRepository = (applicationContext as ApplicationManager).miniRepository
        searchDataList = ArrayList()
        
        /*###########################################################################################*/
        
        // Bottom toolbar as actionbar
        setSupportActionBar(activityBinding.searchToolbarBottom)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        activityBinding.searchToolbarBottom.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        // Perform search
        activityBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            
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
                            coroutineScope.launch {
                                searchDataList = miniRepository.searchFromDb(searchString)
                                withContext(Dispatchers.Main) {
                                    if (searchDataList.isEmpty()) {
                                        activityBinding.searchRv.adapter = null
                                        activityBinding.emptySearchView.visibility = View.VISIBLE
                                    }
                                    else {
                                        activityBinding.emptySearchView.visibility = View.GONE
                                        mainDataItemAdapter = MainDataItemAdapter(searchDataList,
                                                                                  this@SearchActivity,
                                                                                  coroutineScope)
                                        activityBinding.searchRv.adapter = mainDataItemAdapter
                                        FastScrollerBuilder(activityBinding.searchRv).useMd2Style().build() // Fast scroll
                                    }
                                }
                            }
                        }
                        else {
                            activityBinding.searchRv.adapter = null
                            activityBinding.emptySearchView.visibility = View.GONE
                        }
                    }
                }.start()
                
                return true
            }
        })
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val searchData = searchDataList[position]
        startDetailsActivity(this@SearchActivity, searchData.packageName)
    }
    
    override fun finish() {
        super.finish()
        coroutineScope.cancel()
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
    }
}