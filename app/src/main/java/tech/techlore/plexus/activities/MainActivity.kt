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
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityMainBinding
import tech.techlore.plexus.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.bottomsheets.main.NavViewBottomSheet
import tech.techlore.plexus.bottomsheets.main.SortBottomSheet
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEF_VIEW
import tech.techlore.plexus.utils.UiUtils.Companion.overrideTransition
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced

class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    lateinit var navController: NavController
    private val prefManager by inject<PreferenceManager>()
    private var defaultFragment = 0
    var defaultSelectedNavItem = 0
    var selectedNavItem = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.setNavBarContrastEnforced()
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        navController = navHostFragment.navController
        
        setDefaultView()
        
        // Set start destination as default view
        navController.navInflater.inflate(R.navigation.main_fragments_nav_graph).apply {
            setStartDestination(defaultFragment)
            navController.setGraph(this, intent.extras)
        }
        
        // Nav view
        activityBinding.navViewBtn.setOnClickListener {
            showNavView()
        }
        
        activityBinding.mainDockedToolbar.setOnClickListener {
            showNavView()
        }
        
        // Search
        activityBinding.mainSearchBtn.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            overrideTransition(enterAnim = R.anim.fade_in_slide_from_bottom,
                               exitAnim = R.anim.no_movement)
        }
        
        // Sort
        activityBinding.mainSortBtn.setOnClickListener {
            SortBottomSheet(navController).show(supportFragmentManager, "SortBottomSheet")
        }
        
        // Help
        activityBinding.mainHelpBtn.setOnClickListener {
            HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
        }
        
        activityBinding.mainBottomAppBarTitle.text = navController.currentDestination?.label
        
        // To set nav view item background, check selected item
        selectedNavItem = savedInstanceState?.getInt("selectedNavItem") ?: defaultSelectedNavItem
    }
    
    // Set default view
    // Theme bottom sheet was reused for this purpose
    // Don't get confused by the "R.id.followSystem" & "R.id.light"
    fun setDefaultView() {
        when (prefManager.getInt(DEF_VIEW, R.id.followSystem)) {
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
    }
    
    private fun showNavView() {
        NavViewBottomSheet().show(supportFragmentManager, "NavViewBottomSheet")
    }
    
    // Setup fragments
    fun displayFragment(selectedItem: Int) {
        val currentFragment = navController.currentDestination
        
        val actionsMap =
            mapOf(Pair(R.id.installedAppsFragment, R.id.nav_plexus_data) to R.id.action_installedAppsFragment_to_plexusDataFragment,
                  Pair(R.id.favoritesFragment, R.id.nav_plexus_data) to R.id.action_favoritesFragment_to_plexusDataFragment,
                  Pair(R.id.myRatingsFragment, R.id.nav_plexus_data) to R.id.action_myRatingsFragment_to_plexusDataFragment,
                  Pair(R.id.settingsFragment, R.id.nav_plexus_data) to R.id.action_settingsFragment_to_plexusDataFragment,
                  Pair(R.id.plexusDataFragment, R.id.nav_installed_apps) to R.id.action_plexusDataFragment_to_installedAppsFragment,
                  Pair(R.id.favoritesFragment, R.id.nav_installed_apps) to R.id.action_favoritesFragment_to_installedAppsFragment,
                  Pair(R.id.myRatingsFragment, R.id.nav_installed_apps) to R.id.action_myRatingsFragment_to_installedAppsFragment,
                  Pair(R.id.settingsFragment, R.id.nav_installed_apps) to R.id.action_settingsFragment_to_installedAppsFragment,
                  Pair(R.id.plexusDataFragment, R.id.nav_fav) to R.id.action_plexusDataFragment_to_favoritesFragment,
                  Pair(R.id.installedAppsFragment, R.id.nav_fav) to R.id.action_installedAppsFragment_to_favoritesFragment,
                  Pair(R.id.myRatingsFragment, R.id.nav_fav) to R.id.action_myRatingsFragment_to_favoritesFragment,
                  Pair(R.id.settingsFragment, R.id.nav_fav) to R.id.action_settingsFragment_to_favoritesFragment,
                  Pair(R.id.plexusDataFragment, R.id.nav_my_ratings) to R.id.action_plexusDataFragment_to_myRatingsFragment,
                  Pair(R.id.installedAppsFragment, R.id.nav_my_ratings) to R.id.action_installedAppsFragment_to_myRatingsFragment,
                  Pair(R.id.favoritesFragment, R.id.nav_my_ratings) to R.id.action_favoritesFragment_to_myRatingsFragment,
                  Pair(R.id.settingsFragment, R.id.nav_my_ratings) to R.id.action_settingsFragment_to_myRatingsFragment,
                  Pair(R.id.plexusDataFragment, R.id.nav_settings) to R.id.action_plexusDataFragment_to_settingsFragment,
                  Pair(R.id.installedAppsFragment, R.id.nav_settings) to R.id.action_installedAppsFragment_to_settingsFragment,
                  Pair(R.id.favoritesFragment, R.id.nav_settings) to R.id.action_favoritesFragment_to_settingsFragment,
                  Pair(R.id.myRatingsFragment, R.id.nav_settings) to R.id.action_myRatingsFragment_to_settingsFragment)
        
        val action = actionsMap[Pair(currentFragment?.id, selectedItem)] ?: 0
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (selectedItem != currentFragment?.id && action != 0) {
            when (selectedNavItem) {
                R.id.nav_my_ratings -> setMenuButtonStates(isSearchEnabled = false)
                R.id.nav_settings -> setMenuButtonStates(isSearchEnabled = false, isSortEnabled = false)
                else -> setMenuButtonStates()
            }
            navController.navigate(action)
        }
    }
    
    private fun setMenuButtonStates(isSearchEnabled: Boolean = true, isSortEnabled: Boolean = true) {
        activityBinding.mainSearchBtn.isEnabled = isSearchEnabled
        activityBinding.mainSortBtn.isEnabled = isSortEnabled
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedNavItem", selectedNavItem)
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when {
                navController.currentDestination?.id != defaultFragment -> {
                    selectedNavItem = defaultSelectedNavItem
                    displayFragment(selectedNavItem)
                    activityBinding.mainBottomAppBarTitle.text = navController.currentDestination?.label
                }
                else -> finish()
            }
        }
    }
}