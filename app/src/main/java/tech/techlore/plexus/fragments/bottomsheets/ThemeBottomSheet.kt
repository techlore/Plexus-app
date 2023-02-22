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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetThemeBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.THEME

class ThemeBottomSheet : BottomSheetDialogFragment() {
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        val bottomSheetBinding = BottomSheetThemeBinding.inflate(inflater, container, false)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        val preferenceManager = PreferenceManager(requireContext())
        
        headerBinding.bottomSheetTitle.setText(R.string.theme)
        
        // Default checked radio btn
        if (preferenceManager.getInt(THEME) == 0) {
            if (Build.VERSION.SDK_INT >= 29) {
                preferenceManager.setInt(THEME, R.id.sys_default)
            }
            else {
                preferenceManager.setInt(THEME, R.id.light)
            }
        }
        bottomSheetBinding.themeRadiogroup.check(preferenceManager.getInt(THEME))
        
        // Show system default option only on SDK 29 and above
        bottomSheetBinding.sysDefault.visibility = if (Build.VERSION.SDK_INT >= 29) View.VISIBLE else View.GONE
        
        // On selecting option
        bottomSheetBinding.themeRadiogroup.setOnCheckedChangeListener { _, checkedId ->
            
            when (checkedId) {
                
                R.id.sys_default ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                
                R.id.light ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                
                R.id.dark ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
    
            preferenceManager.setInt(THEME, checkedId)
            dismiss()
            requireActivity().recreate()
            
        }
    
        footerBinding.positiveButton.visibility = View.GONE
    
        footerBinding.negativeButton.setOnClickListener {
            dismiss()
        }
        
        return bottomSheetBinding.root
    }
}
