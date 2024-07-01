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

package tech.techlore.plexus.fragments.bottomsheets.verification

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.VerificationActivity
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.BottomSheetEmailVerificationBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.models.get.responses.RegisterDeviceResponse
import tech.techlore.plexus.models.post.device.RegisterDevice
import java.util.UUID

class EmailVerificationBottomSheet(private val email: String) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetEmailVerificationBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var footerBinding: BottomSheetFooterBinding
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setCanceledOnTouchOutside(false)
            behavior.isDraggable = false
            
            // Prevent bottom sheet dismiss on back pressed
            setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // Do nothing
                    return@OnKeyListener true
                }
                false
            })
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetEmailVerificationBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        registerDevice()
        
        // Retry
        footerBinding.positiveButton.apply {
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_retry)
            text = getString(R.string.retry)
            isEnabled = false
            setOnClickListener {
                isEnabled = false
                bottomSheetBinding.textView.text = getString(R.string.sending_code)
                registerDevice()
            }
        }
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener {
            dismiss()
        }
    }
    
    private fun registerDevice() {
        val apiRepository = (requireContext().applicationContext as ApplicationManager).apiRepository
        val randomId = UUID.randomUUID().toString()
        val registerDeviceCall = apiRepository.registerDevice(RegisterDevice(email = email,
                                                                             deviceId = randomId))
        registerDeviceCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val registerDeviceResponse =
                        response.body()?.string()?.let {
                            jacksonObjectMapper().readValue(it, RegisterDeviceResponse::class.java)
                        }
                    if (registerDeviceResponse?.message?.startsWith("Passcode sent") == true) {
                        dismiss()
                        (requireActivity() as VerificationActivity).apply {
                            emailString = email
                            deviceId = randomId
                            navController.navigate(R.id.action_emailVerificationFragment_to_codeVerificationFragment)
                        }
                    }
                    else {
                        onPostFailed(getString(R.string.error_sending_code))
                    }
                }
                else {
                    onPostFailed("${getString(R.string.error_sending_code)}: ${response.code()}")
                }
            }
            
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onPostFailed(getString(R.string.error_sending_code))
            }
        })
    }
    
    private fun onPostFailed(message: String) {
        bottomSheetBinding.textView.text = message
        footerBinding.apply {
            positiveButton.isEnabled = true
            negativeButton.isEnabled = true
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}