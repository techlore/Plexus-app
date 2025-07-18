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
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivityFirstBinding
import tech.techlore.plexus.bottomsheets.common.HelpBottomSheet
import tech.techlore.plexus.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.objects.AppState
import tech.techlore.plexus.objects.DeviceState
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.IS_FIRST_LAUNCH
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MATERIAL_YOU
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.utils.DeviceUtils.Companion.isDeviceDeGoogledOrMicroG
import tech.techlore.plexus.utils.IntentUtils.Companion.startActivityWithTransition
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.hideViewWithAnim
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced
import tech.techlore.plexus.utils.UiUtils.Companion.showViewWithAnim
import kotlin.system.exitProcess

class FirstActivity : AppCompatActivity() {
    
    private lateinit var activityBinding: ActivityFirstBinding
    private val prefManager by inject<PreferenceManager>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        // Set Material You here instead of in Application class,
        // or else Dynamic Colors will not be applied to this activity
        if (prefManager.getBoolean (MATERIAL_YOU, defValue = false)) {
            DynamicColors.applyToActivityIfAvailable(this)
            DynamicColors.applyToActivitiesIfAvailable(applicationContext as ApplicationManager) // For other activities
        }
        enableEdgeToEdge()
        window.setNavBarContrastEnforced()
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
        
        AppState.isAppOpen = true
        
        DeviceState.apply {
            rom = get<EncryptedPreferenceManager>().getString(DEVICE_ROM).orEmpty()
            androidVersion = getAndroidVersionString()
        }
        
        retrieveData()
    }
    
    private fun getAndroidVersionString(): String {
        return when(Build.VERSION.SDK_INT) {
            23 -> "6.0"
            24 -> "7.0"
            25 -> "7.1"
            26 -> "8.0"
            27 -> "8.1"
            28 -> "9.0"
            29 -> "10.0"
            30 -> "11.0"
            31 -> "12.0"
            32 -> "12.1"
            33 -> "13.0"
            34 -> "14.0"
            35 -> "15.0"
            36 -> "16.0"
            else -> "NA" // Should never reach here
        }
    }
    
    private fun retrieveData() {
        lifecycleScope.launch {
            if (hasNetwork(this@FirstActivity) && hasInternet()) {
                try {
                    get<MainDataRepository>().apply {
                        plexusDataIntoDB()
                        deleteNonRatedAppsFromDb()
                        activityBinding.progressText.text = getString(R.string.scan_installed)
                        installedAppsIntoDB(this@FirstActivity)
                    }
                    isDeviceDeGoogledOrMicroG(packageManager)
                    afterDataRetrieved()
                }
                catch (e: Exception) {
                    NoNetworkBottomSheet(isNoNetworkError = false,
                                         exception = e,
                                         negativeButtonText = getString(R.string.exit),
                                         positiveBtnClickAction = { retrieveData() },
                                         negativeBtnClickAction = {
                                             finishAndRemoveTask()
                                             exitProcess(0)
                                         })
                        .show(supportFragmentManager, "NoNetworkBottomSheet")
                }
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.exit),
                                     positiveBtnClickAction = { retrieveData() },
                                     negativeBtnClickAction = {
                                         finishAndRemoveTask()
                                         exitProcess(0)
                                     })
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    private fun afterDataRetrieved() {
        if (prefManager.getBoolean(IS_FIRST_LAUNCH)) {
            activityBinding.apply {
                firstLoadingIndicator.hideViewWithAnim()
                progressText.hideViewWithAnim()
                helloAnimView.apply {
                    setMaxFrame(300)
                    addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        
                        override fun onAnimationEnd(animation: Animator) {
                            progressText.apply {
                                text = getString(R.string.welcome_text_desc)
                                showViewWithAnim(shouldScaleUp = true, setStartScaleValues = true)
                            }
                            firstSkipBtn.apply {
                                showViewWithAnim()
                                setOnClickListener {
                                    prefManager.setBoolean(IS_FIRST_LAUNCH, false)
                                    finishAfterTransition()
                                }
                            }
                            firstProceedBtn.apply {
                                showViewWithAnim()
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
        
        else finishAfterTransition()
    }
    
    override fun finishAfterTransition() {
        super.finishAfterTransition()
        // If started from shortcut, open details activity
        // else main activity
        intent.getStringExtra("packageName")?.let {
            startDetailsActivity(it, isFromShortcut = true)
        }
        ?: startActivityWithTransition(Intent(this@FirstActivity, MainActivity::class.java))
    }
}