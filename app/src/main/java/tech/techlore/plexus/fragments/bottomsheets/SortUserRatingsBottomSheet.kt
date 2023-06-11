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
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortUserRatingsBinding
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment

class SortUserRatingsBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetSortUserRatingsBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        
        _binding = BottomSheetSortUserRatingsBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val detailsActivity = requireActivity() as AppDetailsActivity
        
        headerBinding.bottomSheetTitle.text = getString(R.string.menu_sort)
        
        // Version dropdown
        bottomSheetBinding.userRatingsVersionDropdownMenu.apply {
            setText(detailsActivity.selectedVersionString)
            val versionAdapter = ArrayAdapter(requireContext(),
                                              R.layout.item_dropdown_menu,
                                              detailsActivity.differentVersionsList)
            setAdapter(versionAdapter)
        }
        
        // Rom dropdown
        bottomSheetBinding.userRatingsRomDropdownMenu.apply {
            setText(detailsActivity.selectedRomString)
            val romAdapter = ArrayAdapter(requireContext(),
                                          R.layout.item_dropdown_menu,
                                          detailsActivity.differentRomsList)
            setAdapter(romAdapter)
        }
        
        // Android dropdown
        bottomSheetBinding.userRatingsAndroidDropdownMenu.apply {
            setText(detailsActivity.selectedAndroidString)
            val androidAdapter = ArrayAdapter(requireContext(),
                                              R.layout.item_dropdown_menu,
                                              detailsActivity.differentAndroidsList)
            setAdapter(androidAdapter)
        }
        
        // Installed from chip checked by default
        bottomSheetBinding.userRatingsInstalledFromChipGroup.check(detailsActivity.installedFromChip)
        
        // Status radio group
        bottomSheetBinding.userRatingsStatusRadiogroup.apply {
            check(detailsActivity.statusRadio)
            setOnCheckedChangeListener { _, checkedId: Int ->
                bottomSheetBinding.userRatingsStatusChipGroup.isVisible =
                    checkedId != R.id.user_ratings_radio_any_status
            }
        }
        
        // Status chip group visibility
        bottomSheetBinding.userRatingsStatusChipGroup.apply {
            when (detailsActivity.statusRadio) {
                R.id.user_ratings_radio_dg_status -> {
                    isVisible = true
                    // Default DG status checked chip
                    check(detailsActivity.dgStatusSort)
                }
                R.id.user_ratings_radio_mg_status -> {
                    isVisible = true
                    // Default MG status checked chip
                    check(detailsActivity.mgStatusSort)
                }
                else -> isVisible = false
            }
        }
        
        // Done
        footerBinding.positiveButton.setOnClickListener {
            detailsActivity.selectedVersionString = bottomSheetBinding.userRatingsVersionDropdownMenu.text.toString()
            detailsActivity.selectedRomString = bottomSheetBinding.userRatingsRomDropdownMenu.text.toString()
            detailsActivity.selectedAndroidString = bottomSheetBinding.userRatingsAndroidDropdownMenu.text.toString()
            detailsActivity.installedFromChip = bottomSheetBinding.userRatingsInstalledFromChipGroup.checkedChipId
            detailsActivity.statusRadio = bottomSheetBinding.userRatingsStatusRadiogroup.checkedRadioButtonId
            if (detailsActivity.statusRadio == R.id.user_ratings_radio_dg_status) {
                detailsActivity.dgStatusSort = bottomSheetBinding.userRatingsStatusChipGroup.checkedChipId
            }
            else if (detailsActivity.statusRadio == R.id.user_ratings_radio_mg_status) {
                detailsActivity.mgStatusSort = bottomSheetBinding.userRatingsStatusChipGroup.checkedChipId
            }
            
            detailsActivity.listIsSorted = false // Set to false so list is sorted on fragment refresh
            
            dismiss()
            refreshFragment(detailsActivity.navController)
        }
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}