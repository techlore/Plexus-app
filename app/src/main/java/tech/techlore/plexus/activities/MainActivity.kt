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
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityMainBinding
import tech.techlore.plexus.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.bottomsheets.main.DeleteAccountBottomSheet
import tech.techlore.plexus.bottomsheets.main.NavViewBottomSheet
import tech.techlore.plexus.bottomsheets.main.SortBottomSheet
import tech.techlore.plexus.interfaces.NavViewItemSelectedListener
import tech.techlore.plexus.interfaces.SortPrefsChangedListener
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEF_VIEW
import tech.techlore.plexus.preferences.PreferenceManager.Companion.GRID_VIEW
import tech.techlore.plexus.utils.IntentUtils.Companion.startActivityWithTransition
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced

class MainActivity : AppCompatActivity(), NavViewItemSelectedListener, SortPrefsChangedListener {
    
    lateinit var activityBinding: ActivityMainBinding
    private lateinit var navController: NavController
    private val prefManager by inject<PreferenceManager>()
    private lateinit var navActionsMap: Map<Int, Int>
    private var defaultFragment = 0
    var defaultSelectedNavItem = 0
    var selectedNavItem = 0
    var isGridView = false
    private var job: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.apply {
            setNavBarContrastEnforced()
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        navController = navHostFragment.navController
        navActionsMap =
            mapOf(
                R.id.nav_plexus_data to R.id.action_global_to_plexusDataFragment,
                R.id.nav_installed_apps to R.id.action_global_to_installedAppsFragment,
                R.id.nav_fav to R.id.action_global_to_favoritesFragment,
                R.id.nav_my_ratings to R.id.action_global_to_myRatingsFragment,
                R.id.nav_settings to R.id.action_global_to_settingsFragment
            )
        
        isGridView = prefManager.getBoolean(GRID_VIEW, false)
        
        setDefaultView()
        
        // Set start destination as default view
        navController.navInflater.inflate(R.navigation.main_fragments_nav_graph).apply {
            setStartDestination(defaultFragment)
            navController.setGraph(this, intent.extras)
        }
        
        activityBinding.mainCollapsingToolbar.title = navController.currentDestination?.label
        
        // To set nav view item background, check selected item
        selectedNavItem = savedInstanceState?.getInt("selectedNavItem") ?: defaultSelectedNavItem
        
        // Disable menu buttons for settings fragment when activity is recreated
        // or else they'll be re-enabled when theme is changed
        if (selectedNavItem == R.id.nav_settings) setMenuButtonStates()
        
        // Make the docked toolbar clickable
        // or else clicks pass through it to the recycler view item beneath
        activityBinding.mainDockedToolbar.setOnClickListener {}
        
        // Nav view
        activityBinding.navViewBtn.setOnClickListener {
            NavViewBottomSheet(this).show(supportFragmentManager, "NavViewBottomSheet")
        }
        
        // Search
        activityBinding.mainSearchBtn.apply {
            tooltipText = getString(R.string.menu_search)
            setOnClickListener {
                startActivityWithTransition(Intent(this@MainActivity, SearchActivity::class.java))
            }
        }
        
        // Sort
        activityBinding.mainSortBtn.apply {
            tooltipText = getString(R.string.menu_sort)
            setOnClickListener {
                SortBottomSheet(
                    this@MainActivity,
                    navController.currentDestination?.id ?: 0
                ).show(supportFragmentManager, "SortBottomSheet")
            }
        }
        
        // View
        activityBinding.viewBtn.apply {
            tooltipText = getString(R.string.menu_view)
            setViewButtonIcon()
            setOnClickListener {
                isGridView = !isGridView
                prefManager.setBoolean(GRID_VIEW, isGridView)
                setViewButtonIcon()
                navController.refreshFragment()
            }
        }
        
        // Help
        activityBinding.mainHelpBtn.apply {
            tooltipText = getString(R.string.menu_help)
            setOnClickListener {
                HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
            }
        }
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
    
    // Setup fragments
    private fun displayFragment(selectedItem: Int) {
        val action = navActionsMap[selectedItem] ?: 0
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (selectedNavItem != selectedItem && action != 0) {
            selectedNavItem = selectedItem
            setMenuButtonStates()
            activityBinding.mainAppBar.setExpanded(true, true)
            navController.navigate(action)
            activityBinding.mainCollapsingToolbar.title = navController.currentDestination?.label
        }
    }
    
    private fun setMenuButtonStates() {
        when (selectedNavItem) {
            R.id.nav_my_ratings -> activityBinding.mainSearchBtn.isEnabled = false
            
            R.id.nav_settings -> {
                (1 until 5).forEach {
                    activityBinding.dockedToolbarConstraintLayout.getChildAt(it).isEnabled = false
                }
            }
            
            else -> {
                (1 until 5).forEach {
                    activityBinding.dockedToolbarConstraintLayout.getChildAt(it).isEnabled = true
                }
            }
        }
    }
    
    private fun MaterialButton.setViewButtonIcon() {
        icon =
            ContextCompat.getDrawable(
                this@MainActivity,
                if (!isGridView) R.drawable.ic_view_grid
                else R.drawable.ic_view_list
            )
    }
    
    override fun onNavViewItemSelected(selectedItemId: Int, shouldDelayAction: Boolean) {
        job?.cancel()
        job =
            lifecycleScope.launch {
                // Delay the action till the nav bottom sheet is (almost) hidden.
                // An ideal way to do this would be detecting STATE_HIDDEN from bottom sheet behavior,
                // but I wasn't able to make it work for modal bottom sheet...for now.
                if (shouldDelayAction) delay(265)
                if (selectedItemId != R.id.nav_delete_account) displayFragment(selectedItemId)
                else DeleteAccountBottomSheet().show(supportFragmentManager, "DeleteAccountBottomSheet")
            }
    }
    
    override fun onSortPrefsChanged() {
        activityBinding.mainAppBar.setExpanded(true, true)
        navController.refreshFragment()
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
                    displayFragment(defaultSelectedNavItem)
                }
                else -> finish()
            }
        }
    }
    
}