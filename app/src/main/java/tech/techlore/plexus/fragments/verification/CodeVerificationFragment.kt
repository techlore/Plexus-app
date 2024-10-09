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

package tech.techlore.plexus.fragments.verification

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.text.isDigitsOnly
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.VerificationActivity
import tech.techlore.plexus.databinding.FragmentCodeVerificationBinding
import tech.techlore.plexus.models.get.responses.VerifyDeviceResponseRoot
import tech.techlore.plexus.models.post.device.VerifyDevice
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ID
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_TOKEN
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.IS_REGISTERED
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx

class CodeVerificationFragment : Fragment() {
    
    private var _binding: FragmentCodeVerificationBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var verificationActivity: VerificationActivity
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentCodeVerificationBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        verificationActivity = (requireActivity() as VerificationActivity)
        var job: Job? = null
        
        // Adjust linear layout for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.codeVerificationLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left,
                            top = insets.top,
                            right = insets.right)
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 80f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        // Title
        fragmentBinding.titleText.text = getString(R.string.enter_code_sent_to_email,
                                                   verificationActivity.emailString)
        
        // Edit text
        fragmentBinding.codeText.doOnTextChanged { charSequence, _, _, _ ->
            job?.cancel()
            job =
                lifecycleScope.launch {
                    delay(200)
                    fragmentBinding.doneButton.isEnabled =
                        charSequence!!.isNotEmpty()
                        && charSequence.isDigitsOnly()
                }
        }
        
        // Done
        fragmentBinding.doneButton.setOnClickListener {
            showInfo(true)
            lifecycleScope.launch {
                verifyDevice()
            }
        }
    }
    
    private suspend fun verifyDevice() {
        val verifyDeviceCall =
            get<ApiRepository>().verifyDevice(VerifyDevice(deviceId = verificationActivity.deviceId,
                                                           code = fragmentBinding.codeText.text.toString()))
        val response = withContext(Dispatchers.IO) { verifyDeviceCall.execute() }
        
        if (response.isSuccessful) {
            val verifyDeviceResponse =
                response.body()?.string()?.let {
                    jacksonObjectMapper().readValue(it, VerifyDeviceResponseRoot::class.java)
                }
            get<EncryptedPreferenceManager>().apply {
                setString(DEVICE_TOKEN, verifyDeviceResponse?.deviceToken!!.token)
                setString(DEVICE_ID, verificationActivity.deviceId)
                setBoolean(IS_REGISTERED, true)
            }
            fragmentBinding.infoText.isVisible = false
            (requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                if (isAcceptingText) {
                    hideSoftInputFromWindow(verificationActivity.currentFocus?.windowToken, 0)
                }
            }
            requireActivity().finish()
        }
        else {
            showInfo(false)
        }
    }
    
    private fun showInfo(show: Boolean) {
        fragmentBinding.apply {
            doneButton.isEnabled = !show
            infoAnim.isVisible = show
            infoText.isVisible = show
            if (!show) codeTextBox.error = getString(R.string.incorrect_code)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}