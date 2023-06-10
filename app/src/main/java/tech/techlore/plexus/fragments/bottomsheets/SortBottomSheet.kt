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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.NavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.INSTALLED_FROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_RADIO
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment

class SortBottomSheet(private val navController: NavController) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetSortBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
    
        _binding = BottomSheetSortBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val preferenceManager = PreferenceManager(requireContext())
    
        headerBinding.bottomSheetTitle.text = getString(R.string.menu_sort)
    
        // Default alphabetical checked chip
        if (preferenceManager.getInt(A_Z_SORT) == 0) {
            preferenceManager.setInt(A_Z_SORT, R.id.sort_a_z)
        }
        bottomSheetBinding.alphabeticalChipGroup.check(preferenceManager.getInt(A_Z_SORT))
    
        // Installed from chip group
        val isInstalledAppsFragment =
            navController.currentDestination!!.id in setOf(R.id.submitRatingFragment, R.id.favoritesFragment)
        if (isInstalledAppsFragment) {
            bottomSheetBinding.sortInstalledFromText.isVisible = true
            bottomSheetBinding.installedFromChipGroup.isVisible = true
            if (preferenceManager.getInt(INSTALLED_FROM_SORT) == 0) {
                preferenceManager.setInt(INSTALLED_FROM_SORT, R.id.sort_installed_any)
            }
            bottomSheetBinding.installedFromChipGroup.check(preferenceManager.getInt(INSTALLED_FROM_SORT))
        }
    
        // Status radio btn checked by default
        if (preferenceManager.getInt(STATUS_RADIO) == 0) {
            preferenceManager.setInt(STATUS_RADIO, R.id.radio_any_status)
        }
        bottomSheetBinding.statusRadiogroup.check(preferenceManager.getInt(STATUS_RADIO))
    
        // Status chip group visibility
        if (preferenceManager.getInt(STATUS_RADIO) == R.id.radio_dg_status) {
            bottomSheetBinding.statusChipGroup.isVisible = true
        
            // Default DG status checked chip
            if (preferenceManager.getInt(DG_STATUS_SORT) == 0) {
                preferenceManager.setInt(DG_STATUS_SORT,
                                         R.id.sort_not_tested)
            }
            bottomSheetBinding.statusChipGroup.check(preferenceManager.getInt(DG_STATUS_SORT))
        }
        else if (preferenceManager.getInt(STATUS_RADIO) == R.id.radio_mg_status) {
            bottomSheetBinding.statusChipGroup.isVisible = true
        
            // Default MG status checked chip
            if (preferenceManager.getInt(MG_STATUS_SORT) == 0) {
                preferenceManager.setInt(MG_STATUS_SORT,
                                         R.id.sort_not_tested)
            }
            bottomSheetBinding.statusChipGroup.check(preferenceManager.getInt(MG_STATUS_SORT))
        }
        else {
            bottomSheetBinding.statusChipGroup.isVisible = false
        }
    
        // On selecting status radio btn
        bottomSheetBinding.statusRadiogroup.setOnCheckedChangeListener { _, checkedId: Int ->
            bottomSheetBinding.statusChipGroup.isVisible = checkedId != R.id.radio_any_status
        }
    
        // Done
        footerBinding.positiveButton.setOnClickListener {
            preferenceManager.setInt(A_Z_SORT,
                                     bottomSheetBinding.alphabeticalChipGroup.checkedChipId)
            if (isInstalledAppsFragment) preferenceManager.setInt(INSTALLED_FROM_SORT,
                                                                  bottomSheetBinding.installedFromChipGroup.checkedChipId)
            preferenceManager.setInt(STATUS_RADIO,
                                     bottomSheetBinding.statusRadiogroup.checkedRadioButtonId)
            if (preferenceManager.getInt(STATUS_RADIO) == R.id.radio_dg_status) {
                preferenceManager.setInt(DG_STATUS_SORT,
                                         bottomSheetBinding.statusChipGroup.checkedChipId)
            }
            else if (preferenceManager.getInt(STATUS_RADIO) == R.id.radio_mg_status) {
                preferenceManager.setInt(MG_STATUS_SORT,
                                         bottomSheetBinding.statusChipGroup.checkedChipId)
            }
        
            dismiss()
            refreshFragment(navController)
        }
    
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
