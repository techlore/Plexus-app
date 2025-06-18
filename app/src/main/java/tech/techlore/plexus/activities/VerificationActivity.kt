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

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityVerificationBinding
import tech.techlore.plexus.utils.UiUtils.Companion.overrideTransition
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced

class VerificationActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityVerificationBinding
    lateinit var navController: NavController
    var emailString = ""
    var deviceId = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.setNavBarContrastEnforced()
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
    
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.verificationNavHost) as NavHostFragment
        navController = navHostFragment.navController
        
        // Back
        activityBinding.verificationBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
            overrideTransition(isClosingTransition = true,
                               enterAnim = 0,
                               exitAnim = R.anim.fade_out_slide_to_bottom)
        }
    }
}