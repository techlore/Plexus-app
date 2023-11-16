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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.fragments.bottomsheets.RomSelectionBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.MoreOptionsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.NoNetworkBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.SortUserRatingsBottomSheet
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.IS_REGISTERED
import tech.techlore.plexus.utils.IntentUtils.Companion.startSubmitActivity
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromTextViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class AppDetailsActivity : AppCompatActivity(), MenuProvider {
    
    private lateinit var activityBinding: ActivityAppDetailsBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    lateinit var app: MainData
    var ratingsList = ArrayList<Rating>()
    private var ratingsRetrieved = false
    var totalScoreCalculated = false
    var dgGoldRatingsPercent = 0.0f
    var dgSilverRatingsPercent = 0.0f
    var dgBronzeRatingsPercent = 0.0f
    var dgBrokenRatingsPercent = 0.0f
    var mgGoldRatingsPercent = 0.0f
    var mgSilverRatingsPercent = 0.0f
    var mgBronzeRatingsPercent = 0.0f
    var mgBrokenRatingsPercent = 0.0f
    var sortedRatingsList = ArrayList<Rating>()
    var listIsSorted = false
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
        
        val encPreferenceManager = EncryptedPreferenceManager(this)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.detailsNavHost) as NavHostFragment
        navController = navHostFragment.navController
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
            app = mainRepository.getAppByPackage(intent.getStringExtra("packageName")!!)!!
            
            displayAppIcon(context = this@AppDetailsActivity,
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
            
            setInstalledFromTextViewStyle(this@AppDetailsActivity,
                                          app.installedFrom,
                                          activityBinding.detailsInstalledFrom)
            
            // Radio group/buttons
            activityBinding.detailsRadioGroup.setOnCheckedChangeListener{_, checkedId: Int ->
                activityBinding.nestedScrollView.apply {
                    if (scrollX != 0 || scrollY != 0) {
                        post {
                            scrollTo(0, 0)
                        }
                    }
                }
                displayFragment(checkedId)
            }
            
            val myRatingExists =
                myRatingsRepository.getMyRatingsByPackage(app.packageName)?.ratingsDetails?.any {
                    it.version == app.installedVersion
                } == true
            
            // FAB
            activityBinding.fab.setOnClickListener {
                when {
                    !app.isInstalled ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.install_app_to_submit, app.name),
                                     activityBinding.bottomAppBarRadio)
                    
                    !appManager.deviceIsDeGoogled || !appManager.deviceIsMicroG ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.device_should_be_degoogled_or_microg),
                                     activityBinding.bottomAppBarRadio)
                    
                    encPreferenceManager.getString(DEVICE_ROM).isNullOrEmpty() ->
                        RomSelectionBottomSheet().show(supportFragmentManager, "RomSelectionBottomSheet")
                    
                    !encPreferenceManager.getBoolean(IS_REGISTERED) -> {
                        startActivity(Intent(this@AppDetailsActivity,
                                             VerificationActivity::class.java)
                                          .putExtra("name", app.name)
                                          .putExtra("packageName", app.packageName)
                                          .putExtra("installedVersion", app.installedVersion)
                                          .putExtra("installedBuild", app.installedBuild)
                                          .putExtra("installedFrom", app.installedFrom)
                                          .putExtra("isInPlexusData", app.isInPlexusData))
                        finish()
                    }
                    
                    myRatingExists ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.rating_already_submitted, app.name, app.installedVersion),
                                     activityBinding.bottomAppBarRadio)
                    
                    else -> {
                        startSubmitActivity(this@AppDetailsActivity,
                                            app.name,
                                            app.packageName,
                                            app.installedVersion,
                                            app.installedBuild,
                                            app.installedFrom,
                                            app.isInPlexusData)
                        finish()
                    }
                }
            }
            
            // Only retrieve ratings list if not done already
            if (!ratingsRetrieved) {
                retrieveRatings()
            }
        }
        
    }
    
    private fun retrieveRatings() {
        
        lifecycleScope.launch {
            if (hasNetwork(this@AppDetailsActivity) && hasInternet()) {
                val apiRepository = (applicationContext as ApplicationManager).apiRepository
                val ratingsCall = apiRepository.getRatings(packageName = app.packageName, pageNumber = 1)
                val ratingsResponse = ratingsCall.awaitResponse()
                
                if (ratingsResponse.isSuccessful) {
                    ratingsResponse.body()?.let { ratingsRoot ->
                        ratingsList = ratingsRoot.ratingsData
                        // No need to store the ratings list in database, as:
                        // 1. It's not used anywhere else, except details activity
                        // 2. We're already retrieving the latest ratings everytime in details activity
                        
                        // Retrieve remaining ratings in parallel
                        if (ratingsRoot.meta.totalPages > 1) {
                            val requests = mutableListOf<Deferred<Unit>>()
                            for (pageNumber in 2..ratingsRoot.meta.totalPages) {
                                val request = async {
                                    val remRatingsCall = apiRepository.getRatings(app.packageName, pageNumber)
                                    val remRatingsResponse = remRatingsCall.awaitResponse()
                                    if (remRatingsResponse.isSuccessful) {
                                        remRatingsResponse.body()?.let { root ->
                                            ratingsList = root.ratingsData
                                        }
                                    }
                                }
                                requests.add(request)
                            }
                            // Wait for all requests to complete
                            requests.awaitAll()
                        }
                    }
                }
                
                ratingsRetrieved = true
                displayFragment(10)
                activityBinding.detailsRadioGroup.isVisible = true
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveButtonClickListener = { retrieveRatings() },
                                     negativeButtonClickListener = { finish() })
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    // Setup fragments
    private fun displayFragment(checkedItem: Int) {
        val action =
            when (checkedItem) {
                
                10 -> R.id.action_fragmentProgressView_to_totalScoreFragment
                // 10 is just a custom number
                // to let the nav controller navigate from
                // progress view fragment to total score fragment
                
                R.id.radioTotalScore -> R.id.action_userRatingsFragment_to_totalScoreFragment
                
                R.id.radioUserRatings -> R.id.action_totalScoreFragment_to_userRatingsFragment
                
                else -> 0
            }
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (checkedItem != navController.currentDestination!!.id && action != 0) {
            navController.navigate(action)
        }
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_activity_details, menu)
        
        menu.findItem(R.id.menu_sort_user_ratings).isVisible =
            navController.currentDestination!!.id == R.id.userRatingsFragment
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            
            R.id.details_menu_help -> startActivity(Intent(this@AppDetailsActivity, SettingsActivity::class.java)
                                                        .putExtra("frag", R.id.helpTextFragment))
            
            R.id.menu_sort_user_ratings -> SortUserRatingsBottomSheet().show(supportFragmentManager, "SortUserRatingsBottomSheet")
            
            R.id.menu_more ->
                MoreOptionsBottomSheet(app.name, app.packageName,
                                       mapScoreRangeToStatusString(this@AppDetailsActivity, app.dgScore),
                                       mapScoreRangeToStatusString(this@AppDetailsActivity, app.mgScore),
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