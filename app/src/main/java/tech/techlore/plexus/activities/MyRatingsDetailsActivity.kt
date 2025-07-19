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
import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.bottomsheets.appdetails.LinksBottomSheet
import tech.techlore.plexus.bottomsheets.appdetails.SortAllRatingsBottomSheet
import tech.techlore.plexus.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.hideViewWithAnim
import tech.techlore.plexus.utils.UiUtils.Companion.mapInstalledFromChipIdToString
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromStyle
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore
import tech.techlore.plexus.utils.UiUtils.Companion.scrollToTop
import tech.techlore.plexus.utils.UiUtils.Companion.setButtonTooltipText
import tech.techlore.plexus.utils.UiUtils.Companion.showViewWithAnim
import kotlin.math.abs

class MyRatingsDetailsActivity : AppCompatActivity() {
    
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
        window.apply {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        var isAppIconVisible = true
        var isScrolledByFab = false
        navHostFragment = supportFragmentManager.findFragmentById(R.id.detailsNavHost) as NavHostFragment
        navController = navHostFragment.navController
        packageNameString = intent.getStringExtra("packageName")!!
        selectedVersionString = getString(R.string.any)
        selectedRomString = getString(R.string.any)
        selectedAndroidString = getString(R.string.any)
        val mainRepository by inject<MainDataRepository>()
        val myRatingsRepository by inject<MyRatingsRepository>()
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left,
                            right = insets.right,
                            bottom = insets.bottom + convertDpToPx(this@MyRatingsDetailsActivity, 70f))
            
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.scrollTopFab) { v, windowInsets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin =
                    windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom +
                    convertDpToPx(this@MyRatingsDetailsActivity, 90f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        lifecycleScope.launch {
            app = mainRepository.getAppByPackage(packageNameString)!!
            myRating = myRatingsRepository.getMyRatingsByPackage(packageNameString)!!
            
            activityBinding.detailsAppBar.apply {
                val totalScrollRange = totalScrollRange.toFloat()
                addOnOffsetChangedListener { _, verticalOffset ->
                    val progress = abs(verticalOffset).toFloat() / totalScrollRange
                    if (progress >= 0.22f && isAppIconVisible) {
                        activityBinding.detailsAppIcon.hideViewWithAnim(shouldScaleDown = true,
                                                                        setEndScaleValues = true,
                                                                        animDuration = 150L)
                        isAppIconVisible = false
                    }
                    else if (progress < 0.22f && ! isAppIconVisible) {
                        activityBinding.detailsAppIcon.showViewWithAnim(shouldScaleUp = true,
                                                                        animDuration = 250L)
                        isAppIconVisible = true
                    }
                }
            }
            
            activityBinding.detailsAppIcon.displayAppIcon(
                context = this@MyRatingsDetailsActivity,
                isInstalled = app.isInstalled,
                packageName = app.packageName,
                iconUrl = app.iconUrl
            )
            
            activityBinding.detailsCollapsingToolbar.title = app.name
            activityBinding.detailsPackageName.text = app.packageName
            
            @SuppressLint("SetTextI18n")
            activityBinding.detailsInstalledVersion.text = "${app.installedVersion} (${app.installedBuild})"
            
            activityBinding.totalRatingsCount.isVisible = false
            
            activityBinding.detailsInstalledFrom.setInstalledFromStyle(
                context = this@MyRatingsDetailsActivity,
                installedFrom = app.installedFrom
            )
            
            activityBinding.detailsChipGroup.isVisible = false
            activityBinding.loadingIndicator.isVisible = false
            activityBinding.retrievingRatingsText.isVisible = false
            
            // Show FAB on scroll
            activityBinding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (scrollY == 0) {
                    activityBinding.scrollTopFab.hide()
                    if (isScrolledByFab) {
                        activityBinding.detailsAppBar.setExpanded(true,true)
                        isScrolledByFab = false
                    }
                }
                else activityBinding.scrollTopFab.show()
            }
            
            // Scroll to top FAB
            activityBinding.scrollTopFab.setOnClickListener {
                activityBinding.nestedScrollView.scrollToTop()
                isScrolledByFab = true
            }
            
            // Back
            activityBinding.detailsBackBtn.apply {
                setButtonTooltipText(getString(R.string.menu_back))
                setOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
            
            // Help
            activityBinding.detailsHelpBtn.apply {
                setButtonTooltipText(getString(R.string.menu_help))
                setOnClickListener {
                    HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
                }
            }
            
            // Sort
            activityBinding.detailsSortBtn.apply {
                setButtonTooltipText(getString(R.string.menu_sort))
                setOnClickListener {
                    SortAllRatingsBottomSheet().show(supportFragmentManager, "SortUserRatingsBottomSheet")
                }
            }
            
            // Links
            activityBinding.detailsLinksBtn.apply {
                setButtonTooltipText(getString(R.string.menu_links))
                setOnClickListener {
                    LinksBottomSheet(app.name, packageNameString).show(supportFragmentManager, "LinksBottomSheet")
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
                        myRating.ratingsDetails.map { it.romName }.distinct().sortedBy { it.lowercase() },
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
            ArrayList(
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
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAfterTransition()
        }
    }
}