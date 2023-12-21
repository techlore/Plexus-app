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

import android.app.Dialog
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
import tech.techlore.plexus.activities.MyRatingsDetailsActivity
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortRatingsBinding
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment

class SortMyRatingsDetailsBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetSortRatingsBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetSortRatingsBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val detailsActivity = requireActivity() as MyRatingsDetailsActivity
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.menu_sort)
        
        // Version dropdown
        bottomSheetBinding.ratingsVersionDropdownMenu.apply {
            setText(detailsActivity.selectedVersionString)
            val versionAdapter = ArrayAdapter(requireContext(),
                                              R.layout.item_dropdown_menu,
                                              detailsActivity.differentVersionsList)
            setAdapter(versionAdapter)
        }
        
        // Rom dropdown
        bottomSheetBinding.ratingsRomDropdownMenu.apply {
            setText(detailsActivity.selectedRomString)
            val romAdapter = ArrayAdapter(requireContext(),
                                          R.layout.item_dropdown_menu,
                                          detailsActivity.differentRomsList)
            setAdapter(romAdapter)
        }
        
        // Android dropdown
        bottomSheetBinding.ratingsAndroidDropdownMenu.apply {
            setText(detailsActivity.selectedAndroidString)
            val androidAdapter = ArrayAdapter(requireContext(),
                                              R.layout.item_dropdown_menu,
                                              detailsActivity.differentAndroidsList)
            setAdapter(androidAdapter)
        }
        
        // Installed from chip checked by default
        bottomSheetBinding.ratingsInstalledFromChipGroup.check(detailsActivity.installedFromChip)
        
        // Status radio group
        bottomSheetBinding.ratingsStatusRadioGroup.apply {
            check(detailsActivity.statusRadio)
            setOnCheckedChangeListener { _, checkedId: Int ->
                bottomSheetBinding.ratingsStatusChipGroup.isVisible =
                    checkedId != R.id.ratingsRadioAnyStatus
            }
        }
        
        // Status chip group visibility
        bottomSheetBinding.ratingsStatusChipGroup.apply {
            when (detailsActivity.statusRadio) {
                R.id.ratingsRadioDgStatus -> {
                    isVisible = true
                    // Default DG status checked chip
                    check(detailsActivity.dgStatusSort)
                }
                R.id.ratingsRadioMgStatus -> {
                    isVisible = true
                    // Default MG status checked chip
                    check(detailsActivity.mgStatusSort)
                }
                else -> isVisible = false
            }
        }
        
        // Done
        footerBinding.positiveButton.setOnClickListener {
            detailsActivity.selectedVersionString = bottomSheetBinding.ratingsVersionDropdownMenu.text.toString()
            detailsActivity.selectedRomString = bottomSheetBinding.ratingsRomDropdownMenu.text.toString()
            detailsActivity.selectedAndroidString = bottomSheetBinding.ratingsAndroidDropdownMenu.text.toString()
            detailsActivity.installedFromChip = bottomSheetBinding.ratingsInstalledFromChipGroup.checkedChipId
            detailsActivity.statusRadio = bottomSheetBinding.ratingsStatusRadioGroup.checkedRadioButtonId
            if (detailsActivity.statusRadio == R.id.ratingsRadioDgStatus) {
                detailsActivity.dgStatusSort = bottomSheetBinding.ratingsStatusChipGroup.checkedChipId
            }
            else if (detailsActivity.statusRadio == R.id.ratingsRadioMgStatus) {
                detailsActivity.mgStatusSort = bottomSheetBinding.ratingsStatusChipGroup.checkedChipId
            }
            
            dismiss()
            refreshFragment(detailsActivity.navController)
        }
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}