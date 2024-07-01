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

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivitySettingsBinding
import tech.techlore.plexus.preferences.PreferenceManager.Companion.IS_FIRST_LAUNCH

class SettingsActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivitySettingsBinding
    private lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        navHostFragment = supportFragmentManager.findFragmentById(R.id.settingsNavHost) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.settings_fragments_nav_graph)
        
        activityBinding.toolbarBottom.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        navGraph.setStartDestination(intent.extras?.getInt("fragId")!!)
        navController.setGraph(navGraph, intent.extras)
    }
    
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            
            val preferenceManager = (applicationContext as ApplicationManager).preferenceManager
            
            when {
                preferenceManager.getBoolean(IS_FIRST_LAUNCH) -> {
                    preferenceManager.setBoolean(IS_FIRST_LAUNCH, false)
                    startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
                    finish()
                    overridePendingTransition(R.anim.slide_from_start, R.anim.slide_to_end)
                }
                
                navController.currentDestination?.id == R.id.licensesFragment -> {
                    navController.navigate(R.id.action_licensesFragment_to_settingsFragment)
                }
                
                navController.currentDestination?.id == R.id.supportUsFragment -> {
                    navController.navigate(R.id.action_supportUsFragment_to_settingsFragment)
                }
                
                else -> finish()
            }
            
        }
    }
    
}