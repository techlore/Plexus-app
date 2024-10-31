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

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import retrofit2.awaitResponse
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.fragments.bottomsheets.appdetails.MoreOptionsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.appdetails.RomSelectionBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.appdetails.SortAllRatingsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.appdetails.RateBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.appdetails.SubmitBottomSheet
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.ratingrange.RatingRange
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.IS_REGISTERED
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.objects.DeviceState
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.mapInstalledFromChipIdToString
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromTextViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore
import tech.techlore.plexus.utils.UiUtils.Companion.scrollToTop
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar
import kotlin.getValue

class AppDetailsActivity : AppCompatActivity(), MenuProvider {
    
    private lateinit var activityBinding: ActivityAppDetailsBinding
    lateinit var navController: NavController
    private lateinit var packageNameString: String
    private val apiRepository by inject<ApiRepository>()
    private val mainRepository by inject<MainDataRepository>()
    lateinit var app: MainData
    private var ratingsList = arrayListOf<Rating>()
    private var hasRatings = false
    private var dgGoldRatingsPercent = 0.0f
    private var dgSilverRatingsPercent = 0.0f
    private var dgBronzeRatingsPercent = 0.0f
    private var dgBrokenRatingsPercent = 0.0f
    private var mgGoldRatingsPercent = 0.0f
    private var mgSilverRatingsPercent = 0.0f
    private var mgBronzeRatingsPercent = 0.0f
    private var mgBrokenRatingsPercent = 0.0f
    var sortedRatingsList = arrayListOf<Rating>()
    var isListSorted = false
    var differentLists = arrayOf<Array<String>>()
    lateinit var selectedVersionString: String
    lateinit var selectedRomString: String
    lateinit var selectedAndroidString: String
    var installedFromChipId = R.id.ratingsChipInstalledAny
    var statusToggleBtnId = R.id.ratingsToggleAnyStatus
    var dgStatusSortChipId = 0
    var mgStatusSortChipId = 0
    var submitStatusCheckedChipId = 0
    var submitNotes = ""
    
    private companion object {
        private const val ANIM_DURATION = 400L
        private val SHOW_ANIM_INTERPOLATOR = DecelerateInterpolator()
        private val HIDE_ANIM_INTERPOLATOR = AccelerateInterpolator()
    }
    
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        setNavBarContrastEnforced(window)
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        addMenuProvider(this)
        activityBinding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val encPreferenceManager by inject<EncryptedPreferenceManager>()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.detailsNavHost) as NavHostFragment
        navController = navHostFragment.navController
        packageNameString = intent.getStringExtra("packageName")!!
        val myRatingsRepository by inject<MyRatingsRepository>()
        selectedVersionString = getString(R.string.any)
        selectedRomString = getString(R.string.any)
        selectedAndroidString = getString(R.string.any)
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left,
                            top = insets.top,
                            right = insets.right)
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + convertDpToPx(this@AppDetailsActivity, 80f)
            }
            WindowInsetsCompat.CONSUMED
        }
        mapOf(activityBinding.scrollTopFab to 105f,
              activityBinding.rateBtn to 12f).forEach { (view, margin) ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin =
                        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom +
                        convertDpToPx(this@AppDetailsActivity, margin)
                }
                WindowInsetsCompat.CONSUMED
            }
        }
        
        activityBinding.detailsBottomAppBar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        
        lifecycleScope.launch {
            app = mainRepository.getAppByPackage(packageNameString)!!
            
            displayAppIcon(context = this@AppDetailsActivity,
                           imageView = activityBinding.detailsAppIcon,
                           isInstalled = app.isInstalled,
                           packageName = app.packageName,
                           iconUrl = app.iconUrl)
            
            activityBinding.detailsName.text = app.name
            activityBinding.detailsPackageName.text = app.packageName
            
            activityBinding.detailsInstalledVersion.apply {
                isVisible = app.installedVersion.isNotEmpty()
                if (isVisible) text = "${app.installedVersion} (${app.installedBuild})"
            }
            
            setInstalledFromTextViewStyle(this@AppDetailsActivity,
                                          app.installedFrom,
                                          activityBinding.detailsInstalledFrom)
            
            // Show FAB on scroll
            activityBinding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (scrollY == 0) {
                    activityBinding.scrollTopFab.hide()
                }
                else activityBinding.scrollTopFab.show()
            }
            
            // Scroll to top FAB
            activityBinding.scrollTopFab.setOnClickListener {
                scrollToTop(activityBinding.nestedScrollView)
            }
            
            val myRatingExists =
                myRatingsRepository.getMyRatingsByPackage(app.packageName)?.ratingsDetails?.any {
                    it.version == app.installedVersion
                } == true
            
            // Rate
            activityBinding.rateBtn.setOnClickListener {
                when {
                    !app.isInstalled ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.install_app_to_submit, app.name),
                                     activityBinding.detailsBottomAppBar)
                    
                    !DeviceState.isDeviceDeGoogled && !DeviceState.isDeviceMicroG ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.device_should_be_degoogled_or_microg),
                                     activityBinding.detailsBottomAppBar)
                    
                    encPreferenceManager.getString(DEVICE_ROM).isNullOrEmpty() ->
                        RomSelectionBottomSheet().show(supportFragmentManager, "RomSelectionBottomSheet")
                    
                    !encPreferenceManager.getBoolean(IS_REGISTERED) -> {
                        startActivity(Intent(this@AppDetailsActivity, VerificationActivity::class.java))
                        overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement)
                    }
                    
                    myRatingExists ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.rating_already_submitted, app.name, app.installedVersion),
                                     activityBinding.detailsBottomAppBar)
                    
                    else -> RateBottomSheet().show(supportFragmentManager, "RateBottomSheet")
                }
            }
            
            retrieveRatings()
        }
        
    }
    
    private fun showViewWithAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = ANIM_DURATION
            interpolator = SHOW_ANIM_INTERPOLATOR
            start()
        }.doOnEnd {
            view.isVisible = true
            // Show animated progress after recyclerview is shown,
            // otherwise progress bars animation looks stuck if recyclerview has too many rows
            if (view == activityBinding.detailsNavHost) {
                setTotalScore(isMicroG = DeviceState.isDeviceMicroG)
            }
        }
    }
    
    private fun hideViewWithAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
            duration = ANIM_DURATION
            interpolator = HIDE_ANIM_INTERPOLATOR
            start()
        }.doOnEnd { view.isVisible = false }
    }
    
    private fun retrieveRatings() {
        lifecycleScope.launch {
            if (hasNetwork(this@AppDetailsActivity) && hasInternet()) {
                val ratingsCall = apiRepository.getRatings(packageName = app.packageName, pageNumber = 1)
                val ratingsResponse = ratingsCall.awaitResponse()
                
                if (ratingsResponse.isSuccessful) {
                    ratingsResponse.body()?.let { ratingsRoot ->
                        ratingsRoot.ratingsData.apply {
                            if (isNotEmpty()) {
                                hasRatings = true
                                ratingsList.addAll(this)
                            }
                        }
                        
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
                                            ratingsList.addAll(root.ratingsData)
                                        }
                                    }
                                }
                                requests.add(request)
                            }
                            // Wait for all requests to complete
                            requests.awaitAll()
                        }
                    }
                    
                    afterRatingsRetrieved()
                }
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveButtonClickListener = { retrieveRatings() },
                                     negativeButtonClickListener = { finish() })
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
            
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun afterRatingsRetrieved(){
        lifecycleScope.launch {
            // Since the latest ratings, scores etc. are already retrieved & calculated,
            // update in db
            if (hasRatings) {
                mainRepository.updateSingleApp(packageName = packageNameString)
                app = mainRepository.getAppByPackage(packageNameString)!!
                DataState.isDataUpdated = true
            }
            
            withContext(Dispatchers.Default) {
                val ratingRanges = listOf(RatingRange("gold", 4.0f, 4.0f),
                                          RatingRange("silver", 3.0f, 3.9f),
                                          RatingRange("bronze", 2.0f, 2.9f),
                                          RatingRange("broken", 1.0f, 1.9f))
                
                val ratingCounts = mutableMapOf<Pair<String?, String>, Int>()
                for (rating in ratingsList) {
                    for (range in ratingRanges) {
                        if (rating.ratingScore!!.ratingScore >= range.minValue
                            && rating.ratingScore!!.ratingScore <= range.maxValue) {
                            val key = rating.ratingType to range.status
                            ratingCounts[key] = (ratingCounts[key] ?: 0) + 1
                        }
                    }
                }
                
                dgGoldRatingsPercent = calcPercent(ratingCounts["native" to "gold"] ?: 0, app.totalDgRatings)
                dgSilverRatingsPercent = calcPercent(ratingCounts["native" to "silver"] ?: 0, app.totalDgRatings)
                dgBronzeRatingsPercent = calcPercent(ratingCounts["native" to "bronze"] ?: 0, app.totalDgRatings)
                dgBrokenRatingsPercent = calcPercent(ratingCounts["native" to "broken"] ?: 0, app.totalDgRatings)
                mgGoldRatingsPercent = calcPercent(ratingCounts["micro_g" to "gold"] ?: 0, app.totalMgRatings)
                mgSilverRatingsPercent = calcPercent(ratingCounts["micro_g" to "silver"] ?: 0, app.totalMgRatings)
                mgBronzeRatingsPercent = calcPercent(ratingCounts["micro_g" to "bronze"] ?: 0, app.totalMgRatings)
                mgBrokenRatingsPercent = calcPercent(ratingCounts["micro_g" to "broken"] ?: 0, app.totalMgRatings)
                
                // Get different app versions, ROMs & android versions from ratings list
                // and store them in a separate list to show in sort ratings bottom sheet
                differentLists =
                    arrayOf(
                        // App version
                        arrayOf(getString(R.string.any)) +
                        ratingsList.map { "${it.version} (${it.buildNumber})" }.distinct(),
                        // ROMs
                        arrayOf(getString(R.string.any)) +
                        ratingsList.map { it.romName }.distinct(),
                        // Android versions
                        arrayOf(getString(R.string.any)) +
                        ratingsList.map { it.androidVersion }.distinct()
                    )
                
                sortRatings()
            }
            
            // Chip group
            activityBinding.totalScoreChipGroup.apply {
                check(if (DeviceState.isDeviceMicroG) R.id.mgScoreChip
                      else R.id.dgScoreChip)
                setOnCheckedStateChangeListener { group, _ ->
                    setTotalScore(
                        when (group.checkedChipId) {
                            R.id.mgScoreChip -> true
                            else -> false
                        }
                    )
                }
            }
            
            // Set all ratings fragment
            navController.navInflater.inflate(R.navigation.details_fragments_nav_graph).apply {
                setStartDestination(R.id.allRatingsFragment)
                navController.setGraph(this, intent.extras)
            }
            
            arrayOf(activityBinding.loadingIndicator, activityBinding.retrievingRatingsText,
                   activityBinding.totalScoreText, activityBinding.totalScoreCard).forEachIndexed { index, view ->
                if (index < 2) hideViewWithAnimation(view)
                else showViewWithAnimation(view)
            }
            
            activityBinding.totalRatingsCount.apply {
                text = "${getString(R.string.total_ratings)}: ${(app.totalDgRatings + app.totalMgRatings)}"
                showViewWithAnimation(this)
            }
            
            showViewWithAnimation(activityBinding.detailsNavHost)
        }
    }
    
    private fun calcPercent(ratingsCount: Int, totalRatings: Int): Float {
        return if (totalRatings == 0 || ratingsCount == 0) 0.0f else {
            val result = (ratingsCount.toFloat() / totalRatings.toFloat()) * 100.0f
            ((result * 10.0f).toInt().toFloat()) / 10.0f // Limit result to 1 decimal place without rounding off
        }
    }
    
    private fun removeDotZeroFromFloat(avgScore: Float): String {
        return avgScore.toString().removeSuffix(".0")
    }
    
    private fun mapScoreRangeToColor(score: Float): Int {
        return when(score) {
            0.0f -> 0
            in 1.0f..1.9f -> resources.getColor(R.color.color_broken_status, theme)
            in 2.0f..2.9f -> resources.getColor(R.color.color_bronze_status, theme)
            in 3.0f..3.9f -> resources.getColor(R.color.color_silver_status, theme)
            else -> resources.getColor(R.color.color_gold_status, theme)
        }
    }
    
    private fun mapScoreRangeToProgress(score: Float): Int {
        return when(score) {
            0.0f -> 0
            else -> ((score / 4.0f) * 100.0f ).toInt()
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun setTotalScore(isMicroG: Boolean) {
        val ratingsCount: Int
        val score: Float
        val goldRatingsPercent: Float
        val silverRatingsPercent: Float
        val bronzeRatingsPercent: Float
        val brokenRatingsPercent: Float
        
        if (isMicroG) {
            ratingsCount = app.totalMgRatings
            score = app.mgScore
            goldRatingsPercent = mgGoldRatingsPercent
            silverRatingsPercent = mgSilverRatingsPercent
            bronzeRatingsPercent = mgBronzeRatingsPercent
            brokenRatingsPercent = mgBrokenRatingsPercent
        }
        else {
            ratingsCount = app.totalDgRatings
            score = app.dgScore
            goldRatingsPercent = dgGoldRatingsPercent
            silverRatingsPercent = dgSilverRatingsPercent
            bronzeRatingsPercent = dgBronzeRatingsPercent
            brokenRatingsPercent = dgBrokenRatingsPercent
        }
        
        activityBinding.apply {
            this.ratingsCount.text = "${getString(R.string.ratings)}: $ratingsCount"
            avgScore.text = "${removeDotZeroFromFloat(score)}/4"
            progressCircle.apply {
                setIndicatorColor(mapScoreRangeToColor(score))
                setProgressCompat(mapScoreRangeToProgress(score), true)
            }
            goldProgress.setProgressCompat(goldRatingsPercent.toInt(), true)
            goldPercent.text = "${removeDotZeroFromFloat(goldRatingsPercent)}%"
            silverProgress.setProgressCompat(silverRatingsPercent.toInt(), true)
            silverPercent.text = "${removeDotZeroFromFloat(silverRatingsPercent)}%"
            bronzeProgress.setProgressCompat(bronzeRatingsPercent.toInt(), true)
            bronzePercent.text = "${removeDotZeroFromFloat(bronzeRatingsPercent)}%"
            brokenProgress.setProgressCompat(brokenRatingsPercent.toInt(), true)
            brokenPercent.text = "${removeDotZeroFromFloat(brokenRatingsPercent)}%"
        }
    }
    
    fun sortRatings() {
        sortedRatingsList =
            ArrayList<Rating>(
                ratingsList
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
                            || rating.ratingType == (if (statusToggleBtnId == R.id.ratingsToggleDgStatus) "native" else "micro_g")
                        
                        val statusChipMatches =
                            when {
                                statusToggleBtnId == R.id.ratingsToggleDgStatus
                                && dgStatusSortChipId != R.id.ratingsSortAny -> {
                                    rating.ratingScore?.ratingScore == mapStatusChipIdToRatingScore(dgStatusSortChipId)
                                }
                                statusToggleBtnId == R.id.ratingsToggleMgStatus
                                && mgStatusSortChipId != R.id.ratingsSortAny -> {
                                    rating.ratingScore?.ratingScore == mapStatusChipIdToRatingScore(mgStatusSortChipId)
                                }
                                else -> true
                            }
                        
                        appVerMatches && romMatches
                        && androidMatches && installedFromMatches
                        && statusToggleMatches && statusChipMatches
                        
                    })
        
        isListSorted = true
    }
    
    fun showSubmitBtmSheet() {
        lifecycleScope.launch {
            if (hasNetwork(this@AppDetailsActivity) && hasInternet()) {
                SubmitBottomSheet().show(supportFragmentManager, "SubmitBottomSheet")
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveButtonClickListener = { showSubmitBtmSheet() },
                                     negativeButtonClickListener = {})
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_activity_details, menu)
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.details_menu_help -> HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
            R.id.menu_sort_user_ratings -> SortAllRatingsBottomSheet().show(supportFragmentManager, "SortUserRatingsBottomSheet")
            R.id.menu_more ->
                MoreOptionsBottomSheet(app.name, app.packageName,
                                       mapScoreRangeToStatusString(this@AppDetailsActivity, app.dgScore),
                                       mapScoreRangeToStatusString(this@AppDetailsActivity, app.mgScore),
                                       activityBinding.detailsCoordLayout,
                                       activityBinding.detailsBottomAppBar)
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