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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.activities.VerificationActivity
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetRomSelectionBinding
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.utils.SystemUtils.Companion.getSystemProperty

class RomSelectionBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetRomSelectionBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private var timer: CountDownTimer? = null
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetRomSelectionBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val preferenceManager = EncryptedPreferenceManager(requireContext())
        var remSecs: Int
        
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
        bottomSheetBinding.romDropdownMenu.apply {
            setText(
                if (romNameIndex != -1) allRomsDropdownList[romNameIndex + 2] else allRomsDropdownList[0]
                // allRomsDropdownList[romNameIndex + 2]
                // because allRomsDropdownList has two extra items "Select" & "Stock (device)",
                // not present in truncatedCustomRomsList
            )
            
            val adapter = ArrayAdapter(requireContext(),
                                       R.layout.item_dropdown_menu,
                                       allRomsDropdownList)
            setAdapter(adapter)
            
            setOnItemClickListener { _, _, position, _ ->
                footerBinding.positiveButton.isEnabled = position != 0 && timer == null
            }
        }
        
        // Proceed
        footerBinding.positiveButton.apply {
            isEnabled = false
            
            timer = object : CountDownTimer(5000, 1000) {
                
                override fun onTick(millisUntilFinished: Long) {
                    remSecs = (millisUntilFinished / 1000).toInt() + 1 // Show 5s..1s instead of 4s..0s
                    @SuppressLint("SetTextI18n")
                    text = "${getString(R.string.proceed)} ${remSecs}s"
                }
                
                override fun onFinish() {
                    text = getString(R.string.proceed)
                    isEnabled = bottomSheetBinding.romDropdownMenu.text.toString() != allRomsDropdownList[0]
                    timer = null
                }
            }.start()
            
            setOnClickListener {
                preferenceManager.setString(DEVICE_ROM, bottomSheetBinding.romDropdownMenu.text.toString())
                dismiss()
                val detailsActivity = requireActivity() as AppDetailsActivity
                detailsActivity.startActivity(Intent(detailsActivity, VerificationActivity::class.java)
                                                    .putExtra("name", detailsActivity.app.name)
                                                    .putExtra("packageName", detailsActivity.app.packageName)
                                                    .putExtra("installedVersion", detailsActivity.app.installedVersion)
                                                    .putExtra("installedBuild", detailsActivity.app.installedBuild)
                                                    .putExtra("installedFrom", detailsActivity.app.installedFrom)
                                                    .putExtra("isInPlexusData", detailsActivity.app.isInPlexusData))
            }
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