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
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.appdetails.LinksBottomSheet
import tech.techlore.plexus.bottomsheets.appdetails.SortAllRatingsBottomSheet
import tech.techlore.plexus.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.interfaces.SortPrefsChangedListener
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.hideViewWithAnim
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment
import tech.techlore.plexus.utils.UiUtils.Companion.scrollToTop
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromStyle
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced
import tech.techlore.plexus.utils.UiUtils.Companion.showViewWithAnim
import kotlin.getValue
import kotlin.math.abs

abstract class BaseDetailsActivity : AppCompatActivity(), SortPrefsChangedListener {
    
    lateinit var activityBinding: ActivityAppDetailsBinding
    protected lateinit var navController: NavController
    protected lateinit var packageNameString: String
    var checkIcon: Drawable? = null
    protected var myRating: MyRating? = null
    protected var isFromShortcut = false
    var isScrolledByFab = false
    protected val mainRepository by inject<MainDataRepository>()
    protected val myRatingsRepository by inject<MyRatingsRepository>()
    lateinit var app: MainData
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
    
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        isFromShortcut = intent.getBooleanExtra("fromShortcut", false)
        window.apply {
            setNavBarContrastEnforced()
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
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.detailsNavHost) as NavHostFragment
        navController = navHostFragment.navController
        packageNameString = intent.getStringExtra("packageName")!!
        checkIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done, theme)
        selectedVersionString = getString(R.string.any)
        selectedRomString = getString(R.string.any)
        selectedAndroidString = getString(R.string.any)
        initAdditionalValuesInOnCreate()
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom + convertDpToPx(this, 70f)
            )
            
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.scrollTopFab) { v, windowInsets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin =
                    windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom +
                    convertDpToPx(this@BaseDetailsActivity, 90f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        lifecycleScope.launch {
            app = mainRepository.getAppByPackage(packageNameString)!!
            myRating = myRatingsRepository.getMyRatingsByPackage(packageNameString)
            
            // Show/hide anchored icon with FAB like animation
            activityBinding.detailsAppBar.apply {
                val totalScrollRange = totalScrollRange.toFloat()
                addOnOffsetChangedListener { _, verticalOffset ->
                    val progress = abs(verticalOffset).toFloat() / totalScrollRange
                    if (progress >= 0.22f && isAppIconVisible) {
                        activityBinding.detailsAppIcon.hideViewWithAnim(isFab = true)
                        isAppIconVisible = false
                    }
                    else if (progress < 0.22f && ! isAppIconVisible) {
                        activityBinding.detailsAppIcon.showViewWithAnim(isFab = true)
                        isAppIconVisible = true
                    }
                }
            }
            
            activityBinding.detailsAppIcon.displayAppIcon(
                context = this@BaseDetailsActivity,
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
                        context = this@BaseDetailsActivity,
                        installedFrom = app.installedFrom
                    )
                }
            }
            
            setupUiComponents()
            
            // Scroll to top FAB
            activityBinding.scrollTopFab.setOnClickListener {
                activityBinding.nestedScrollView.scrollToTop()
                isScrolledByFab = true
            }
            
            // Back
            activityBinding.detailsBackBtn.apply {
                tooltipText = getString(R.string.menu_back)
                setOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
            
            // Help
            activityBinding.detailsHelpBtn.apply {
                tooltipText = getString(R.string.menu_help)
                setOnClickListener {
                    HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
                }
            }
            
            // Sort
            activityBinding.detailsSortBtn.apply {
                tooltipText = getString(R.string.menu_sort)
                setOnClickListener {
                    SortAllRatingsBottomSheet(this@BaseDetailsActivity)
                        .show(supportFragmentManager, "SortUserRatingsBottomSheet")
                }
            }
            
            // Links
            activityBinding.detailsLinksBtn.apply {
                tooltipText = getString(R.string.menu_links)
                setOnClickListener {
                    LinksBottomSheet(packageNameString).show(supportFragmentManager, "LinksBottomSheet")
                }
            }
            
            retrieveAndDisplayData()
        }
    }
    
    protected abstract fun initAdditionalValuesInOnCreate()
    
    protected abstract suspend fun setupUiComponents()
    
    protected abstract suspend fun retrieveAndDisplayData()
    
    override fun onSortPrefsChanged() {
        isListSorted = false
        navController.refreshFragment()
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackNavigation()
        }
    }
    
    // Default back press action
    // Subclasses can override if required
    protected open fun handleBackNavigation() {
        finishAfterTransition()
    }
}