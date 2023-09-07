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
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivitySplashBinding
import tech.techlore.plexus.fragments.bottomsheets.FirstLaunchBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.NoNetworkBottomSheet
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.IS_FIRST_LAUNCH
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.SystemUtils.Companion.isDeviceDeGoogledOrMicroG

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        retrieveData()
    }
    
    private fun retrieveData() {
        lifecycleScope.launch{
            val context = this@SplashActivity
            if (hasNetwork(context) && hasInternet()) {
                val mainRepository = (applicationContext as ApplicationManager).mainRepository
                mainRepository.plexusDataIntoDB(context)
                activityBinding.progressText.text = getString(R.string.scan_installed)
                mainRepository.installedAppsIntoDB(context)
                isDeviceDeGoogledOrMicroG(context)
                afterDataRetrieved()
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.exit),
                                     positiveButtonClickListener = { retrieveData() },
                                     negativeButtonClickListener = { finishAndRemoveTask() })
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    private fun afterDataRetrieved() {
        if (PreferenceManager(this@SplashActivity).getBoolean(IS_FIRST_LAUNCH)) {
            FirstLaunchBottomSheet().show(supportFragmentManager, "FirstLaunchBottomSheet")
        }
        else {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
        activityBinding.progressIndicator.apply {
            pauseAnimation()
            isVisible = false
        }
        activityBinding.progressText.isVisible = false
    }
    
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_from_end, R.anim.slide_to_start)
    }
}