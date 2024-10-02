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

package tech.techlore.plexus.fragments.bottomsheets.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.INSTALLED_FROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
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
    
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val prefManager by inject<PreferenceManager>()
        val checkIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_done)
        val isInstalledAppsFragment =
            navController.currentDestination!!.id in setOf(R.id.installedAppsFragment, R.id.favoritesFragment)
        val isMyRatingsFragment =
            navController.currentDestination!!.id == R.id.myRatingsFragment
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.menu_sort)
    
        // Default alphabetical checked chip
        if (prefManager.getInt(A_Z_SORT) == 0) {
            prefManager.setInt(A_Z_SORT, R.id.sortAZ)
        }
        bottomSheetBinding.alphabeticalChipGroup.check(prefManager.getInt(A_Z_SORT))
        
        // Installed from chip group
        if (isInstalledAppsFragment) {
            bottomSheetBinding.sortInstalledFromText.isVisible = true
            bottomSheetBinding.installedFromChipGroup.apply {
                isVisible = true
                if (prefManager.getInt(INSTALLED_FROM_SORT) == 0) {
                    prefManager.setInt(INSTALLED_FROM_SORT, R.id.sortInstalledAny)
                }
                check(prefManager.getInt(INSTALLED_FROM_SORT))
            }
        }
    
        // Status toggle button group
        if (!isMyRatingsFragment) {
            bottomSheetBinding.statusToggleGroup.apply {
                if (prefManager.getInt(STATUS_TOGGLE) == 0) {
                    prefManager.setInt(STATUS_TOGGLE, R.id.toggleAnyStatus)
                }
                val selectedToggle =  prefManager.getInt(STATUS_TOGGLE)
                check(selectedToggle)
                findViewById<MaterialButton>(selectedToggle).icon = checkIcon
                addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        findViewById<MaterialButton>(checkedId).icon = checkIcon // Add checkmark icon
                        bottomSheetBinding.statusChipGroup.isVisible = checkedId != R.id.toggleAnyStatus
                    }
                    else {
                        findViewById<MaterialButton>(checkedId).icon = null // Remove checkmark icon
                    }
                }
            }
        }
        else {
            bottomSheetBinding.sortStatusText.isVisible = false
            bottomSheetBinding.statusToggleGroup.isVisible = false
        }
    
        // Status chip group
        if (bottomSheetBinding.statusToggleGroup.isVisible) {
            bottomSheetBinding.statusChipGroup.apply {
                if (prefManager.getInt(STATUS_TOGGLE) == R.id.toggleDgStatus) {
                    isVisible = true
            
                    // Default de-Googled status checked chip
                    if (prefManager.getInt(DG_STATUS_SORT) == 0) {
                        prefManager.setInt(DG_STATUS_SORT, R.id.sortNotTested)
                    }
                    check(prefManager.getInt(DG_STATUS_SORT))
                }
                else if (prefManager.getInt(STATUS_TOGGLE) == R.id.toggleMgStatus) {
                    isVisible = true
            
                    // Default microG status checked chip
                    if (prefManager.getInt(MG_STATUS_SORT) == 0) {
                        prefManager.setInt(MG_STATUS_SORT, R.id.sortNotTested)
                    }
                    check(prefManager.getInt(MG_STATUS_SORT))
                }
                else {
                    isVisible = false
                }
            }
        }
    
        // Done
        footerBinding.positiveButton.setOnClickListener {
            prefManager.setInt(A_Z_SORT,
                               bottomSheetBinding.alphabeticalChipGroup.checkedChipId)
            if (isInstalledAppsFragment) prefManager.setInt(INSTALLED_FROM_SORT,
                                                            bottomSheetBinding.installedFromChipGroup.checkedChipId)
            prefManager.setInt(STATUS_TOGGLE,
                               bottomSheetBinding.statusToggleGroup.checkedButtonId)
            if (prefManager.getInt(STATUS_TOGGLE) == R.id.toggleDgStatus) {
                prefManager.setInt(DG_STATUS_SORT,
                                   bottomSheetBinding.statusChipGroup.checkedChipId)
            }
            else if (prefManager.getInt(STATUS_TOGGLE) == R.id.toggleMgStatus) {
                prefManager.setInt(MG_STATUS_SORT,
                                   bottomSheetBinding.statusChipGroup.checkedChipId)
            }
        
            dismiss()
            refreshFragment(navController)
        }
    
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
