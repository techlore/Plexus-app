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

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.fragments.bottomsheets.FirstSubmissionBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.MoreOptionsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.SortUserRatingsBottomSheet
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.ratings.Ratings
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.FIRST_SUBMISSION
import kotlin.coroutines.CoroutineContext
import com.bumptech.glide.RequestBuilder as RequestBuilder1

class AppDetailsActivity : AppCompatActivity(), MenuProvider, CoroutineScope {
    
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private lateinit var activityBinding: ActivityAppDetailsBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    lateinit var app: MainData
    lateinit var ratingsList: ArrayList<Ratings>
    lateinit var differentVersionsList: List<String>
    var selectedVersionString: String? = null
    var statusRadio = R.id.user_ratings_radio_any_status
    var dgStatusSort = 0
    var mgStatusSort = 0
    
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        addMenuProvider(this)
        activityBinding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val preferenceManager = PreferenceManager(this)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.details_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        selectedVersionString = getString(R.string.any)
        val repository = (applicationContext as ApplicationManager).mainRepository
        val requestManager = Glide.with(applicationContext)
        val requestBuilder: RequestBuilder1<Drawable>
        
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.bottomAppBar)
        activityBinding.bottomAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    
        runBlocking {
            launch {
                app = intent.getStringExtra("packageName")?.let { repository.getAppByPackage(it) } !!
            }
        }
        
        // Icon
        if (!app.isInstalled) {
            requestBuilder =
                requestManager
                    .load("")
                    .placeholder(R.drawable.ic_apk)
                    .onlyRetrieveFromCache(true) // Image should always be in cache
                                                    // since it's loaded in Plexus data fragment
        }
        else {
            requestBuilder =
                try {
                    requestManager.load(packageManager.getApplicationIcon(app.packageName))
                }
                catch (e: PackageManager.NameNotFoundException) {
                    throw RuntimeException(e)
                }
        }
        
        requestBuilder.into(activityBinding.detailsAppIcon)
        activityBinding.detailsName.text = app.name
        activityBinding.detailsPackageName.text = app.packageName
        activityBinding.detailsInstalledVersion.text = "${getString(R.string.installed)}: " +
                                                       app.installedVersion.ifEmpty { getString(R.string.not_tested_title) }
        
        // Radio group/buttons
        activityBinding.detailsRadiogroup.setOnCheckedChangeListener{_, checkedId: Int ->
            displayFragment(checkedId)
        }
        
        // FAB
        if (!app.isInstalled){
            activityBinding.fab.visibility = View.GONE
        }
        else {
            activityBinding.fab.setOnClickListener {
                if (preferenceManager.getBoolean(FIRST_SUBMISSION)) {
                    FirstSubmissionBottomSheet(positiveButtonClickListener = { startSubmitActivity() })
                        .show(supportFragmentManager, "FirstSubmissionBottomSheet")
                }
                else {
                    startSubmitActivity()
                }
            }
        }
    
        // Get different versions from ratings list
        // and store them in a separate list
        ratingsList = app.ratingsList
        val uniqueVersions = HashSet<String>()
        for (ratings in ratingsList) {
            uniqueVersions.add(ratings.version!!)
        }
        differentVersionsList = listOf(getString(R.string.any)) + uniqueVersions.toList()
        
    }
    
    // Setup fragments
    private fun displayFragment(checkedItem: Int) {
        val currentFragment = navController.currentDestination!!
        
        val action: Int =
            when (checkedItem) {
                
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
            navController.currentDestination!!.id != navController.graph.startDestinationId
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            
            R.id.menu_help -> startActivity(Intent(this@AppDetailsActivity, SettingsActivity::class.java)
                                                .putExtra("frag", R.id.helpFragment))
            
            R.id.menu_sort_user_ratings -> SortUserRatingsBottomSheet().show(supportFragmentManager, "SortUserRatingsBottomSheet")
            
            R.id.menu_more ->
                MoreOptionsBottomSheet(app.name, app.packageName,  /*plexusData.version,
                                 plexusData.dgStatus, plexusData.mgStatus,
                                 plexusData.dgNotes, plexusData.mgNotes,*/
                                       activityBinding.appDetailsCoordinatorLayout,
                                       activityBinding.bottomAppBar)
                    .show(supportFragmentManager, "MoreOptionsBottomSheet")
            
        }
        
        return true
    }
    
    private fun startSubmitActivity() {
        Toast.makeText(this@AppDetailsActivity, "Success", Toast.LENGTH_SHORT).show()
        /*val intent =
                        Intent(this@AppDetailsActivity, SubmitActivity::class.java)
                            .putExtra("name", app.name)
                            .putExtra("packageName", app.packageName)
                    
                    if (preferenceManager.getBoolean(DEVICE_IS_MICROG)) {
                        //intent.putExtra("", app.)
                    }
                    
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement)*/
    }
    
    override fun finish() {
        super.finish()
        job.cancel()
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() { finish() }
    }
}