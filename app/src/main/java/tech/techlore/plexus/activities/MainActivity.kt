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

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityMainBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortBinding
import tech.techlore.plexus.databinding.BottomSheetThemeBinding
import tech.techlore.plexus.fragments.main.InstalledAppsFragment
import tech.techlore.plexus.fragments.main.PlexusDataFragment
import tech.techlore.plexus.models.InstalledApp
import tech.techlore.plexus.models.PlexusData
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.utils.IntentUtils.Companion.reloadFragment
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>
    private var checkedItem = 0
    private var clickedItem = 0 // To set nav view item background, check selected item
    private lateinit var fragment: Fragment
    private lateinit var toolbarTitle: String
    private lateinit var preferenceManager: PreferenceManager
    lateinit var dataList: ArrayList<PlexusData>
    lateinit var installedList: ArrayList<InstalledApp>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        preferenceManager = PreferenceManager(this)
        bottomSheetBehavior = BottomSheetBehavior.from(activityBinding.bottomNavContainer)
        
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.toolbarBottom)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // Get lists from previous activity
        if (Build.VERSION.SDK_INT >= 33) {
            dataList = intent.getParcelableArrayListExtra("plexusDataList", PlexusData::class.java)!!
            installedList = intent.getParcelableArrayListExtra("installedAppsList", InstalledApp::class.java)!!
        }
        else {
            dataList = intent.getParcelableArrayListExtra("plexusDataList")!!
            installedList = intent.getParcelableArrayListExtra("installedAppsList")!!
        }
        
        // Default fragment
        fragment = PlexusDataFragment()
        clickedItem = R.id.nav_plexus_data
        checkedItem = clickedItem
        toolbarTitle = getString(R.string.plexus_data)
        displayFragment(fragment, checkedItem)
        
        // Nav view items
        activityBinding.navView.setNavigationItemSelectedListener { navMenuItem: MenuItem ->
            
            when (navMenuItem.itemId) {
    
                R.id.nav_plexus_data -> {
                    fragment = PlexusDataFragment()
                    checkedItem = navMenuItem.itemId
                    toolbarTitle = getString(R.string.plexus_data)
                }
    
                R.id.nav_installed_apps -> {
                    fragment = InstalledAppsFragment()
                    checkedItem = navMenuItem.itemId
                    toolbarTitle = getString(R.string.installed_apps)
                }
    
                R.id.nav_fav -> {
                    checkedItem = navMenuItem.itemId
                }
                
            }
            
            clickedItem = navMenuItem.itemId
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED // Close nav view
            true
        }
        
        // Nav view bottom sheet
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                
                // Perform all onClick actions from nav view
                // after bottom sheet is collapsed
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    
                    when (clickedItem) {
    
                        R.id.nav_plexus_data, R.id.nav_installed_apps -> displayFragment(fragment, checkedItem)
                        
                        R.id.nav_report_issue -> openURL(this@MainActivity,
                                                         "https://github.com/techlore/Plexus-app/issues",
                                                         activityBinding.mainCoordinatorLayout,
                                                         activityBinding.bottomNavContainer)
    
                        R.id.nav_theme -> themeBottomSheet()
    
                        R.id.nav_about -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java)
                                                            .putExtra("frag", clickedItem))
                    }
                    
                    // Set to 0,
                    // otherwise if bottom sheet is dragged up and no item is clicked
                    // then on bottom sheet collapse, same action will be triggered again.
                    clickedItem = 0
                }
            }
            
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                
                activityBinding.dimBg.alpha = slideOffset * 2 // Dim background on sliding up
                activityBinding.navView.setCheckedItem(checkedItem) // Always sync checked item on slide
                
                // Hide toolbar title and menu on slide up
                if (slideOffset > 0.03) {
                    activityBinding.toolbarBottom.title = null
                    activityBinding.toolbarBottom.menu.clear()
                }
                else {
                    activityBinding.toolbarBottom.title = toolbarTitle
                    invalidateMenu()
                }
            }
        })
        
        // Nav view icon
        activityBinding.toolbarBottom.setNavigationOnClickListener {
            
            when (bottomSheetBehavior.state) {
                
                BottomSheetBehavior.STATE_COLLAPSED ->
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    
                BottomSheetBehavior.STATE_HALF_EXPANDED ->
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    
                BottomSheetBehavior.STATE_EXPANDED ->
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    
                BottomSheetBehavior.STATE_DRAGGING -> {}
                BottomSheetBehavior.STATE_HIDDEN -> {}
                BottomSheetBehavior.STATE_SETTLING -> {}
            }
        }
    }
    
    // Setup fragments
    private fun displayFragment(fragment: Fragment, checkedItem: Int) {
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.activity_host_fragment, fragment)
            .commitNow()
        activityBinding.navView.setCheckedItem(checkedItem)
        activityBinding.toolbarBottom.title = toolbarTitle
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        
        if (checkedItem == R.id.nav_installed_apps) {
            menu.findItem(R.id.menu_filter).isVisible = true
            if (preferenceManager.getInt(PreferenceManager.FILTER_PREF) == 0
                || preferenceManager.getInt(PreferenceManager.FILTER_PREF) == R.id.menu_all_apps) {
                menu.findItem(R.id.menu_all_apps).isChecked = true
            }
            else if (preferenceManager.getInt(PreferenceManager.FILTER_PREF) == R.id.menu_play_apps) {
                menu.findItem(R.id.menu_play_apps).isChecked = true
            }
            else {
                menu.findItem(R.id.menu_non_play_apps).isChecked = true
            }
        }
        else if (checkedItem == R.id.nav_about) {
            menu.findItem(R.id.menu_search).isVisible = false
            menu.findItem(R.id.menu_sort).isVisible = false
            menu.findItem(R.id.menu_filter).isVisible = false
        }
        else {
            menu.findItem(R.id.menu_search).isVisible = true
            menu.findItem(R.id.menu_sort).isVisible = true
            menu.findItem(R.id.menu_filter).isVisible = false
        }
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        
        // Search
        // Don't finish main activity,
        // Or else issues when getting list back from search activity
        when (item.itemId) {
            
            R.id.menu_search -> {
                val searchIntent = Intent(this, SearchActivity::class.java)
                searchIntent.putExtra("from", checkedItem)
    
                // If from Plexus Data, give Plexus Data list
                if (checkedItem == R.id.nav_plexus_data) {
                    searchIntent.putExtra("plexusDataList", checkedItem)
                }
                else {
                    searchIntent.putExtra("installedAppsList", checkedItem)
                }
                startActivity(searchIntent)
                overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement)
            }
    
            R.id.menu_sort -> sortBottomSheet()
    
            R.id.menu_all_apps, R.id.menu_play_apps, R.id.menu_non_play_apps -> {
                preferenceManager.setInt(PreferenceManager.FILTER_PREF, item.itemId)
                reloadFragment(supportFragmentManager, fragment)
            }
            
        }
        
        return true
    }
    
    private fun sortBottomSheet() {
        
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetSortBinding.inflate(layoutInflater)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        
        headerBinding.bottomSheetTitle.setText(R.string.menu_sort)
        
        // Default alphabetical checked chip
        if (preferenceManager.getInt(PreferenceManager.A_Z_SORT_PREF) == 0) {
            preferenceManager.setInt(PreferenceManager.A_Z_SORT_PREF, R.id.sort_a_z)
        }
        bottomSheetBinding.alphabeticalChipGroup.check(preferenceManager.getInt(PreferenceManager.A_Z_SORT_PREF))
        
        // Status radio btn checked by default
        if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == 0) {
            preferenceManager.setInt(PreferenceManager.STATUS_RADIO_PREF, R.id.radio_any_status)
        }
        bottomSheetBinding.statusRadiogroup.check(preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF))
        
        // Status chip group visibility
        if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == R.id.radio_dg_status) {
            bottomSheetBinding.statusChipGroup.visibility = View.VISIBLE
            
            // Default DG status checked chip
            if (preferenceManager.getInt(PreferenceManager.DG_STATUS_SORT_PREF) == 0) {
                preferenceManager.setInt(PreferenceManager.DG_STATUS_SORT_PREF,
                                            R.id.sort_not_tested)
            }
            bottomSheetBinding.statusChipGroup.check(preferenceManager.getInt(PreferenceManager.DG_STATUS_SORT_PREF))
        }
        else if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == R.id.radio_mg_status) {
            bottomSheetBinding.statusChipGroup.visibility = View.VISIBLE
            
            // Default MG status checked chip
            if (preferenceManager.getInt(PreferenceManager.MG_STATUS_SORT_PREF) == 0) {
                preferenceManager.setInt(PreferenceManager.MG_STATUS_SORT_PREF,
                                            R.id.sort_not_tested)
            }
            bottomSheetBinding.statusChipGroup.check(preferenceManager.getInt(PreferenceManager.MG_STATUS_SORT_PREF))
        }
        else {
            bottomSheetBinding.statusChipGroup.visibility = View.GONE
        }
        
        // On selecting status radio btn
        bottomSheetBinding.statusRadiogroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            if (checkedId != R.id.radio_any_status) {
                bottomSheetBinding.statusChipGroup.visibility = View.VISIBLE
            }
            else {
                bottomSheetBinding.statusChipGroup.visibility = View.GONE
            }
        }
        
        // Done
        footerBinding.positiveButton.text = getString(R.string.done)
        footerBinding.positiveButton.setOnClickListener {
            preferenceManager.setInt(PreferenceManager.A_Z_SORT_PREF,
                                        bottomSheetBinding.alphabeticalChipGroup.checkedChipId)
            preferenceManager.setInt(PreferenceManager.STATUS_RADIO_PREF,
                                        bottomSheetBinding.statusRadiogroup.checkedRadioButtonId)
            if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == R.id.radio_dg_status) {
                preferenceManager.setInt(PreferenceManager.DG_STATUS_SORT_PREF,
                                            bottomSheetBinding.statusChipGroup.checkedChipId)
            }
            else if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO_PREF) == R.id.radio_mg_status) {
                preferenceManager.setInt(PreferenceManager.MG_STATUS_SORT_PREF,
                                            bottomSheetBinding.statusChipGroup.checkedChipId)
            }
            bottomSheetDialog.dismiss()
            reloadFragment(supportFragmentManager, fragment)
        }
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener { bottomSheetDialog.cancel() }
        bottomSheetDialog.show()
    }
    
    private fun themeBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetThemeBinding.inflate(layoutInflater)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        
        headerBinding.bottomSheetTitle.setText(R.string.theme)
        
        // Default checked radio btn
        if (preferenceManager.getInt(PreferenceManager.THEME_PREF) == 0) {
            if (Build.VERSION.SDK_INT >= 29) {
                preferenceManager.setInt(PreferenceManager.THEME_PREF, R.id.sys_default)
            }
            else {
                preferenceManager.setInt(PreferenceManager.THEME_PREF, R.id.light)
            }
        }
        bottomSheetBinding.themeRadiogroup.check(preferenceManager.getInt(PreferenceManager.THEME_PREF))
        
        // Show system default option only on SDK 29 and above
        if (Build.VERSION.SDK_INT >= 29) {
            bottomSheetBinding.sysDefault.visibility = View.VISIBLE
        }
        else {
            bottomSheetBinding.sysDefault.visibility = View.GONE
        }
        
        // On selecting option
        bottomSheetBinding.themeRadiogroup
            .setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
                
                when (checkedId) {
    
                    R.id.sys_default ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    
                    R.id.light ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    
                    R.id.dark ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    
                }
                
                preferenceManager.setInt(PreferenceManager.THEME_PREF, checkedId)
                bottomSheetDialog.dismiss()
                recreate()
            }
        footerBinding.positiveButton.visibility = View.GONE
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener { bottomSheetDialog.cancel() }
        bottomSheetDialog.show()
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
            else if (checkedItem != R.id.nav_plexus_data) {
                fragment = PlexusDataFragment()
                clickedItem = R.id.nav_plexus_data
                checkedItem = clickedItem
                toolbarTitle = getString(R.string.plexus_data)
                displayFragment(fragment, checkedItem)
            }
            else {
                finish()
            }
        }
    }
}