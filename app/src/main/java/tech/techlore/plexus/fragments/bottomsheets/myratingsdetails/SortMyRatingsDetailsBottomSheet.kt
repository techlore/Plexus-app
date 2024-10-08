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

package tech.techlore.plexus.fragments.bottomsheets.myratingsdetails

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
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
        bottomSheetBinding.ratingsAppVerDropdownMenu.apply {
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
        
        // Status toggle button group
        bottomSheetBinding.ratingsStatusToggleGroup.apply {
            check(detailsActivity.statusToggleBtn)
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    findViewById<MaterialButton>(checkedId).icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_done) // Add checkmark icon
                    bottomSheetBinding.ratingsStatusChipGroup.isVisible = checkedId != R.id.ratingsToggleAnyStatus
                }
                else {
                    findViewById<MaterialButton>(checkedId).icon = null // Remove checkmark icon
                }
            }
        }
        
        // Status chip group visibility
        bottomSheetBinding.ratingsStatusChipGroup.apply {
            when (detailsActivity.statusToggleBtn) {
                R.id.ratingsToggleDgStatus -> {
                    isVisible = true
                    // Default DG status checked chip
                    check(detailsActivity.dgStatusSort)
                }
                R.id.ratingsToggleMgStatus -> {
                    isVisible = true
                    // Default MG status checked chip
                    check(detailsActivity.mgStatusSort)
                }
                else -> isVisible = false
            }
        }
        
        // Done
        footerBinding.positiveButton.setOnClickListener {
            detailsActivity.selectedVersionString = bottomSheetBinding.ratingsAppVerDropdownMenu.text.toString()
            detailsActivity.selectedRomString = bottomSheetBinding.ratingsRomDropdownMenu.text.toString()
            detailsActivity.selectedAndroidString = bottomSheetBinding.ratingsAndroidDropdownMenu.text.toString()
            detailsActivity.installedFromChip = bottomSheetBinding.ratingsInstalledFromChipGroup.checkedChipId
            detailsActivity.statusToggleBtn = bottomSheetBinding.ratingsStatusToggleGroup.checkedButtonId
            if (detailsActivity.statusToggleBtn == R.id.ratingsToggleDgStatus) {
                detailsActivity.dgStatusSort = bottomSheetBinding.ratingsStatusChipGroup.checkedChipId
            }
            else if (detailsActivity.statusToggleBtn == R.id.ratingsToggleMgStatus) {
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