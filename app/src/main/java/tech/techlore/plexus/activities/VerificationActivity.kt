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
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.platform.MaterialSharedAxis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.common.ExceptionErrorBottomSheet
import tech.techlore.plexus.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.databinding.ActivityVerificationBinding
import tech.techlore.plexus.models.post.device.RegisterDevice
import tech.techlore.plexus.models.post.device.VerifyDevice
import tech.techlore.plexus.objects.AppState
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ID
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_TOKEN
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.IS_REGISTERED
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.TextUtils.Companion.hasBlockedWord
import tech.techlore.plexus.utils.TextUtils.Companion.isEmail
import tech.techlore.plexus.utils.TextUtils.Companion.hasEmojis
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

class VerificationActivity : AppCompatActivity() {
    
    private lateinit var activityBinding: ActivityVerificationBinding
    private var isCodeReceived = false
    private var emailString = ""
    private var deviceId = ""
    private val fadeInAnim by lazy {
        AnimationUtils.loadAnimation(
            this,
            android.R.anim.fade_in)
    }
    private val fadeOutAnim by lazy {
        AnimationUtils.loadAnimation(
            this,
            android.R.anim.fade_out)
    }
    private val sixteenDpToPx by lazy {
        convertDpToPx(this, 16f)
    }
    private val tenDpToPx by lazy {
        convertDpToPx(this, 10f)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.apply {
            setNavBarContrastEnforced()
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        var job: Job? = null
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.verifNestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom + convertDpToPx(this, 64f)
                // Docked toolbar height = 64dp
            )
            WindowInsetsCompat.CONSUMED
        }
        // Prevent docked toolbar above the keyboard, when keyboard is visible
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.verifDockedToolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(
                left = insets.left + sixteenDpToPx,
                right = insets.right + sixteenDpToPx,
                bottom = insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        
        activityBinding.emailEditText.doOnTextChanged { charSequence, _, _, _ ->
            job?.cancel()
            job =
                lifecycleScope.launch {
                    delay(300.milliseconds)
                    if (!charSequence.isNullOrEmpty()
                        && !charSequence.hasBlockedWord(this@VerificationActivity)
                        && !charSequence.hasEmojis()
                        && charSequence.isEmail()) {
                        emailString = charSequence.toString()
                        activityBinding.proceedBtn.isEnabled = true
                    }
                }
        }
        
        activityBinding.codeEditText.doOnTextChanged { charSequence, _, _, _ ->
            job?.cancel()
            job =
                lifecycleScope.launch {
                    activityBinding.proceedBtn.isEnabled =
                        !charSequence.isNullOrEmpty()
                        && charSequence.isDigitsOnly()
                        && charSequence.length == 6
                }
        }
        
        // Proceed
        activityBinding.proceedBtn.apply {
            setOnClickListener {
                isEnabled = false
                activityBinding.emailEditText.clearFocus()
                registerDevice()
            }
        }
        
        // Cancel
        activityBinding.verifCancelBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        activityBinding.verifPrivacyPolicyBtn.setOnClickListener {
            openURL(getString(R.string.plexus_privacy_policy_url),
                    activityBinding.verificationCoordLayout,
                    activityBinding.verifDockedToolbar)
        }
    }
    
    private fun registerDevice() {
        activityBinding.progressIndicator.show()
        lifecycleScope.launch {
            if (hasInternet(this@VerificationActivity)) {
                try {
                    if (!isCodeReceived) requestCode() else verifyCode()
                }
                catch (e: Exception) {
                    activityBinding.progressIndicator.hide()
                    ExceptionErrorBottomSheet(
                        exception = e,
                        negativeBtnText = getString(R.string.cancel),
                        onPositiveBtnClick = { registerDevice() },
                        onNegativeBtnClick = { finishAfterTransition() }
                    ).show(supportFragmentManager, "ExceptionErrorBottomSheet")
                }
            }
            else {
                activityBinding.progressIndicator.hide()
                NoNetworkBottomSheet(
                    negativeBtnText = getString(R.string.cancel),
                    onPositiveBtnClick = { registerDevice() },
                    onNegativeBtnClick = {}
                ).show(supportFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
    private suspend fun requestCode() {
        val randomId = Uuid.random().toString()
        val requestCodeResponse =
            get<ApiRepository>().registerDevice(
                RegisterDevice(email = emailString, deviceId = randomId)
            )
        
        if (!requestCodeResponse.message.startsWith("Passcode sent")) {
            throw Exception(getString(R.string.error_sending_code))
        }
        
        deviceId = randomId
        afterCodeReceived()
    }
    
    private fun afterCodeReceived() {
        isCodeReceived = true
        activityBinding.progressIndicator.hide()
        
        activityBinding.codeTitleTextView.text =
            getString(R.string.enter_code_sent_to_email, emailString)
        
        arrayOf(activityBinding.titleTextViewSwitcher, activityBinding.editTextViewSwitcher)
            .forEach {
                it.apply {
                    inAnimation = fadeInAnim
                    outAnimation = fadeOutAnim
                    layoutParams =
                        (layoutParams as ViewGroup.MarginLayoutParams).apply {
                            topMargin = tenDpToPx
                        }
                    showNext()
                }
            }
        
        activityBinding.codeEditText.requestFocus()
    }
    
    private suspend fun verifyCode() {
        val verifyCodeResponse =
            get<ApiRepository>().verifyDevice(
                VerifyDevice(
                    deviceId = deviceId,
                    code = activityBinding.codeEditText.text.toString()
                )
            )
        verifyCodeResponse.deviceTokenData?.let {
            get<EncryptedPreferenceManager>().apply {
                setString(DEVICE_TOKEN, it.token)
                setString(DEVICE_ID, deviceId)
                setBoolean(IS_REGISTERED, true)
            }
            AppState.isVerificationSuccessful = true
            finishAfterTransition()
        } ?: throw Exception(verifyCodeResponse.errors?.errorDetail ?: getString(R.string.error_sending_code))
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAfterTransition()
        }
    }
}