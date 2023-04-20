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

package tech.techlore.plexus.fragments.bottomsheets

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFirstSubmissionBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEVICE_IS_MICROG
import tech.techlore.plexus.preferences.PreferenceManager.Companion.FIRST_SUBMISSION

class FirstSubmissionBottomSheet(private val positiveButtonClickListener: () -> Unit) : BottomSheetDialogFragment() {
    
    private var timer: CountDownTimer? = null
    private var remSecs = 0
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        val bottomSheetBinding = BottomSheetFirstSubmissionBinding.inflate(inflater, container, false)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val preferenceManager = PreferenceManager(requireContext())
        
        headerBinding.bottomSheetTitle.visibility = View.GONE
        
        // Proceed
        footerBinding.positiveButton.text = getString(R.string.proceed)
        footerBinding.positiveButton.isEnabled = false
        timer = object : CountDownTimer(10000, 1000) {
            
            override fun onTick(millisUntilFinished: Long) {
                remSecs = (millisUntilFinished / 1000).toInt() + 1 // Show 10s..1s instead of 9s..0s
                val positiveBtnText = ("${getString(R.string.proceed)} ${remSecs}s")
                footerBinding.positiveButton.text = positiveBtnText
            }
            
            override fun onFinish() {
                footerBinding.positiveButton.isEnabled = true
                footerBinding.positiveButton.text = getString(R.string.proceed)
                timer = null
                footerBinding.positiveButton.setOnClickListener{
                    preferenceManager.setBoolean(FIRST_SUBMISSION, false)
                    if (bottomSheetBinding.dgMgRadiogroup.checkedRadioButtonId == R.id.dg_submit) {
                        preferenceManager.setBoolean(DEVICE_IS_MICROG, false)
                    }
                    dismiss()
                    positiveButtonClickListener.invoke()
                }
            }
        }.start()
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
        
        return bottomSheetBinding.root
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}