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

package tech.techlore.plexus.bottomsheets.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textview.MaterialTextView
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.bottomsheets.common.RomSelectionBottomSheet
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetNavViewBinding
import tech.techlore.plexus.objects.DeviceState
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.IS_REGISTERED
import tech.techlore.plexus.utils.UiUtils.Companion.setDgMgTextWithIcon

class NavViewBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetNavViewBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetNavViewBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val mainActivity = requireActivity() as MainActivity
        
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.isVisible = false
        
        bottomSheetBinding.navView.apply {
            //Header
            getHeaderView(0).apply {
                findViewById<MaterialTextView>(R.id.deviceRom).apply {
                    if (DeviceState.rom.isNotEmpty()) text = DeviceState.rom
                    else
                        setOnClickListener {
                            dismiss()
                            RomSelectionBottomSheet().show(parentFragmentManager, "RomSelectionBottomSheet")
                        }
                }
                findViewById<MaterialTextView>(R.id.deviceAndroidVersion).text = DeviceState.androidVersion
                findViewById<MaterialTextView>(R.id.deviceDgMgStatus).apply {
                    if (!DeviceState.isDeviceDeGoogled && !DeviceState.isDeviceMicroG) isVisible = false
                    else setDgMgTextWithIcon(requireContext())
                }
            }
            
            // Nav view items
            arrayOf(R.id.nav_my_ratings, R.id.nav_delete_account).forEach {
                menu.findItem(it).isVisible = get<EncryptedPreferenceManager>().getBoolean(IS_REGISTERED)
            }
            setCheckedItem(mainActivity.selectedNavItem) // Always sync selected item
            
            setNavigationItemSelectedListener { navMenuItem ->
                dismiss()
                if (navMenuItem.itemId != R.id.nav_delete_account) {
                    mainActivity.apply {
                        selectedNavItem = navMenuItem.itemId
                        displayFragment(navMenuItem.itemId)
                    }
                }
                else DeleteAccountBottomSheet().show(parentFragmentManager, "DeleteAccountBottomSheet")
                true
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}