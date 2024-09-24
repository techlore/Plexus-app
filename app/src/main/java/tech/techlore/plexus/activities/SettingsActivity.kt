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
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import tech.techlore.plexus.BuildConfig
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivitySettingsBinding
import tech.techlore.plexus.fragments.bottomsheets.settings.DefaultViewBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.settings.LicensesBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.settings.SupportUsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.settings.ThemeBottomSheet
import tech.techlore.plexus.preferences.PreferenceManager.Companion.CONF_BEFORE_SUBMIT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.IS_FIRST_LAUNCH
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MATERIAL_YOU
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var activityBinding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val preferenceManager = (applicationContext as ApplicationManager).preferenceManager
        
        // Adjust scrollview for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.settingsScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left,
                            top = insets.top,
                            right = insets.right)
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + convertDpToPx(this@SettingsActivity, 80f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        activityBinding.settingsBottomAppBar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        
        // Version
        @SuppressLint("SetTextI18n")
        activityBinding.aboutVersion.text = "${getString(R.string.version)}: ${BuildConfig.VERSION_NAME}"
        
        // Theme
        activityBinding.theme.setOnClickListener {
            ThemeBottomSheet().show(supportFragmentManager, "ThemeBottomSheet")
        }
        
        // Material You
        activityBinding.materialYouSwitch.apply {
            if (Build.VERSION.SDK_INT >= 31) {
                isVisible = true
                isChecked = preferenceManager.getBoolean(MATERIAL_YOU, defValue = false)
                setOnCheckedChangeListener { _, isChecked ->
                    preferenceManager.setBoolean(MATERIAL_YOU, isChecked)
                }
            }
        }
        
        // Default view
        activityBinding.defaultView.setOnClickListener {
            DefaultViewBottomSheet().show(supportFragmentManager, "DefaultViewBottomSheet")
        }
        
        // Confirm before submitting
        activityBinding.confBeforeSubmitSwitch.apply {
            isChecked = preferenceManager.getBoolean(CONF_BEFORE_SUBMIT, defValue = false)
            setOnCheckedChangeListener { _, isChecked ->
                preferenceManager.setBoolean(CONF_BEFORE_SUBMIT, isChecked)
            }
        }
        
        // Privacy policy
        activityBinding.privacyPolicy.setOnClickListener {
            openURL(this,
                    getString(R.string.plexus_privacy_policy_url),
                    activityBinding.settingsCoordLayout,
                    activityBinding.settingsBottomAppBar)
        }
        
        // Report an issue
        activityBinding.reportIssue.setOnClickListener {
            openURL(this,
                    getString(R.string.plexus_report_issue_url),
                    activityBinding.settingsCoordLayout,
                    activityBinding.settingsBottomAppBar)
        }
        
        // Support us
        activityBinding.supportUs.setOnClickListener {
            SupportUsBottomSheet().show(supportFragmentManager, "SupportUsBottomSheet")
        }
        
        // View on GitHub
        activityBinding.viewOnGitHub.setOnClickListener {
            openURL(this,
                    getString(R.string.plexus_github_url),
                    activityBinding.settingsCoordLayout,
                    activityBinding.settingsBottomAppBar)
        }
        
        // Visit Techlore
        activityBinding.visitTechlore.setOnClickListener {
            openURL(this,
                    getString(R.string.techlore_website_url),
                    activityBinding.settingsCoordLayout,
                    activityBinding.settingsBottomAppBar)
        }
        
        // Third party licenses
        activityBinding.licenses.setOnClickListener {
            LicensesBottomSheet().show(supportFragmentManager, "LicensesBottomSheet")
        }
        
        
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
                
                else -> finish()
            }
            
        }
    }
    
}