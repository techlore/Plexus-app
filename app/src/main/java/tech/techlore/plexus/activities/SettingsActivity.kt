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

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivitySettingsBinding

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
        
        /*####################################################################################*/
        setSupportActionBar(activityBinding.toolbarBottom)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        activityBinding.toolbarBottom.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        intent.extras?.let {
            navController.navigate(it.getInt("frag"))
        }
    }
    
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navController.currentDestination?.id == R.id.licensesFragment) {
                navController.navigate(R.id.action_licensesFragment_to_aboutFragment)
            }
            else {
                finish()
            }
        }
    }

}