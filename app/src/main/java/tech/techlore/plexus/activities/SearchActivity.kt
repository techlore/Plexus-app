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

package tech.techlore.plexus.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityMainBinding
import tech.techlore.plexus.databinding.SearchViewBinding
import tech.techlore.plexus.fragments.search.SearchDataFragment
import tech.techlore.plexus.fragments.search.SearchInstalledFragment
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.models.PlexusData

class SearchActivity : AppCompatActivity() {
    
    lateinit var dataList: ArrayList<PlexusData>
    lateinit var installedList: ArrayList<InstalledApp>
    lateinit var searchViewBinding: SearchViewBinding
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        searchViewBinding = SearchViewBinding.bind(activityBinding.searchViewStub.inflate())
        
        /*###########################################################################################*/
        
        // Bottom toolbar as actionbar
        setSupportActionBar(activityBinding.toolbarBottom)
        activityBinding.toolbarBottom.setNavigationIcon(R.drawable.ic_back)
        activityBinding.toolbarBottom.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        // Nav view bottom sheet
        BottomSheetBehavior.from(activityBinding.bottomNavContainer).isDraggable = false
        
        // Default fragment
        if (savedInstanceState == null) {
            
            if (intent.extras!!.getInt("from") == R.id.nav_plexus_data) {
                
                /*dataList =
                    if (Build.VERSION.SDK_INT >= 33) {
                        intent.getParcelableArrayListExtra("plexusDataList", PlexusData::class.java)!!
                    }
                    else {
                        intent.getParcelableArrayListExtra("plexusDataList")!!
                    }*/
                
                displayFragment("Search Data")
            }
            else {
    
                /*installedList =
                    if (Build.VERSION.SDK_INT >= 33) {
                        intent.getParcelableArrayListExtra("installedAppsList", InstalledApp::class.java)!!
                    }
                    else {
                        intent.getParcelableArrayListExtra("installedAppsList")!!
                    }*/
                displayFragment("Search Installed")
            }
        }
    }
    
    // SETUP FRAGMENTS
    private fun displayFragment(fragmentName: String) {
        val fragment: Fragment
        val transaction = supportFragmentManager.beginTransaction()
        
        if (fragmentName == "Search Data") {
            searchViewBinding.searchView.queryHint = "${resources.getString(R.string.menu_search)} ${resources.getString(R.string.plexus_data)}"
            fragment = SearchDataFragment()
        }
        else {
            searchViewBinding.searchView.queryHint = "${resources.getString(R.string.menu_search)} ${resources.getString(R.string.installed_apps)}"
            fragment = SearchInstalledFragment()
        }
        
        transaction.replace(R.id.activity_host_fragment, fragment).commit()
    }
    
    // SET TRANSITION WHEN FINISHING ACTIVITY
    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
    }
}