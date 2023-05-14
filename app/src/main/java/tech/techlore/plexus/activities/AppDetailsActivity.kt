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
import android.content.pm.PackageManager
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.awaitResponse
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.fragments.bottomsheets.FirstSubmissionBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.MoreOptionsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.NoNetworkBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.SortUserRatingsBottomSheet
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.FIRST_SUBMISSION
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class AppDetailsActivity : AppCompatActivity(), MenuProvider {
    
    private lateinit var activityBinding: ActivityAppDetailsBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
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
    var selectedVersionString: String? = null
    var statusRadio = R.id.user_ratings_radio_any_status
    var dgStatusSort = 0
    var mgStatusSort = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        addMenuProvider(this)
        activityBinding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        preferenceManager = PreferenceManager(this)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.details_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        selectedVersionString = getString(R.string.any)
        val repository = (applicationContext as ApplicationManager).mainRepository
        val requestManager = Glide.with(this)
        val requestOptions =
            RequestOptions()
                .placeholder(R.drawable.ic_apk) // Placeholder image
                .fallback(R.drawable.ic_apk) // Fallback image in case requested image isn't available
                .centerCrop() // Center-crop the image to fill the ImageView
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache strategy
    
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.bottomAppBar)
        activityBinding.bottomAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        runBlocking {
            launch {
                app = intent.getStringExtra("packageName")?.let { repository.getAppByPackage(it) } !!
            }
        }
    
        lifecycleScope.launch {
    
            // Icon
            val requestBuilder =
                if (!app.isInstalled) {
                    requestManager
                        .load(app.iconUrl)
                        .onlyRetrieveFromCache(true) // Icon should always be in cache
                        .apply(requestOptions)
                }
                else {
                    try {
                        requestManager
                            .load(packageManager.getApplicationIcon(app.packageName))
                            .apply(requestOptions)
                    }
                    catch (e: PackageManager.NameNotFoundException) {
                        throw RuntimeException(e)
                    }
                }
    
            requestBuilder.into(activityBinding.detailsAppIcon)
            activityBinding.detailsName.text = app.name
            activityBinding.detailsPackageName.text = app.packageName
            activityBinding.detailsInstalledVersion.text =
                if (app.installedVersion.isEmpty()) {
                    "${getString(R.string.installed)}: ${getString(R.string.na)}"
                }
                else {
                    "${getString(R.string.installed)}: ${app.installedVersion} (${app.installedBuild})"
                }
    
            // Radio group/buttons
            activityBinding.detailsRadiogroup.setOnCheckedChangeListener{_, checkedId: Int ->
                displayFragment(checkedId)
            }
    
            // FAB
            activityBinding.fab.setOnClickListener {
                when {
                    !app.isInstalled ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.install_app_to_submit),
                                     activityBinding.bottomAppBarRadio)
        
                    preferenceManager.getBoolean(FIRST_SUBMISSION) ->
                        FirstSubmissionBottomSheet(positiveButtonClickListener = { startSubmitActivity() })
                            .show(supportFragmentManager, "FirstSubmissionBottomSheet")
        
                    else -> startSubmitActivity()
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
                        // 2. We're already retrieving latest ratings everytime in details activity
        
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
                activityBinding.detailsRadiogroup.isVisible = true
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
        val currentFragment = navController.currentDestination!!
        
        val action =
            when (checkedItem) {
                
                10 -> R.id.action_fragmentProgressBar_to_totalScoreFragment
                // 10 is just a custom number
                // to let the nav controller navigate from
                // progress bar fragment to total score fragment
                
                R.id.radio_total_score -> R.id.action_userRatingsFragment_to_totalScoreFragment
                
                R.id.radio_user_ratings -> R.id.action_totalScoreFragment_to_userRatingsFragment
                
                else -> 0
            }
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (checkedItem != currentFragment.id && action != 0) {
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
                                                        .putExtra("frag", R.id.helpFragment))
            
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
    
    private fun startSubmitActivity() {
        startActivity(Intent(this@AppDetailsActivity, SubmitActivity::class.java)
                          .putExtra("name", app.name)
                          .putExtra("packageName", app.packageName)
                          .putExtra("installedVersion", app.installedVersion)
                          .putExtra("installedBuild", app.installedBuild)
                          .putExtra("isInPlexusData", app.isInPlexusData))
        finish()
        overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement)
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() { finish() }
    }
}