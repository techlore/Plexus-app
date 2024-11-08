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

import android.animation.Animator
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivityFirstBinding
import tech.techlore.plexus.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.objects.DeviceState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.IS_FIRST_LAUNCH
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MATERIAL_YOU
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced

class FirstActivity : AppCompatActivity() {
    
    private lateinit var activityBinding: ActivityFirstBinding
    private val prefManager by inject<PreferenceManager>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        // Material you
        // set this here instead of in Application class,
        // or else Dynamic Colors will not be applied to this activity
        if (prefManager.getBoolean (MATERIAL_YOU, defValue = false)) {
            DynamicColors.applyToActivityIfAvailable(this)
            DynamicColors.applyToActivitiesIfAvailable(applicationContext as ApplicationManager) // For other activities
        }
        enableEdgeToEdge()
        setNavBarContrastEnforced(window)
        super.onCreate(savedInstanceState)
        activityBinding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        // Adjust root layout for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
        
        retrieveData()
    }
    
    private fun retrieveData() {
        lifecycleScope.launch {
            if (hasNetwork(this@FirstActivity) && hasInternet()) {
                try {
                    get<MainDataRepository>().apply {
                        plexusDataIntoDB()
                        activityBinding.progressText.text = getString(R.string.scan_installed)
                        installedAppsIntoDB(this@FirstActivity)
                    }
                    isDeviceDeGoogledOrMicroG()
                    afterDataRetrieved()
                }
                catch (e: Exception) {
                    NoNetworkBottomSheet(isNoNetworkError = false,
                                         exception = e,
                                         negativeButtonText = getString(R.string.exit),
                                         positiveButtonClickListener = { retrieveData() },
                                         negativeButtonClickListener = { finishAndRemoveTask() })
                        .show(supportFragmentManager, "NoNetworkBottomSheet")
                }
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.exit),
                                     positiveButtonClickListener = { retrieveData() },
                                     negativeButtonClickListener = { finishAndRemoveTask() })
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    private suspend fun isDeviceDeGoogledOrMicroG() {
        val gappsPackages = arrayOf("com.google.android.gms",
                                    "com.google.android.gsf",
                                    "com.android.vending")
        
        var microGCount = 0
        var installedGappsCount = 0
        
        withContext(Dispatchers.IO) {
            gappsPackages.forEach {
                getAppInfo(packageManager, it)?.let { appInfo ->
                    installedGappsCount ++
                    if (!packageManager.getApplicationLabel(appInfo)
                            .startsWith("Google", ignoreCase = true))
                        microGCount ++
                }
            }
            
            DeviceState.apply {
                isDeviceMicroG = installedGappsCount == microGCount
                isDeviceDeGoogled = installedGappsCount == 0
            }
        }
    }
    
    private fun getAppInfo(packageManager: PackageManager, packageName: String): ApplicationInfo? {
        return try {
            packageManager.getApplicationInfo(packageName, 0)
        }
        catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    private fun afterDataRetrieved() {
        prefManager.apply {
            if (getBoolean(IS_FIRST_LAUNCH)) {
                val fadeOut = AlphaAnimation(1.0f, 0.0f).apply { duration = 300L }
                activityBinding.apply {
                    firstLoadingIndicator.apply {
                        isVisible = false
                        startAnimation(fadeOut)
                    }
                    progressText.apply {
                        isVisible = false
                        startAnimation(fadeOut)
                    }
                    helloAnimView.apply {
                        setMaxFrame(300)
                        addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}
                            
                            override fun onAnimationEnd(animation: Animator) {
                                val fadeIn = AlphaAnimation(0.5f, 1.0f).apply { duration = 500L }
                                progressText.apply {
                                    text = getString(R.string.welcome_text_desc)
                                    isVisible = true
                                    startAnimation(fadeIn)
                                }
                                firstSkipBtn.apply {
                                    isVisible = true
                                    startAnimation(fadeIn)
                                    setOnClickListener {
                                        setBoolean(IS_FIRST_LAUNCH, false)
                                        finish()
                                    }
                                }
                                firstProceedBtn.apply {
                                    isVisible = true
                                    startAnimation(fadeIn)
                                    setOnClickListener {
                                        HelpBottomSheet().show(supportFragmentManager, "HelpBottomSheet")
                                    }
                                }
                            }
                            
                            override fun onAnimationCancel(animation: Animator) {}
                            
                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                        
                        isVisible = true
                        playAnimation()
                    }
                }
            }
            
            else finish()
        }
    }
    
    override fun finish() {
        super.finish()
        startActivity(Intent(this@FirstActivity, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}