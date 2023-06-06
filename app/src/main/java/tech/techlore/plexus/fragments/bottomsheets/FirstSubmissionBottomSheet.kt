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

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFirstSubmissionBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.preferences.PreferenceManager.Companion.FIRST_SUBMISSION
import tech.techlore.plexus.utils.SystemUtils.Companion.getSystemProperty

class FirstSubmissionBottomSheet(private val positiveButtonClickListener: () -> Unit) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetFirstSubmissionBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var headerBinding: BottomSheetHeaderBinding
    private lateinit var footerBinding: BottomSheetFooterBinding
    private var timer: CountDownTimer? = null
    private var remSecs = 0
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetFirstSubmissionBinding.inflate(inflater, container, false)
        headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val preferenceManager = PreferenceManager(requireContext())
    
        headerBinding.bottomSheetTitle.text = getString(R.string.new_submission)
    
        val customRomsList = resources.getStringArray(R.array.custom_roms)
        val truncatedCustomRomsList =
            customRomsList.map {
                // Remove "OS", "Project", "ROM" etc.
                // from words in the custom ROMs list
                it.removePrefix("Project")
                    .removeSuffix("OS")
                    .removeSuffix("Project")
                    .removeSuffix("ROM")
                    .replace(" ", "")
            }
    
        val allRomsDropdownList = listOf(getString(R.string.select)) +
                                  "Stock (${Build.MANUFACTURER} ${Build.MODEL})" +
                                  customRomsList
    
        val customRomsPattern = truncatedCustomRomsList.joinToString("|") { Regex.escape(it) }
        val customRomsRegex = "(?i)($customRomsPattern)".toRegex(setOf(RegexOption.IGNORE_CASE))
    
        // Check a few properties from build.prop
        // that might contain ROM name
        val buildPropValues =
            listOf("ro.build.flavor",
                   "ro.product.system.name",
                   "ro.build.user",
                   "ro.build.description",
                   "ro.build.fingerprint")
                .mapNotNull { getSystemProperty(it) }
    
        // Check if any value from build.prop matches against custom roms list
        val matchingValue = buildPropValues.firstOrNull { customRomsRegex.containsMatchIn(it) }
        val romNameIndex =
            truncatedCustomRomsList.indexOfFirst {
                matchingValue?.contains(it, ignoreCase = true) == true
            } // -1 would mean no matching index
    
        // ROM dropdown
        bottomSheetBinding.romDropdownMenu.setText(
            if (romNameIndex != -1) allRomsDropdownList[romNameIndex + 2] else allRomsDropdownList[0]
            // allRomsDropdownList[romNameIndex + 2]
            // because allRomsDropdownList has two extra items "Select" & "Stock-device",
            // not present in truncatedCustomRomsList
        )
        val adapter = ArrayAdapter(requireContext(),
                                   R.layout.item_dropdown_menu,
                                   allRomsDropdownList)
        bottomSheetBinding.romDropdownMenu.setAdapter(adapter)
    
        bottomSheetBinding.romDropdownMenu.setOnItemClickListener { _, _, position, _ ->
            footerBinding.positiveButton.isEnabled = position != 0
        }
    
        // Proceed
        footerBinding.positiveButton.isEnabled = false
        timer = object : CountDownTimer(10000, 1000) {
        
            override fun onTick(millisUntilFinished: Long) {
                remSecs = (millisUntilFinished / 1000).toInt() + 1 // Show 10s..1s instead of 9s..0s
                val positiveBtnText = ("${getString(R.string.proceed)} ${remSecs}s")
                footerBinding.positiveButton.text = positiveBtnText
            }
        
            override fun onFinish() {
                footerBinding.positiveButton.text = getString(R.string.proceed)
                footerBinding.positiveButton.isEnabled =
                    bottomSheetBinding.romDropdownMenu.text.toString() != allRomsDropdownList[0]
                timer = null
            }
        }.start()
    
        // Proceed
        footerBinding.positiveButton.setOnClickListener{
            preferenceManager.setBoolean(FIRST_SUBMISSION, false)
            preferenceManager.setString(DEVICE_ROM, bottomSheetBinding.romDropdownMenu.text.toString())
            dismiss()
            positiveButtonClickListener.invoke()
        }
    
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        _binding = null
    }
}