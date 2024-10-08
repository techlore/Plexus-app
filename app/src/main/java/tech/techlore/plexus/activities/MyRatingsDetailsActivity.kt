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
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.fragments.bottomsheets.appdetails.MoreOptionsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.myratingsdetails.SortMyRatingsDetailsBottomSheet
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromTextViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString
import tech.techlore.plexus.utils.UiUtils.Companion.scrollToTop

class MyRatingsDetailsActivity : AppCompatActivity(), MenuProvider {
    
    lateinit var activityBinding: ActivityAppDetailsBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
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
    var statusToggleBtn = R.id.ratingsToggleAnyStatus
    var dgStatusSort = 0
    var mgStatusSort = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
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
                            right = insets.right)
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + convertDpToPx(this@MyRatingsDetailsActivity, 80f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        activityBinding.detailsBottomAppBar.apply {
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
            
            @SuppressLint("SetTextI18n")
            activityBinding.detailsInstalledVersion.text = "${app.installedVersion} (${app.installedBuild})"
            
            activityBinding.totalRatingsCount.isVisible = false
            
            setInstalledFromTextViewStyle(this@MyRatingsDetailsActivity,
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
            
            navController.navigate(R.id.myRatingsDetailsFragment)
            activityBinding.rateBtn.isVisible = false
        }
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_activity_details, menu)
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.details_menu_help -> HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
            R.id.menu_sort_user_ratings -> SortMyRatingsDetailsBottomSheet().show(supportFragmentManager, "SortMyRatingsBottomSheet")
            R.id.menu_more ->
                MoreOptionsBottomSheet(app.name, packageNameString,
                                       mapScoreRangeToStatusString(this@MyRatingsDetailsActivity, app.dgScore),
                                       mapScoreRangeToStatusString(this@MyRatingsDetailsActivity, app.mgScore),
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