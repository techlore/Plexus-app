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
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetDevVerfWarnBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.SHOW_DEV_VERF_WARNING
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class DevVerfWarnBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetDevVerfWarnBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetDevVerfWarnBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.isVisible = false
        
        // Details
        bottomSheetBinding.warnDetailsCard.setOnClickListener {
            requireActivity().openURL(getString(R.string.dev_verf_warn_details_url))
        }
        
        // Solutions
        bottomSheetBinding.warnSolutionsCard.setOnClickListener {
            requireActivity().openURL(getString(R.string.dev_verf_warn_solutions_url))
        }
        
        // Don't show again
        bottomSheetBinding.warnHideCheckbox.setOnCheckedChangeListener { _, isChecked ->
            get<PreferenceManager>().setBoolean(SHOW_DEV_VERF_WARNING, !isChecked)
        }
        
        footerBinding.positiveButton.isVisible = false
        
        // Dismiss
        footerBinding.negativeButton.apply {
            text = getString(R.string.dismiss)
            setOnClickListener {
                dismiss()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}