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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityMainBinding
import tech.techlore.plexus.fragments.bottomsheets.SortBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.SortMyRatingsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.ThemeBottomSheet
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.SEL_ITEM
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class MainActivity : AppCompatActivity(), MenuProvider {
    
    lateinit var activityBinding: ActivityMainBinding
    lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>
    var clickedItem = 0
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        addMenuProvider(this)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        preferenceManager = PreferenceManager(this)
        bottomSheetBehavior = BottomSheetBehavior.from(activityBinding.bottomNavContainer)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.toolbarBottom)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        activityBinding.toolbarBottom.title = navController.currentDestination!!.label.toString()
        
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
                
                R.id.nav_fav -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.nav_fav)
                }
    
                R.id.nav_submit_rating -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.nav_submit_rating)
                }
                
                R.id.nav_my_ratings -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.nav_my_ratings)
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
                        
                        R.id.nav_plexus_data, R.id.nav_fav, R.id.nav_submit_rating, R.id.nav_my_ratings -> {
                            displayFragment(clickedItem)
                            activityBinding.toolbarBottom.title =
                                navController.currentDestination !!.label.toString()
                        }
                        
                        R.id.nav_report_issue -> openURL(this@MainActivity,
                                                         "https://github.com/techlore/Plexus-app/issues",
                                                         activityBinding.mainCoordinatorLayout,
                                                         activityBinding.bottomNavContainer)
                        
                        R.id.nav_theme -> ThemeBottomSheet().show(supportFragmentManager, "ThemeBottomSheet")
                        
                        R.id.nav_about -> startActivity(Intent(this@MainActivity,
                                                               SettingsActivity::class.java)
                                                            .putExtra("frag", R.id.aboutFragment))
                    }
                    
                    
                    // Set to 0,
                    // otherwise if bottom sheet is dragged up and no item is clicked
                    // then on bottom sheet collapse, same action will be triggered again.
                    clickedItem = 0
                }
            }
            
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                
                activityBinding.dimBg.alpha = slideOffset * 2f // Dim background on sliding up
                activityBinding.navView.setCheckedItem(preferenceManager.getInt(SEL_ITEM)) // Always sync checked item on slide
                
                // Hide toolbar title and menu on slide up
                if (slideOffset > 0.02) {
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
                        R.id.submitRatingFragment -> R.id.action_submitRatingFragment_to_plexusDataFragment
                        R.id.favoritesFragment -> R.id.action_favoritesFragment_to_plexusDataFragment
                        R.id.myRatingsFragment -> R.id.action_myRatingsFragment_to_plexusDataFragment
                        else -> 0
                    }
                
                R.id.nav_fav ->
                    when (currentFragment.id) {
                        R.id.plexusDataFragment -> R.id.action_plexusDataFragment_to_favoritesFragment
                        R.id.submitRatingFragment -> R.id.action_submitRatingFragment_to_favoritesFragment
                        R.id.myRatingsFragment -> R.id.action_myRatingsFragment_to_favoritesFragment
                        else -> 0
                    }
    
                R.id.nav_submit_rating ->
                    when (currentFragment.id) {
                        R.id.plexusDataFragment -> R.id.action_plexusDataFragment_to_installedAppsFragment
                        R.id.favoritesFragment -> R.id.action_favoritesFragment_to_installedAppsFragment
                        R.id.myRatingsFragment -> R.id.action_myRatingsFragment_to_installedAppsFragment
                        else -> 0
                    }
                
                R.id.nav_my_ratings ->
                    when(currentFragment.id) {
                        R.id.plexusDataFragment -> R.id.action_plexusDataFragment_to_myRatingsFragment
                        R.id.submitRatingFragment -> R.id.action_submitRatingFragment_to_myRatingsFragment
                        R.id.favoritesFragment -> R.id.action_favoritesFragment_to_myRatingsFragment
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
        
        menu.findItem(R.id.menu_search).isVisible = preferenceManager.getInt(SEL_ITEM) != R.id.nav_my_ratings
        
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        
        when (menuItem.itemId) {
            
            R.id.menu_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement)
            }
            
            R.id.menu_sort -> {
                if (navController.currentDestination!!.id == R.id.myRatingsFragment) {
                    SortMyRatingsBottomSheet(navController).show(supportFragmentManager, "SortMyRatingsBottomSheet")
                }
                else {
                    SortBottomSheet(navController).show(supportFragmentManager, "SortBottomSheet")
                }
            }
            
            R.id.main_menu_help -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java)
                                                .putExtra("frag", R.id.helpFragment))
            
        }
        
        return true
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            
            when {
                
                bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED ->
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                
                navController.currentDestination!!.id != navController.graph.startDestinationId -> {
                    clickedItem = R.id.nav_plexus_data
                    preferenceManager.setInt(SEL_ITEM, R.id.nav_plexus_data)
                    displayFragment(clickedItem)
                    activityBinding.toolbarBottom.title = navController.currentDestination!!.label.toString()
                }
                
                else -> finish()
            }
        }
    }
}