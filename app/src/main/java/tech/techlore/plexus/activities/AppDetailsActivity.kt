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
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.appdetails.LinksBottomSheet
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.bottomsheets.common.RomSelectionBottomSheet
import tech.techlore.plexus.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.bottomsheets.appdetails.SortAllRatingsBottomSheet
import tech.techlore.plexus.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.bottomsheets.appdetails.RateBottomSheet
import tech.techlore.plexus.bottomsheets.appdetails.SubmitBottomSheet
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.ratingrange.RatingRange
import tech.techlore.plexus.objects.AppState
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.IS_REGISTERED
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.objects.DeviceState
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL
import tech.techlore.plexus.utils.IntentUtils.Companion.startActivityWithTransition
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.hideViewWithAnim
import tech.techlore.plexus.utils.UiUtils.Companion.mapInstalledFromChipIdToString
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromStyle
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore
import tech.techlore.plexus.utils.UiUtils.Companion.scrollToTop
import tech.techlore.plexus.utils.UiUtils.Companion.setButtonTooltipText
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar
import tech.techlore.plexus.utils.UiUtils.Companion.showViewWithAnim
import kotlin.getValue
import kotlin.math.abs
import androidx.core.graphics.scale
import com.google.android.material.button.MaterialButton
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlin.system.exitProcess

class AppDetailsActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityAppDetailsBinding
    lateinit var navController: NavController
    private lateinit var packageNameString: String
    private var isFromShortcut = false
    private var toggleBtnCheckIcon: Drawable? = null
    private var dgIcon: Drawable? = null
    private var mgIcon: Drawable? = null
    private val apiRepository by inject<ApiRepository>()
    private val mainRepository by inject<MainDataRepository>()
    lateinit var app: MainData
    private var ratingsList = arrayListOf<Rating>()
    private var hasRatings = false
    var isScrolledByFab = false
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
    var differentAppVerList = arrayOf<String>()
    var differentRomsList = arrayOf<String>()
    var differentAndroidVerList = arrayOf<String>()
    lateinit var selectedVersionString: String
    lateinit var selectedRomString: String
    lateinit var selectedAndroidString: String
    var installedFromChipId = R.id.ratingsChipInstalledAny
    var statusToggleBtnId = R.id.ratingsToggleAnyStatus
    var dgStatusSortChipId = 0
    var mgStatusSortChipId = 0
    var submitStatusCheckedChipId = 0
    var submitNotes = ""
    
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        isFromShortcut = intent.getBooleanExtra("fromShortcut", false)
        window.apply {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition =
                MaterialSharedAxis(
                    if (isFromShortcut) MaterialSharedAxis.X else MaterialSharedAxis.Z,
                    true
                )
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        var isAppIconVisible = true
        val encPreferenceManager by inject<EncryptedPreferenceManager>()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.detailsNavHost) as NavHostFragment
        navController = navHostFragment.navController
        packageNameString = intent.getStringExtra("packageName")!!
        selectedVersionString = getString(R.string.any)
        selectedRomString = getString(R.string.any)
        selectedAndroidString = getString(R.string.any)
        toggleBtnCheckIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done, theme)
        dgIcon = ContextCompat.getDrawable(this, R.drawable.ic_degoogled)
        mgIcon = ContextCompat.getDrawable(this, R.drawable.ic_microg)
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left,
                            right = insets.right,
                            bottom = insets.bottom + convertDpToPx(this@AppDetailsActivity, 70f))
            
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.scrollTopFab) { v, windowInsets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin =
                    windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom +
                    convertDpToPx(this@AppDetailsActivity, 90f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        lifecycleScope.launch {
            app = mainRepository.getAppByPackage(packageNameString)!!
            
            // Show/hide anchored icon with FAB like animation
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
                context = this@AppDetailsActivity,
                isInstalled = app.isInstalled,
                packageName = app.packageName,
                iconUrl = app.iconUrl
            )
            
            activityBinding.detailsCollapsingToolbar.title = app.name
            activityBinding.detailsPackageName.text = app.packageName
            
            activityBinding.detailsInstalledVersion.apply {
                isVisible = app.installedVersion.isNotEmpty()
                if (isVisible) {
                    text = "${app.installedVersion} (${app.installedBuild})"
                    activityBinding.detailsInstalledFrom.setInstalledFromStyle(
                        context = this@AppDetailsActivity,
                        installedFrom = app.installedFrom
                    )
                }
            }
            
            // Share chip
            activityBinding.shareChip.setOnClickListener {
                startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT,
                                  """
                              ${getString(R.string.app)}: ${app.name}
                              ${getString(R.string.package_name)}: $packageNameString
                              ${getString(R.string.de_Googled)}: ${
                                      mapScoreRangeToStatusString(this@AppDetailsActivity, app.dgScore)
                                  }
                              ${getString(R.string.microG)}: ${
                                      mapScoreRangeToStatusString(this@AppDetailsActivity, app.mgScore)
                                  }
                              """.trimIndent()),
                    getString(R.string.menu_share)))
            }
            
            // Shortcut chip
            if (Build.VERSION.SDK_INT >= 26) {
                activityBinding.shortcutChip.apply {
                    isVisible = true
                    setOnClickListener {
                        val bitmap =
                            if (app.isInstalled) packageManager.getApplicationIcon(packageNameString).toBitmap()
                            else (activityBinding.detailsAppIcon.drawable as? BitmapDrawable)?.bitmap?.toSoftwareBitmap()
                        
                        val resizedBitmap =
                            bitmap?.let {
                                // https://developer.android.com/reference/android/graphics/drawable/AdaptiveIconDrawable.html
                                // fullSize = 108
                                // imageSize = 77 (72dp icon + 5dp padding)
                                val output = createBitmap(108, 108)
                                val canvas = Canvas(output)
                                val compositeColor =
                                    ColorUtils.compositeColors(
                                        Color.argb(0, 0, 0, 0),
                                        Color.WHITE
                                    )
                                val resized = it.scale(77, 77)
                                val adjustValue = 15.5f // (108 - 77) / 2; To keep the icon centered
                                
                                canvas.drawColor(compositeColor) // Background color
                                canvas.drawBitmap(resized, adjustValue, adjustValue, null)
                                output
                            }
                        
                        val shortcutIcon =
                            resizedBitmap?.let {
                                IconCompat.createWithAdaptiveBitmap(it)
                            } ?: IconCompat.createWithResource(this@AppDetailsActivity,
                                                               R.drawable.ic_apk)
                        
                        val shortcut =
                            ShortcutInfoCompat.Builder(this@AppDetailsActivity, packageNameString)
                                .setShortLabel(app.name)
                                .setIcon(shortcutIcon)
                                .setIntent(
                                    Intent(this@AppDetailsActivity,
                                           ShortcutRouterActivity::class.java).apply {
                                        action = Intent.ACTION_VIEW
                                        putExtra("packageName", packageNameString)
                                    }
                                )
                                .build()
                        
                        ShortcutManagerCompat.requestPinShortcut(this@AppDetailsActivity,
                                                                 shortcut,
                                                                 null)
                    }
                }
            }
            
            // VPN Toolkit chip
            activityBinding.vpnToolkitChip.apply {
                if (app.name.contains("VPN", ignoreCase = true)
                    || packageNameString.contains("VPN", ignoreCase = true)) {
                    isVisible = true
                    setOnClickListener {
                        openURL(getString(R.string.vpn_toolkit_url))
                    }
                }
            }
            
            // Scroll to top FAB
            activityBinding.scrollTopFab.setOnClickListener {
                activityBinding.nestedScrollView.scrollToTop()
                isScrolledByFab = true
            }
            
            val myRatingExists =
                get<MyRatingsRepository>().getMyRatingsByPackage(app.packageName)?.ratingsDetails?.any {
                    it.version == app.installedVersion
                } == true
            
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
                    LinksBottomSheet(packageNameString).show(supportFragmentManager, "LinksBottomSheet")
                }
            }
            
            // Rate
            activityBinding.rateBtn.setOnClickListener {
                when {
                    !app.isInstalled ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.install_app_to_submit, app.name),
                                     anchorView =
                                         if (activityBinding.scrollTopFab.isVisible) activityBinding.scrollTopFab
                                         else activityBinding.detailsFloatingToolbar)
                    
                    !DeviceState.isDeviceDeGoogled && !DeviceState.isDeviceMicroG ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.device_should_be_degoogled_or_microg),
                                     activityBinding.detailsFloatingToolbar)
                    
                    encPreferenceManager.getString(DEVICE_ROM).isNullOrEmpty() ->
                        RomSelectionBottomSheet(isFromNavView = false).show(supportFragmentManager, "RomSelectionBottomSheet")
                    
                    !encPreferenceManager.getBoolean(IS_REGISTERED) -> {
                        startActivityWithTransition(Intent(this@AppDetailsActivity, VerificationActivity::class.java))
                    }
                    
                    myRatingExists ->
                        showSnackbar(activityBinding.detailsCoordLayout,
                                     getString(R.string.rating_already_submitted, app.name, app.installedVersion),
                                     activityBinding.detailsFloatingToolbar)
                    
                    else -> RateBottomSheet().show(supportFragmentManager, "RateBottomSheet")
                }
            }
            
            retrieveRatings()
        }
        
    }
    
    // Software rendering doesn't support hardware bitmaps
    @RequiresApi(Build.VERSION_CODES.O)
    private fun Bitmap.toSoftwareBitmap(): Bitmap {
        if (config != Bitmap.Config.HARDWARE) return this
        return copy(Bitmap.Config.ARGB_8888, true)
    }
    
    private fun retrieveRatings() {
        lifecycleScope.launch {
            if (hasNetwork(this@AppDetailsActivity) && hasInternet()) {
                try {
                    val ratingsResponse = apiRepository.getRatings(packageName = app.packageName, pageNumber = 1)
                    
                    ratingsResponse.ratingsData.apply {
                        if (isNotEmpty()) {
                            hasRatings = true
                            ratingsList.addAll(this)
                        }
                    }
                    
                    // Retrieve remaining ratings in parallel
                    // No need to store the ratings list in database, as:
                    // 1. It's not used anywhere else, except details activity
                    // 2. We're already retrieving the latest ratings everytime in details activity
                    if (ratingsResponse.meta.totalPages > 1) {
                        val requests = mutableListOf<Deferred<Boolean>>()
                        (2 .. ratingsResponse.meta.totalPages).forEach { pageNumber ->
                            val request =
                                async {
                                    val remRatingsResponse = apiRepository.getRatings(app.packageName, pageNumber)
                                    ratingsList.addAll(remRatingsResponse.ratingsData)
                                }
                            requests.add(request)
                        }
                        requests.awaitAll() // Wait for all requests to complete
                    }
                    
                    // Since the latest ratings are already retrieved,
                    // get latest score of current app & update in DB
                    if (hasRatings) {
                        mainRepository.updateSingleApp(packageName = packageNameString)
                        app = mainRepository.getAppByPackage(packageNameString) !!
                        DataState.isDataUpdated = true
                    }
                    
                    afterRatingsRetrieved()
                }
                catch (e: Exception) {
                    NoNetworkBottomSheet(isNoNetworkError = false,
                                         exception = e,
                                         negativeButtonText = getString(R.string.exit),
                                         positiveBtnClickAction = { retrieveRatings() },
                                         negativeBtnClickAction = { finishAfterTransition() })
                        .show(supportFragmentManager, "NoNetworkBottomSheet")
                }
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveBtnClickAction = { retrieveRatings() },
                                     negativeBtnClickAction = { finishAfterTransition() })
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
            
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun afterRatingsRetrieved(){
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                val ratingRanges = arrayOf(RatingRange("gold", 4.0f, 4.0f),
                                           RatingRange("silver", 3.0f, 3.9f),
                                           RatingRange("bronze", 2.0f, 2.9f),
                                           RatingRange("broken", 1.0f, 1.9f))
                
                val ratingCounts = mutableMapOf<Pair<String?, String>, Int>()
                for (rating in ratingsList) {
                    for (range in ratingRanges) {
                        if (rating.ratingScore !!.ratingScore >= range.minValue
                            && rating.ratingScore !!.ratingScore <= range.maxValue) {
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
                differentAppVerList =
                    arrayOf(getString(R.string.any)) +
                    ratingsList.map { "${it.version} (${it.buildNumber})" }.distinct()
                
                differentRomsList =
                    arrayOf(getString(R.string.any)) +
                    ratingsList.map { it.romName }.distinct().sortedBy { it.lowercase() }
                
                differentAndroidVerList =
                    arrayOf(getString(R.string.any)) +
                    ratingsList.map { it.androidVersion }.distinct()
                
                sortRatings()
            }
            
            // Toggle button group
            activityBinding.detailsToggleBtnGroup.apply {
                val selectedToggle =
                    if (DeviceState.isDeviceMicroG) R.id.mgScoreToggleBtn else R.id.dgScoreToggleBtn
                check(selectedToggle)
                findViewById<MaterialButton>(selectedToggle).icon = toggleBtnCheckIcon
                addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        findViewById<MaterialButton>(checkedId).icon = toggleBtnCheckIcon // Add checkmark icon
                        setTotalScore(
                            when (checkedId) {
                                R.id.mgScoreToggleBtn -> true
                                else -> false
                            }
                        )
                    }
                    else {
                        findViewById<MaterialButton>(checkedId).icon =
                            when (checkedId) {
                                R.id.mgScoreToggleBtn -> mgIcon
                                else -> dgIcon
                            }
                    }
                }
            }
            
            // Set all ratings fragment
            navController.navInflater.inflate(R.navigation.details_fragments_nav_graph).apply {
                setStartDestination(R.id.allRatingsFragment)
                navController.setGraph(this, intent.extras)
            }
            
            arrayOf(activityBinding.loadingIndicator, activityBinding.retrievingRatingsText,
                    activityBinding.totalScoreText, activityBinding.totalScoreCard).forEachIndexed { index, view ->
                if (index < 2) view.isVisible = false
                else view.showViewWithAnim()
            }
            
            activityBinding.totalRatingsCount.apply {
                text = "${getString(R.string.total_ratings)}: ${(app.totalDgRatings + app.totalMgRatings)}"
                showViewWithAnim()
            }
            
            // No need to animate recycler view as it won't be shown unless scrolled
            activityBinding.detailsNavHost.isVisible = true
            
            // Show animated progress after recyclerview is shown,
            // otherwise progress bars animation looks stuck if recyclerview has too many rows
            setTotalScore(isMicroG = DeviceState.isDeviceMicroG)
            
            activityBinding.detailsSortBtn.isEnabled = true
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
            ArrayList(
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
    
    fun showSubmitBottomSheet() {
        lifecycleScope.launch {
            if (hasNetwork(this@AppDetailsActivity) && hasInternet()) {
                SubmitBottomSheet().show(supportFragmentManager, "SubmitBottomSheet")
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveBtnClickAction = { showSubmitBottomSheet() },
                                     negativeBtnClickAction = {})
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (AppState.isVerificationSuccessful) {
            RateBottomSheet().show(supportFragmentManager, "RateBottomSheet")
            AppState.isVerificationSuccessful = false
        }
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFromShortcut) {
                AppState.isAppOpen = false
                finishAndRemoveTask()
                exitProcess(0)
            }
            else finishAfterTransition()
        }
    }
}