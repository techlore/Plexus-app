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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortUserRatingsBinding
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment

class SortUserRatingsBottomSheet : BottomSheetDialogFragment() {
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        val bottomSheetBinding = BottomSheetSortUserRatingsBinding.inflate(inflater, container, false)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val detailsActivity = requireActivity() as AppDetailsActivity
        var tmpSelVerString: String? = null
        
        headerBinding.bottomSheetTitle.text = getString(R.string.menu_sort)
        
        // Version dropdown
        bottomSheetBinding.versionDropdownMenu.setText(detailsActivity.selectedVersionString)
        val adapter = ArrayAdapter(requireContext(),
                                   R.layout.item_version_dropdown_menu,
                                   detailsActivity.differentVersionsList)
        bottomSheetBinding.versionDropdownMenu.setAdapter(adapter)
        
        // Status radio btn checked by default
        bottomSheetBinding.userRatingsStatusRadiogroup.check(detailsActivity.statusRadio)
        
        // Status chip group visibility
        when (detailsActivity.statusRadio) {
            R.id.user_ratings_radio_dg_status -> {
                bottomSheetBinding.userRatingsStatusChipGroup.visibility = View.VISIBLE
                
                // Default DG status checked chip
                bottomSheetBinding.userRatingsStatusChipGroup.check(detailsActivity.dgStatusSort)
            }
            R.id.user_ratings_radio_mg_status -> {
                bottomSheetBinding.userRatingsStatusChipGroup.visibility = View.VISIBLE
                
                // Default MG status checked chip
                bottomSheetBinding.userRatingsStatusChipGroup.check(detailsActivity.mgStatusSort)
            }
            else -> bottomSheetBinding.userRatingsStatusChipGroup.visibility = View.GONE
        }
        
        // On selecting item in version dropdown
        bottomSheetBinding.versionDropdownMenu.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                tmpSelVerString = parent.getItemAtPosition(position) as String
            }
        
        // On selecting status radio btn
        bottomSheetBinding.userRatingsStatusRadiogroup.setOnCheckedChangeListener { _, checkedId: Int ->
            if (checkedId != R.id.user_ratings_radio_any_status) {
                bottomSheetBinding.userRatingsStatusChipGroup.visibility = View.VISIBLE
            }
            else {
                bottomSheetBinding.userRatingsStatusChipGroup.visibility = View.GONE
            }
        }
        
        // Done
        footerBinding.positiveButton.text = getString(R.string.done)
        footerBinding.positiveButton.setOnClickListener {
            if (!tmpSelVerString.isNullOrEmpty()) {
                detailsActivity.selectedVersionString = tmpSelVerString
            }
            detailsActivity.statusRadio = bottomSheetBinding.userRatingsStatusRadiogroup.checkedRadioButtonId
            if (detailsActivity.statusRadio == R.id.user_ratings_radio_dg_status) {
                detailsActivity.dgStatusSort = bottomSheetBinding.userRatingsStatusChipGroup.checkedChipId
            }
            else if (detailsActivity.statusRadio == R.id.user_ratings_radio_mg_status) {
                detailsActivity.mgStatusSort = bottomSheetBinding.userRatingsStatusChipGroup.checkedChipId
            }
            
            detailsActivity.ratingsListSorted = false // Set this to false so list is sorted on fragment refresh
            
            dismiss()
            refreshFragment(detailsActivity.navController)
        }
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
        
        return bottomSheetBinding.root
    }
}