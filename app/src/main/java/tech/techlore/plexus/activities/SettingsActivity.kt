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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivitySettingsBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.FIRST_LAUNCH

class SettingsActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivitySettingsBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.settings_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        val displayFragmentId = intent.extras?.getInt("frag")!!
        
        /*####################################################################################*/
        
        setSupportActionBar(activityBinding.toolbarBottom)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        activityBinding.toolbarBottom.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        activityBinding.helpRadioBottomAppbar.isVisible = displayFragmentId == R.id.helpVideosFragment
        
        navController.navigate(displayFragmentId)
        
        activityBinding.helpRadiogroup.apply {
            isVisible = displayFragmentId == R.id.helpVideosFragment
            if (isVisible) {
                check(R.id.radio_help_videos)
                setOnCheckedChangeListener { _, checkedId ->
                    val action =
                        when (checkedId) {
                            R.id.radio_help_text -> R.id.action_helpVideosFragment_to_helpTextFragment
                            else -> R.id.action_helpTextFragment_to_helpVideosFragment
                        }
                    navController.navigate(action)
                }
            }
        }
    }
    
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            
            val preferenceManager = PreferenceManager(this@SettingsActivity)
            
            if (preferenceManager.getBoolean(FIRST_LAUNCH)) {
                preferenceManager.setBoolean(FIRST_LAUNCH, false)
                startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
                finish()
                overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
            }
            else if (navController.currentDestination?.id == R.id.licensesFragment) {
                navController.navigate(R.id.action_licensesFragment_to_aboutFragment)
            }
            else if (navController.currentDestination?.id == R.id.supportUsFragment) {
                navController.navigate(R.id.action_supportUsFragment_to_aboutFragment)
            }
            else {
                finish()
            }
        }
    }
    
}