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
        val isInstalledAppsFragment =
            navController.currentDestination!!.id in setOf(R.id.submitRatingFragment, R.id.favoritesFragment)
        val isMyRatingsFragment =
            navController.currentDestination!!.id == R.id.myRatingsFragment
    
        headerBinding.bottomSheetTitle.text = getString(R.string.menu_sort)
    
        // Default alphabetical checked chip
        if (preferenceManager.getInt(A_Z_SORT) == 0) {
            preferenceManager.setInt(A_Z_SORT, R.id.sortAZ)
        }
        bottomSheetBinding.alphabeticalChipGroup.check(preferenceManager.getInt(A_Z_SORT))
        
        // Installed from chip group
        if (isInstalledAppsFragment) {
            bottomSheetBinding.sortInstalledFromText.isVisible = true
            bottomSheetBinding.installedFromChipGroup.apply {
                isVisible = true
                if (preferenceManager.getInt(INSTALLED_FROM_SORT) == 0) {
                    preferenceManager.setInt(INSTALLED_FROM_SORT, R.id.sortInstalledAny)
                }
                check(preferenceManager.getInt(INSTALLED_FROM_SORT))
            }
        }
    
        // Status radio group
        if (!isMyRatingsFragment) {
            bottomSheetBinding.statusRadioGroup.apply {
                if (preferenceManager.getInt(STATUS_RADIO) == 0) {
                    preferenceManager.setInt(STATUS_RADIO, R.id.radioAnyStatus)
                }
                check(preferenceManager.getInt(STATUS_RADIO))
                setOnCheckedChangeListener { _, checkedId: Int ->
                    bottomSheetBinding.statusChipGroup.isVisible = checkedId != R.id.radioAnyStatus
                }
            }
        }
        else {
            bottomSheetBinding.sortStatusText.isVisible = false
            bottomSheetBinding.statusRadioGroup.isVisible = false
        }
    
        // Status chip group
        if (bottomSheetBinding.statusRadioGroup.isVisible) {
            bottomSheetBinding.statusChipGroup.apply {
                if (preferenceManager.getInt(STATUS_RADIO) == R.id.radioDgStatus) {
                    isVisible = true
            
                    // Default de-Googled status checked chip
                    if (preferenceManager.getInt(DG_STATUS_SORT) == 0) {
                        preferenceManager.setInt(DG_STATUS_SORT, R.id.sortNotTested)
                    }
                    check(preferenceManager.getInt(DG_STATUS_SORT))
                }
                else if (preferenceManager.getInt(STATUS_RADIO) == R.id.radioMgStatus) {
                    isVisible = true
            
                    // Default microG status checked chip
                    if (preferenceManager.getInt(MG_STATUS_SORT) == 0) {
                        preferenceManager.setInt(MG_STATUS_SORT, R.id.sortNotTested)
                    }
                    check(preferenceManager.getInt(MG_STATUS_SORT))
                }
                else {
                    isVisible = false
                }
            }
        }
    
        // Done
        footerBinding.positiveButton.setOnClickListener {
            preferenceManager.setInt(A_Z_SORT,
                                     bottomSheetBinding.alphabeticalChipGroup.checkedChipId)
            if (isInstalledAppsFragment) preferenceManager.setInt(INSTALLED_FROM_SORT,
                                                                  bottomSheetBinding.installedFromChipGroup.checkedChipId)
            preferenceManager.setInt(STATUS_RADIO,
                                     bottomSheetBinding.statusRadioGroup.checkedRadioButtonId)
            if (preferenceManager.getInt(STATUS_RADIO) == R.id.radioDgStatus) {
                preferenceManager.setInt(DG_STATUS_SORT,
                                         bottomSheetBinding.statusChipGroup.checkedChipId)
            }
            else if (preferenceManager.getInt(STATUS_RADIO) == R.id.radioMgStatus) {
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
