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
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivitySubmitBinding
import tech.techlore.plexus.fragments.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.submit.ConfirmSubmitBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.submit.SubmitBottomSheet
import tech.techlore.plexus.preferences.PreferenceManager.Companion.CONF_BEFORE_SUBMIT
import tech.techlore.plexus.utils.IntentUtils.Companion.startDetailsActivity
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.TextUtils.Companion.hasBlockedWord
import tech.techlore.plexus.utils.TextUtils.Companion.hasEmojis
import tech.techlore.plexus.utils.TextUtils.Companion.hasRepeatedChars
import tech.techlore.plexus.utils.TextUtils.Companion.hasURL
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromTextViewStyle

class SubmitActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivitySubmitBinding
    lateinit var nameString: String
    lateinit var packageNameString: String
    lateinit var installedVersion: String
    var installedBuild = 0
    lateinit var installedFromString: String
    var isInPlexusData = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivitySubmitBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        nameString = intent.getStringExtra("name")!!
        packageNameString = intent.getStringExtra("packageName")!!
        installedVersion = intent.getStringExtra("installedVersion")!!
        installedBuild = intent.getIntExtra("installedBuild", 0)
        installedFromString = intent.getStringExtra("installedFrom")!!
        isInPlexusData = intent.getBooleanExtra("isInPlexusData", true)
        val appManager = applicationContext as ApplicationManager
        var job: Job? = null
        
        activityBinding.submitBottomAppBar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        
        // Icon
        try{
            activityBinding.submitAppIcon.setImageDrawable(packageManager.getApplicationIcon(packageNameString))
        }
        catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        
        activityBinding.submitName.text = nameString
        activityBinding.submitPackageName.text = packageNameString
        @SuppressLint("SetTextI18n")
        activityBinding.submitInstalledVersion.text = "${getString(R.string.installed)}: $installedVersion (${installedBuild})"
        
        setInstalledFromTextViewStyle(this@SubmitActivity,
                                      installedFromString,
                                      activityBinding.submitInstalledFrom)
        
        activityBinding.dgMgText.apply {
            val (statusIcon, statusText) =
                if (appManager.isDeviceMicroG) {
                    Pair(ContextCompat.getDrawable(context, R.drawable.ic_microg),
                         "${getString(R.string.microG)} ${getString(R.string.status)}")
                }
                else {
                    Pair(ContextCompat.getDrawable(context, R.drawable.ic_degoogled),
                         "${getString(R.string.de_Googled)} ${getString(R.string.status)}")
                }
            setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null)
            text = statusText
        }
        
        // Notes
        activityBinding.submitNotesBox.hint = "${getString(R.string.notes)} (${getString(R.string.optional)})"
        activityBinding.submitNotesText.doOnTextChanged { charSequence, _, _, _ ->
            job?.cancel()
            job = lifecycleScope.launch {
                delay(300)
                activityBinding.submitFab.isEnabled =
                    charSequence!!.isEmpty()
                    || (charSequence.length in 5..300
                        && !hasBlockedWord(this@SubmitActivity, charSequence)
                        && !hasRepeatedChars(charSequence)
                        && !hasEmojis(charSequence)
                        && !hasURL(charSequence))
            }
        }
        
        // FAB
        activityBinding.submitFab.setOnClickListener {
            if ((applicationContext as ApplicationManager).preferenceManager.getBoolean(CONF_BEFORE_SUBMIT, defValue = false)) {
                ConfirmSubmitBottomSheet().show(supportFragmentManager, "ConfirmSubmitBottomSheet")
            }
            else {
                showSubmitBtmSheet()
            }
        }
    }
    
    fun showSubmitBtmSheet() {
        lifecycleScope.launch {
            if (hasNetwork(this@SubmitActivity) && hasInternet()) {
                activityBinding.submitFab.isEnabled = false
                SubmitBottomSheet().show(supportFragmentManager, "SubmitBottomSheet")
            }
            else {
                NoNetworkBottomSheet(negativeButtonText = getString(R.string.cancel),
                                     positiveButtonClickListener = { showSubmitBtmSheet() },
                                     negativeButtonClickListener = {})
                    .show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    override fun finish() {
        super.finish()
        startDetailsActivity(this@SubmitActivity, packageNameString)
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
    }
    
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
}