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

package tech.techlore.plexus.fragments.verification

import android.annotation.SuppressLint
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.VerificationActivity
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.FragmentCodeVerificationBinding
import tech.techlore.plexus.models.get.responses.VerifyDeviceResponseRoot
import tech.techlore.plexus.models.post.device.VerifyDevice
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_TOKEN
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class CodeVerificationFragment : Fragment() {
    
    private var _binding: FragmentCodeVerificationBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var verificationActivity: VerificationActivity
    private var resendCodeTimer: CountDownTimer? = null
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentCodeVerificationBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        verificationActivity = (requireActivity() as VerificationActivity)
        var clickedTimes = 0
        val inputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        
        // Title
        fragmentBinding.titleText.text = getString(R.string.enter_code_sent_to_email,
                                                   verificationActivity.emailString)
        
        // Edit text
        fragmentBinding.codeText.addTextChangedListener(object : TextWatcher {
            
            var delayTimer: CountDownTimer? = null
            
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
    
                fragmentBinding.codeTextBox.error = null
    
                // Introduce a subtle delay
                // so text is checked after typing is finished
                delayTimer?.cancel()
                
                delayTimer = object : CountDownTimer(200, 100) {
                    
                    override fun onTick(millisUntilFinished: Long) {}
                    
                    // On timer finish, perform task
                    override fun onFinish() {
                        fragmentBinding.doneButton.isEnabled =
                            charSequence.isNotEmpty()
                            && charSequence.isDigitsOnly()
                            && charSequence.length == 6
                    }
                    
                }.start()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Done
        fragmentBinding.doneButton.apply {
            setOnClickListener {
                isEnabled = false
                
                fragmentBinding.infoText.apply {
                    isVisible = true
                    text = getString(R.string.verifying_code)
                }
                
                verifyDevice()
            }
        }
        
        // Resend code
        resendCodeCountdown(30000)
        fragmentBinding.resendButton.apply {
            setOnClickListener {
                clickedTimes += 1
        
                when (clickedTimes) {
                    1 -> {
                        isEnabled = false
                        fragmentBinding.infoText.isVisible = true
                        // TODO: Send code
                        fragmentBinding.infoText.isVisible = false
                        resendCodeCountdown(59000)
                        showSnackbar(verificationActivity.activityBinding.verificationCoordLayout,
                                     getString(R.string.code_sent, verificationActivity.emailString),
                                     verificationActivity.activityBinding.verificationToolbarBottom)
                    }
            
                    2 -> {
                        isEnabled = false
                        fragmentBinding.infoText.isVisible = true
                        // TODO: Send code
                        isEnabled = true
                        fragmentBinding.infoText.isVisible = false
                        showSnackbar(verificationActivity.activityBinding.verificationCoordLayout,
                                     getString(R.string.code_sent, verificationActivity.emailString),
                                     verificationActivity.activityBinding.verificationToolbarBottom)
                    }
            
                    else -> showSnackbar(verificationActivity.activityBinding.verificationCoordLayout,
                                         getString(R.string.try_after_2_hours),
                                         verificationActivity.activityBinding.verificationToolbarBottom)
                }
            }
        }
    }
    
    private fun resendCodeCountdown(duration: Long) {
        var remSecs: Int
        fragmentBinding.resendButton.apply {
            resendCodeTimer = object : CountDownTimer(duration, 1000) {
                
                override fun onTick(millisUntilFinished: Long) {
                    isEnabled = false
                    remSecs =
                        (millisUntilFinished / 1000).toInt() + 1 // Show 30s..1s instead of 29s..0s
                    @SuppressLint("SetTextI18n")
                    text = "${getString(R.string.resend_code)} ${remSecs}s"
                }
                
                override fun onFinish() {
                    text = getString(R.string.resend_code)
                    isEnabled = true
                    resendCodeTimer = null
                }
            }.start()
        }
    }
    
    private fun verifyDevice() {
        val apiRepository = (requireContext().applicationContext as ApplicationManager).apiRepository
        val verifyDeviceCall = apiRepository.verifyDevice(VerifyDevice(deviceId = verificationActivity.deviceId,
                                                                       code = fragmentBinding.codeText.text.toString()))
        verifyDeviceCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val verifyDeviceResponse =
                        response.body()?.string()?.let {
                            jacksonObjectMapper().readValue(it, VerifyDeviceResponseRoot::class.java)
                        }
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    EncryptedPreferenceManager(requireContext()).setString(DEVICE_TOKEN, verifyDeviceResponse?.deviceToken!!.token)
                    fragmentBinding.infoText.isVisible = false
                    /*if (inputMethodManager.isAcceptingText) {
                        inputMethodManager.hideSoftInputFromWindow(verificationActivity.currentFocus?.windowToken, 0)
                    }
                    EncryptedPreferenceManager(requireContext()).setBoolean(IS_REGISTERED, true)
                    startSubmitActivity(verificationActivity,
                                        verificationActivity.nameString,
                                        verificationActivity.packageNameString,
                                        verificationActivity.installedVersionString,
                                        verificationActivity.installedBuild,
                                        verificationActivity.installedFromString,
                                        verificationActivity.isInPlexusData)*/
                }
                else {
                    fragmentBinding.apply {
                        infoText.isVisible = false
                        fragmentBinding.doneButton.isEnabled = true
                        fragmentBinding.codeTextBox.error = getString(R.string.incorrect_code)
                    }
                }
            }
            
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                fragmentBinding.apply {
                    infoText.isVisible = false
                    fragmentBinding.doneButton.isEnabled = true
                    fragmentBinding.codeTextBox.error = getString(R.string.incorrect_code)
                }
            }
        })
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        resendCodeTimer?.cancel()
        _binding = null
    }
}