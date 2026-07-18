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

package tech.techlore.plexus.bottomsheets.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.activities.VerificationActivity
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetRomSelectionBinding
import tech.techlore.plexus.objects.DeviceState
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.utils.DeviceUtils.Companion.isDeviceDeGoogledOrMicroG
import tech.techlore.plexus.utils.IntentUtils.Companion.startActivityWithTransition
import kotlin.time.Duration.Companion.milliseconds

class RomSelectionBottomSheet(private val isFromNavView: Boolean = true) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetRomSelectionBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private var job: Job? = null
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetRomSelectionBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.isVisible = false
        
        lifecycleScope.launch {
            
            val allRomsDropdownList =
                arrayOf(getString(R.string.select)) +
                "Stock (${Build.MANUFACTURER} ${Build.MODEL})" +
                resources.getStringArray(R.array.custom_roms)
            
            // ROM dropdown
            (bottomSheetBinding.romDropdownMenu as MaterialAutoCompleteTextView).apply {
                setText(allRomsDropdownList[0])
                setSimpleItems(allRomsDropdownList)
                setOnItemClickListener { _, _, position, _ ->
                    footerBinding.positiveButton.isEnabled = position != 0 && job == null
                }
            }
            
            // Done/Proceed
            footerBinding.positiveButton.apply {
                isEnabled = false
                val positiveButtonText = if (isFromNavView) getString(R.string.done) else getString(R.string.proceed)
                job =
                    lifecycleScope.launch {
                        (5 downTo 1).forEach {
                            withContext(Dispatchers.Main) {
                                @SuppressLint("SetTextI18n")
                                text = "$positiveButtonText ${it}s"
                            }
                            delay(1000.milliseconds)
                        }
                        withContext(Dispatchers.Main) {
                            text = positiveButtonText
                            isEnabled = bottomSheetBinding.romDropdownMenu.text.toString() != allRomsDropdownList[0]
                            job = null
                        }
                    }
                setOnClickListener {
                    get<EncryptedPreferenceManager>().apply {
                        setString(DEVICE_ROM, bottomSheetBinding.romDropdownMenu.text.toString())
                        getString(DEVICE_ROM)?.let {
                            DeviceState.rom = it
                            if (it == "CalyxOS") {
                                lifecycleScope.launch {
                                    isDeviceDeGoogledOrMicroG(requireContext().packageManager)
                                }
                            }
                        }
                    }
                    dismiss()
                    if (!isFromNavView) {
                        (requireActivity() as AppDetailsActivity).apply {
                            startActivityWithTransition(Intent(this, VerificationActivity::class.java))
                        }
                    }
                }
            }
            
            // Cancel
            footerBinding.negativeButton.setOnClickListener { dismiss() }
            
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        _binding = null
    }
}