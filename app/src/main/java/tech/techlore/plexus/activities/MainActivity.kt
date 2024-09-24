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

package tech.techlore.plexus.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.color.MaterialColors
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivityMainBinding
import tech.techlore.plexus.fragments.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.main.DeleteAccountBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.main.SortBottomSheet
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.IS_REGISTERED
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEF_VIEW

class MainActivity : AppCompatActivity(), MenuProvider {
    
    lateinit var activityBinding: ActivityMainBinding
    lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    private var defaultFragment = 0
    var defaultSelectedNavItem = 0
    var clickedNavItem = 0
    var selectedNavItem = 0
    private var surfaceContainerLowColor = 0
    private var surfaceContainerColor = 0
    
    private companion object {
        const val BOTTOM_NAV_SLIDE_UP_THRESHOLD = 0.02
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        addMenuProvider(this)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        bottomSheetBehavior = BottomSheetBehavior.from(activityBinding.bottomNavContainer)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.main_fragments_nav_graph)
        val navMyAccountItems = listOf(R.id.nav_my_ratings, R.id.nav_delete_account)
        val encPreferenceManager = EncryptedPreferenceManager(this)
        
        // Set default view
        // Theme bottom sheet was reused for this purpose
        // Don't get confused by the "R.id.followSystem" & "R.id.light"
        when ((applicationContext as ApplicationManager).preferenceManager.getInt(DEF_VIEW, R.id.followSystem)) {
            R.id.followSystem -> {
                defaultFragment = R.id.plexusDataFragment
                defaultSelectedNavItem = R.id.nav_plexus_data
            }
            R.id.light -> {
                defaultFragment = R.id.installedAppsFragment
                defaultSelectedNavItem = R.id.nav_installed_apps
            }
            else -> {
                defaultFragment = R.id.favoritesFragment
                defaultSelectedNavItem = R.id.nav_fav
            }
        }
        
        // Set start destination as default view
        navGraph.setStartDestination(defaultFragment)
        navController.setGraph(navGraph, intent.extras)
        
        activityBinding.toolbarBottom.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            title = navController.currentDestination?.label.toString()
        }
        
        // To set nav view item background, check selected item
        selectedNavItem = savedInstanceState?.getInt("selectedNavItem") ?: defaultSelectedNavItem
        
        // Nav view items
        activityBinding.navView.setNavigationItemSelectedListener { navMenuItem: MenuItem ->
            
            when (navMenuItem.itemId) {
                R.id.nav_plexus_data, R.id.nav_installed_apps, R.id.nav_fav, R.id.nav_my_ratings ->
                    selectedNavItem = navMenuItem.itemId
            }
            
            clickedNavItem = navMenuItem.itemId
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED // Close nav view
            true
        }
        
        // Nav view bottom sheet
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                
                // Set background clickable,
                // only when bottom sheet is not collapsed
                activityBinding.scrimBg.isClickable = newState != BottomSheetBehavior.STATE_COLLAPSED
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    activityBinding.toolbarBottom.navigationIcon = null
                }
                else {
                    activityBinding.toolbarBottom.navigationIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_nav_view)
                }
                
                /*if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    animateStatusBarColor(surfaceContainerLowColor, true)
                }
                else {
                    animateStatusBarColor(Color.TRANSPARENT, false)
                }*/
                
                // Perform all onClick actions from nav view
                // after bottom sheet is collapsed
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    when (clickedNavItem) {
                        R.id.nav_plexus_data, R.id.nav_installed_apps, R.id.nav_fav, R.id.nav_my_ratings -> {
                            displayFragment(clickedNavItem)
                            activityBinding.toolbarBottom.title = navController.currentDestination?.label.toString()
                        }
                        R.id.nav_delete_account -> DeleteAccountBottomSheet().show(supportFragmentManager, "DeleteAccountBottomSheet")
                        R.id.nav_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    }
                    
                    // Set to 0,
                    // otherwise if bottom sheet is dragged up and no item is clicked
                    // then on bottom sheet collapse, same action will be triggered again.
                    clickedNavItem = 0
                }
            }
            
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                
                activityBinding.scrimBg.alpha = slideOffset * 2f // Dim background on sliding up
                
                activityBinding.navView.apply {
                    navMyAccountItems.forEach {
                        menu.findItem(it).isVisible =
                            encPreferenceManager.getBoolean(IS_REGISTERED)
                    }
                    setCheckedItem(selectedNavItem) // Always sync selected item on slide
                }
                
                // Hide toolbar title and menu on slide up
                if (slideOffset > BOTTOM_NAV_SLIDE_UP_THRESHOLD) {
                    updateUiOnSlide(true)
                    activityBinding.navView.isVisible = true
                }
                else {
                    updateUiOnSlide(false)
                    activityBinding.navView.isVisible = false
                }
                
                // Collapse nav view on clicking background (scrim)
                // just like modal bottom sheets & dialogs
                activityBinding.scrimBg.setOnClickListener{
                    if ( bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED ) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }
        })
        
        // Nav view icon
        activityBinding.toolbarBottom.setNavigationOnClickListener {
            bottomSheetBehavior.state =
                when (bottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_HALF_EXPANDED
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> BottomSheetBehavior.STATE_EXPANDED
                    else -> 0
                }
        }
    }
    
    // Setup fragments
    fun displayFragment(clickedItem: Int) {
        val currentFragment = navController.currentDestination
        
        val actionsMap =
            mapOf(Pair(R.id.installedAppsFragment, R.id.nav_plexus_data) to R.id.action_installedAppsFragment_to_plexusDataFragment,
                  Pair(R.id.favoritesFragment, R.id.nav_plexus_data) to R.id.action_favoritesFragment_to_plexusDataFragment,
                  Pair(R.id.myRatingsFragment, R.id.nav_plexus_data) to R.id.action_myRatingsFragment_to_plexusDataFragment,
                  Pair(R.id.plexusDataFragment, R.id.nav_fav) to R.id.action_plexusDataFragment_to_favoritesFragment,
                  Pair(R.id.installedAppsFragment, R.id.nav_fav) to R.id.action_installedAppsFragment_to_favoritesFragment,
                  Pair(R.id.myRatingsFragment, R.id.nav_fav) to R.id.action_myRatingsFragment_to_favoritesFragment,
                  Pair(R.id.plexusDataFragment, R.id.nav_installed_apps) to R.id.action_plexusDataFragment_to_installedAppsFragment,
                  Pair(R.id.favoritesFragment, R.id.nav_installed_apps) to R.id.action_favoritesFragment_to_installedAppsFragment,
                  Pair(R.id.myRatingsFragment, R.id.nav_installed_apps) to R.id.action_myRatingsFragment_to_installedAppsFragment,
                  Pair(R.id.plexusDataFragment, R.id.nav_my_ratings) to R.id.action_plexusDataFragment_to_myRatingsFragment,
                  Pair(R.id.installedAppsFragment, R.id.nav_my_ratings) to R.id.action_installedAppsFragment_to_myRatingsFragment,
                  Pair(R.id.favoritesFragment, R.id.nav_my_ratings) to R.id.action_favoritesFragment_to_myRatingsFragment)
        
        val action = actionsMap[Pair(currentFragment?.id, clickedItem)] ?: 0
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (clickedItem != currentFragment?.id && action != 0) {
            activityBinding.navView.setCheckedItem(selectedNavItem)
            navController.navigate(action)
        }
    }
    
    private fun updateUiOnSlide(thresholdCrossed: Boolean) {
        if (thresholdCrossed) {
            activityBinding.apply {
                toolbarBottom.apply {
                    title = null
                    menu.clear()
                }
                bottomNavContainer.apply {
                    surfaceContainerLowColor =
                        surfaceContainerLowColor.takeIf { it != 0 } ?: MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainer)
                    backgroundTintList = ColorStateList.valueOf(surfaceContainerLowColor)
                }
            }
        }
        else {
            activityBinding.apply {
                toolbarBottom.apply {
                    title = navController.currentDestination!!.label.toString()
                }
                invalidateMenu()
                bottomNavContainer.apply {
                    surfaceContainerColor =
                        surfaceContainerColor.takeIf { it != 0 } ?: MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainer)
                    backgroundTintList = ColorStateList.valueOf(surfaceContainerColor)
                }
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedNavItem", selectedNavItem)
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        
        menu.findItem(R.id.menu_search).isVisible = selectedNavItem != R.id.nav_my_ratings
        
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        
        when (menuItem.itemId) {
            R.id.menu_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement)
            }
            R.id.menu_sort -> SortBottomSheet(navController).show(supportFragmentManager, "SortBottomSheet")
            R.id.main_menu_help -> HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
        }
        
        return true
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            
            when {
                bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED ->
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                
                navController.currentDestination?.id != defaultFragment -> {
                    clickedNavItem = defaultSelectedNavItem
                    selectedNavItem = clickedNavItem
                    displayFragment(clickedNavItem)
                    activityBinding.toolbarBottom.title = navController.currentDestination?.label.toString()
                }
                
                else -> finish()
            }
        }
    }
}