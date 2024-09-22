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

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityVerificationBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity

class VerificationActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityVerificationBinding
    lateinit var navController: NavController
    lateinit var nameString: String
    lateinit var packageNameString: String
    lateinit var installedVersionString: String
    var installedBuild = 0
    lateinit var installedFromString: String
    var isInPlexusData = true
    var emailString = ""
    var deviceId = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
    
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.verificationNavHost) as NavHostFragment
        navController = navHostFragment.navController
        nameString = intent.getStringExtra("name")!!
        packageNameString = intent.getStringExtra("packageName")!!
        installedVersionString = intent.getStringExtra("installedVersion")!!
        installedBuild = intent.getIntExtra("installedBuild", 0)
        installedFromString = intent.getStringExtra("installedFrom")!!
        isInPlexusData = intent.getBooleanExtra("isInPlexusData", true)
    
        activityBinding.verificationBottomAppBar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            startDetailsActivity(this@VerificationActivity, packageNameString)
            overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
            finish()
        }
    }
}