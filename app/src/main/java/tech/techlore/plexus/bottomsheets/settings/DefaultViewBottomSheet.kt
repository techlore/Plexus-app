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

package tech.techlore.plexus.bottomsheets.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetThemeBinding
import tech.techlore.plexus.objects.AppState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEF_VIEW

// Reuse "Theme" bottom sheet layout
class DefaultViewBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetThemeBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetThemeBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val prefManager by inject<PreferenceManager>()
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.default_view)
        
        bottomSheetBinding.followSystem.apply {
            closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_plexus_data)
            text = getString(R.string.plexus_data)
        }
        
        bottomSheetBinding.light.apply {
            closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_installed_apps)
            text = getString(R.string.installed_apps)
        }
        
        bottomSheetBinding.dark.apply {
            closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fav_outline)
            text = getString(R.string.favorites)
        }
        
        // Chip group
        bottomSheetBinding.themeChipGroup.apply {
            
            // Default checked chip
            check(prefManager.getInt(DEF_VIEW, R.id.followSystem))
            
            // On selecting option
            setOnCheckedStateChangeListener { _, checkedIds ->
                val checkedChip = checkedIds.first()
                prefManager.setInt(DEF_VIEW, checkedChip)
                AppState.isDefaultViewChanged = true
                dismiss()
            }
        }
        
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).apply {
            positiveButton.isVisible = false
            negativeButton.setOnClickListener { dismiss() }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}