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
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityMainBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortBinding
import tech.techlore.plexus.databinding.BottomSheetThemeBinding
import tech.techlore.plexus.models.MainData
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.SEL_ITEM
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL
import tech.techlore.plexus.utils.IntentUtils.Companion.refreshFragment

class MainActivity : AppCompatActivity(), MenuProvider {
    
    lateinit var activityBinding: ActivityMainBinding
    lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>
    private var clickedItem = 0
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    lateinit var dataList: ArrayList<MainData>
    lateinit var installedList: ArrayList<MainData>
    lateinit var favList: ArrayList<MainData>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        addMenuProvider(this)
        
        preferenceManager = PreferenceManager(this)
        bottomSheetBehavior = BottomSheetBehavior.from(activityBinding.bottomNavContainer)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
    
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.toolbarBottom)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        activityBinding.toolbarBottom.title = navController.currentDestination!!.label.toString()
        
        // Get lists from previous activity
        if (Build.VERSION.SDK_INT >= 33) {
            dataList = intent.getParcelableArrayListExtra("plexusDataList", MainData::class.java)!!
            installedList = intent.getParcelableArrayListExtra("installedAppsList", MainData::class.java)!!
            favList = intent.getParcelableArrayListExtra("favList", MainData::class.java)!!
        }
        else {
            dataList = intent.getParcelableArrayListExtra("plexusDataList")!!
            installedList = intent.getParcelableArrayListExtra("installedAppsList")!!
            favList = intent.getParcelableArrayListExtra("favList")!!
        }
        
        // To set nav view item background, check selected item
        if (savedInstanceState == null) {
            preferenceManager.setInt(SEL_ITEM, R.id.nav_plexus_data)
        }
        
        // Nav view items
        activityBinding.navView.setNavigationItemSelectedListener { navMenuItem: MenuItem ->
            
            when (navMenuItem.itemId) {
    
                R.id.nav_plexus_data -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.nav_plexus_data)
                }
    
                R.id.nav_installed_apps -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.nav_installed_apps)
                }
    
                R.id.nav_fav -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.nav_fav)
                }
                
            }
            
            clickedItem = navMenuItem.itemId
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED // Close nav view
            true
        }
        
        // Nav view bottom sheet
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            
                // Set background clickable,
                // only when bottom sheet is not collapsed
                activityBinding.dimBg.isClickable = newState != BottomSheetBehavior.STATE_COLLAPSED
            
                // Perform all onClick actions from nav view
                // after bottom sheet is collapsed
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                
                    when (clickedItem) {
                    
                        R.id.nav_plexus_data, R.id.nav_installed_apps, R.id.nav_fav -> {
                            displayFragment(clickedItem)
                            activityBinding.toolbarBottom.title =
                                navController.currentDestination !!.label.toString()
                        }
                    
                        R.id.nav_report_issue -> openURL(this@MainActivity,
                                                         "https://github.com/techlore/Plexus-app/issues",
                                                         activityBinding.mainCoordinatorLayout,
                                                         activityBinding.bottomNavContainer)
                    
                        R.id.nav_theme -> themeBottomSheet()
                    
                        R.id.nav_about -> startActivity(Intent(this@MainActivity,
                                                               SettingsActivity::class.java)
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
                activityBinding.navView.setCheckedItem(preferenceManager.getInt(SEL_ITEM)) // Always sync checked item on slide
                
                // Hide toolbar title and menu on slide up
                if (slideOffset > 0.03) {
                    activityBinding.toolbarBottom.title = null
                    activityBinding.toolbarBottom.menu.clear()
                }
                else {
                    activityBinding.toolbarBottom.title = navController.currentDestination!!.label.toString()
                    invalidateMenu()
                }
    
                // Collapse nav view on clicking background
                // just like dialogs and bottom sheets
                activityBinding.dimBg.setOnClickListener{
                    if ( bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED ) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
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
    fun displayFragment(clickedItem: Int) {
        val currentFragment = navController.currentDestination!!
    
        val action: Int =
            when (clickedItem) {
            
                R.id.nav_plexus_data ->
                    when (currentFragment.id) {
                        R.id.installedAppsFragment -> R.id.action_installedAppsFragment_to_plexusDataFragment
                        R.id.favoritesFragment -> R.id.action_favoritesFragment_to_plexusDataFragment
                        else -> 0
                    }
            
                R.id.nav_installed_apps ->
                    when (currentFragment.id) {
                        R.id.plexusDataFragment -> R.id.action_plexusDataFragment_to_installedAppsFragment
                        R.id.favoritesFragment -> R.id.action_favoritesFragment_to_installedAppsFragment
                        else -> 0
                    }
            
                R.id.nav_fav ->
                    when (currentFragment.id) {
                        R.id.plexusDataFragment -> R.id.action_plexusDataFragment_to_favoritesFragment
                        R.id.installedAppsFragment -> R.id.action_installedAppsFragment_to_favoritesFragment
                        else -> 0
                    }
            
                else -> 0
            }
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (clickedItem != currentFragment.id && action != 0) {
            activityBinding.navView.setCheckedItem(preferenceManager.getInt(SEL_ITEM))
            navController.navigate(action)
        }
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        
        if (preferenceManager.getInt(SEL_ITEM) == R.id.nav_installed_apps) {
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
        menu.findItem(R.id.menu_filter).isVisible = preferenceManager.getInt(SEL_ITEM) != R.id.nav_plexus_data
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        
        // Search
        // Don't finish main activity,
        // Or else issues when getting list back from search activity
        when (menuItem.itemId) {
            
            /*R.id.menu_search -> {
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
            }*/
    
            R.id.menu_sort -> sortBottomSheet()
    
            R.id.menu_all_apps,
            R.id.menu_play_apps,
            R.id.menu_non_play_apps -> {
                preferenceManager.setInt(PreferenceManager.FILTER_PREF, menuItem.itemId)
                refreshFragment(navController)
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
            refreshFragment(navController)
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
        footerBinding.negativeButton.setOnClickListener {
            bottomSheetDialog.cancel()
        }
        bottomSheetDialog.show()
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            else if (navController.currentDestination!!.id != navController.graph.startDestinationId) {
                clickedItem = R.id.nav_plexus_data
                preferenceManager.setInt(SEL_ITEM, R.id.nav_plexus_data)
                displayFragment(clickedItem)
                activityBinding.toolbarBottom.title = navController.currentDestination!!.label.toString()
            }
            else {
                finish()
            }
        }
    }
}