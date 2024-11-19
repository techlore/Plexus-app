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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.bottomsheets.appdetails.MoreOptionsBottomSheet
import tech.techlore.plexus.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.bottomsheets.myratingsdetails.SortMyRatingsDetailsBottomSheet
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.mapInstalledFromChipIdToString
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromStyle
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore
import tech.techlore.plexus.utils.UiUtils.Companion.scrollToTop
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced

class MyRatingsDetailsActivity : AppCompatActivity(), MenuProvider {
    
    lateinit var activityBinding: ActivityAppDetailsBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    private lateinit var app: MainData
    private lateinit var packageNameString: String
    private lateinit var myRating: MyRating
    var sortedMyRatingsDetailsList = arrayListOf<MyRatingDetails>()
    var isListSorted = false
    var differentLists = arrayOf<Array<String>>()
    lateinit var selectedVersionString: String
    lateinit var selectedRomString: String
    lateinit var selectedAndroidString: String
    var installedFromChipId = R.id.ratingsChipInstalledAny
    var statusToggleBtnId = R.id.ratingsToggleAnyStatus
    var dgStatusSortChipId = 0
    var mgStatusSortChipId = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.setNavBarContrastEnforced()
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        addMenuProvider(this)
        activityBinding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        navHostFragment = supportFragmentManager.findFragmentById(R.id.detailsNavHost) as NavHostFragment
        navController = navHostFragment.navController
        packageNameString = intent.getStringExtra("packageName")!!
        selectedVersionString = getString(R.string.any)
        selectedRomString = getString(R.string.any)
        selectedAndroidString = getString(R.string.any)
        val mainRepository by inject<MainDataRepository>()
        val myRatingsRepository by inject<MyRatingsRepository>()
        
        // Adjust nested scrollview for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left,
                            top = insets.top,
                            right = insets.right,
                            bottom = insets.bottom + convertDpToPx(this@MyRatingsDetailsActivity, 80f))
            
            WindowInsetsCompat.CONSUMED
        }
        
        activityBinding.detailsBottomAppBar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        
        lifecycleScope.launch {
            app = mainRepository.getAppByPackage(packageNameString)!!
            myRating = myRatingsRepository.getMyRatingsByPackage(packageNameString)!!
            
            activityBinding.detailsAppIcon.displayAppIcon(
                context = this@MyRatingsDetailsActivity,
                isInstalled = app.isInstalled,
                packageName = app.packageName,
                iconUrl = app.iconUrl
            )
            
            activityBinding.detailsName.text = app.name
            activityBinding.detailsPackageName.text = app.packageName
            
            @SuppressLint("SetTextI18n")
            activityBinding.detailsInstalledVersion.text = "${app.installedVersion} (${app.installedBuild})"
            
            activityBinding.totalRatingsCount.isVisible = false
            
            activityBinding.detailsInstalledFrom.setInstalledFromStyle(
                context = this@MyRatingsDetailsActivity,
                installedFrom = app.installedFrom
            )
            
            activityBinding.loadingIndicator.isVisible = false
            activityBinding.retrievingRatingsText.isVisible = false
            
            // Show FAB on scroll
            activityBinding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (scrollY == 0) {
                    activityBinding.scrollTopFab.hide()
                }
                else activityBinding.scrollTopFab.show()
            }
            
            // Scroll to top FAB
            activityBinding.scrollTopFab.setOnClickListener {
                activityBinding.nestedScrollView.scrollToTop()
                // Show bottom app bar on scroll up,
                // otherwise when scrollview reaches top, bottom app bar stays hidden.
                activityBinding.detailsBottomAppBar.apply {
                    if (isScrolledDown) performShow()
                }
            }
            
            activityBinding.totalRatingsCount.apply {
                @SuppressLint("SetTextI18n")
                text = "${getString(R.string.my_ratings)}: ${(myRating.ratingsDetails.size)}"
                isVisible = true
            }
            
            navController.navInflater.inflate(R.navigation.details_fragments_nav_graph).apply {
                setStartDestination(R.id.myRatingsDetailsFragment)
                navController.setGraph(this, intent.extras)
            }
            
            activityBinding.detailsNavHost.isVisible = true
            activityBinding.rateBtn.isVisible = false
            
            withContext(Dispatchers.Default) {
                // Get different app versions, ROMs & android versions from ratings details list
                // and store them in a separate list to show in sort my ratings details bottom sheet
                differentLists =
                    arrayOf(
                        // App version
                        arrayOf(getString(R.string.any)) +
                        myRating.ratingsDetails.map { "${it.version} (${it.buildNumber})" }.distinct(),
                        // ROMs
                        arrayOf(getString(R.string.any)) +
                        myRating.ratingsDetails.map { it.romName }.distinct(),
                        // Android versions
                        arrayOf(getString(R.string.any)) +
                        myRating.ratingsDetails.map { it.androidVersion }.distinct()
                    )
                
                sortMyRatingsDetails()
            }
        }
    }
    
    fun sortMyRatingsDetails() {
        sortedMyRatingsDetailsList =
            ArrayList<MyRatingDetails>(
                myRating.ratingsDetails
                    .filter { rating ->
                        val appVerMatches =
                            selectedVersionString == getString(R.string.any)
                            || rating.version == selectedVersionString.substringBefore(" (")
                        
                        val romMatches =
                            selectedRomString == getString(R.string.any)
                            || rating.romName == selectedRomString
                        
                        val androidMatches =
                            selectedAndroidString == getString(R.string.any)
                            || rating.androidVersion == selectedAndroidString
                        
                        val installedFromMatches =
                            installedFromChipId == R.id.ratingsChipInstalledAny
                            || rating.installedFrom == mapInstalledFromChipIdToString(installedFromChipId)
                        
                        val statusToggleMatches =
                            statusToggleBtnId == R.id.ratingsToggleAnyStatus
                            || rating.googleLib == (if (statusToggleBtnId == R.id.ratingsToggleDgStatus) "native" else "micro_g")
                        
                        val statusChipMatches =
                            when {
                                statusToggleBtnId == R.id.ratingsToggleDgStatus
                                && dgStatusSortChipId != R.id.ratingsSortAny -> {
                                    rating.myRatingScore == mapStatusChipIdToRatingScore(dgStatusSortChipId)
                                }
                                statusToggleBtnId == R.id.ratingsToggleMgStatus
                                && mgStatusSortChipId != R.id.ratingsSortAny -> {
                                    rating.myRatingScore == mapStatusChipIdToRatingScore(mgStatusSortChipId)
                                }
                                else -> true
                            }
                        
                        appVerMatches && romMatches
                        && androidMatches && installedFromMatches
                        && statusToggleMatches && statusChipMatches
                        
                    })
        
        isListSorted = true
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_activity_details, menu)
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.details_menu_help -> HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
            R.id.menu_sort_user_ratings -> SortMyRatingsDetailsBottomSheet().show(supportFragmentManager, "SortMyRatingsBottomSheet")
            R.id.menu_more ->
                MoreOptionsBottomSheet(app.name,
                                       packageNameString,
                                       mapScoreRangeToStatusString(this@MyRatingsDetailsActivity, app.dgScore),
                                       mapScoreRangeToStatusString(this@MyRatingsDetailsActivity, app.mgScore))
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