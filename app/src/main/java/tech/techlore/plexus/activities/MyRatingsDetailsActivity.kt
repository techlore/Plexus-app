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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.fragments.bottomsheets.MoreOptionsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.SortMyRatingsDetailsBottomSheet
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromTextViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString

class MyRatingsDetailsActivity : AppCompatActivity(), MenuProvider {
    
    private lateinit var activityBinding: ActivityAppDetailsBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var app: MainData
    private lateinit var packageNameString: String
    lateinit var myRating: MyRating
    var differentVersionsList = listOf<String>()
    var differentRomsList = listOf<String>()
    var differentAndroidsList = listOf<String>()
    lateinit var selectedVersionString: String
    lateinit var selectedRomString: String
    lateinit var selectedAndroidString: String
    var installedFromChip = R.id.ratingsChipInstalledAny
    var statusRadio = R.id.ratingsRadioAnyStatus
    var dgStatusSort = 0
    var mgStatusSort = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        addMenuProvider(this)
        activityBinding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        preferenceManager = PreferenceManager(this)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.detailsNavHost) as NavHostFragment
        navController = navHostFragment.navController
        packageNameString = intent.getStringExtra("packageName")!!
        selectedVersionString = getString(R.string.any)
        selectedRomString = getString(R.string.any)
        selectedAndroidString = getString(R.string.any)
        val appManager = applicationContext as ApplicationManager
        val mainRepository = appManager.mainRepository
        val myRatingsRepository = appManager.myRatingsRepository
        
        activityBinding.bottomAppBar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        
        lifecycleScope.launch {
            app = mainRepository.getAppByPackage(packageNameString)!!
            myRating = myRatingsRepository.getMyRatingsByPackage(packageNameString)!!
            
            displayAppIcon(context = this@MyRatingsDetailsActivity,
                           imageView = activityBinding.detailsAppIcon,
                           isInstalled = app.isInstalled,
                           packageName = app.packageName,
                           iconUrl = app.iconUrl)
            
            activityBinding.detailsName.text = app.name
            activityBinding.detailsPackageName.text = app.packageName
            
            activityBinding.detailsInstalledVersion.text =
                if (app.installedVersion.isEmpty()) {
                    "${getString(R.string.installed)}: ${getString(R.string.na)}"
                }
                else {
                    "${getString(R.string.installed)}: ${app.installedVersion} (${app.installedBuild})"
                }
            
            setInstalledFromTextViewStyle(this@MyRatingsDetailsActivity,
                                          app.installedFrom,
                                          activityBinding.detailsInstalledFrom)
            
            activityBinding.bottomAppBarRadio.isVisible = false
            
            navController.navigate(R.id.myRatingsDetailsFragment)
            
            activityBinding.fab.isVisible = false
        }
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_activity_details, menu)
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            
            R.id.details_menu_help -> startActivity(Intent(this@MyRatingsDetailsActivity, SettingsActivity::class.java)
                                                        .putExtra("frag", R.id.helpTextFragment))
            
            R.id.menu_sort_user_ratings -> SortMyRatingsDetailsBottomSheet().show(supportFragmentManager, "SortMyRatingsBottomSheet")
            
            R.id.menu_more ->
                MoreOptionsBottomSheet(app.name, packageNameString,
                                       mapScoreRangeToStatusString(this@MyRatingsDetailsActivity, app.dgScore),
                                       mapScoreRangeToStatusString(this@MyRatingsDetailsActivity, app.mgScore),
                                       activityBinding.detailsCoordLayout,
                                       activityBinding.bottomAppBarRadio)
                    .show(supportFragmentManager, "MoreOptionsBottomSheet")
            
        }
        
        return true
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
}