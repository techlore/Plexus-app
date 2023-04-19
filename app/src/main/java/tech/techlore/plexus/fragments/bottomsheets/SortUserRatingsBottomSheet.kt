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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortUserRatingsBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.USER_RATINGS_DG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.USER_RATINGS_MG_STATUS_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.USER_RATINGS_STATUS_RADIO

class SortUserRatingsBottomSheet : BottomSheetDialogFragment() {
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
    
        val bottomSheetBinding = BottomSheetSortUserRatingsBinding.inflate(inflater, container, false)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val preferenceManager = PreferenceManager(requireContext())
    
        headerBinding.bottomSheetTitle.text = getString(R.string.menu_sort)
    
        // Status radio btn checked by default
        if (preferenceManager.getInt(USER_RATINGS_STATUS_RADIO) == 0) {
            preferenceManager.setInt(USER_RATINGS_STATUS_RADIO, R.id.user_ratings_radio_any_status)
        }
        bottomSheetBinding.userRatingsStatusRadiogroup.check(preferenceManager.getInt(USER_RATINGS_STATUS_RADIO))
    
        // Status chip group visibility
        if (preferenceManager.getInt(USER_RATINGS_STATUS_RADIO) == R.id.user_ratings_radio_dg_status) {
            bottomSheetBinding.userRatingsStatusChipGroup.visibility = View.VISIBLE
        
            // Default DG status checked chip
            if (preferenceManager.getInt(USER_RATINGS_DG_STATUS_SORT) == 0) {
                preferenceManager.setInt(USER_RATINGS_DG_STATUS_SORT, R.id.user_ratings_sort_not_tested)
            }
            bottomSheetBinding.userRatingsStatusChipGroup.check(preferenceManager.getInt(USER_RATINGS_DG_STATUS_SORT))
        }
        else if (preferenceManager.getInt(USER_RATINGS_STATUS_RADIO) == R.id.user_ratings_radio_mg_status) {
            bottomSheetBinding.userRatingsStatusChipGroup.visibility = View.VISIBLE
        
            // Default MG status checked chip
            if (preferenceManager.getInt(USER_RATINGS_MG_STATUS_SORT) == 0) {
                preferenceManager.setInt(USER_RATINGS_MG_STATUS_SORT, R.id.user_ratings_sort_not_tested)
            }
            bottomSheetBinding.userRatingsStatusChipGroup.check(preferenceManager.getInt(USER_RATINGS_MG_STATUS_SORT))
        }
        else {
            bottomSheetBinding.userRatingsStatusChipGroup.visibility = View.GONE
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
            /*preferenceManager.setInt(PreferenceManager.A_Z_SORT,
                                     bottomSheetBinding.alphabeticalChipGroup.checkedChipId)
            preferenceManager.setInt(PreferenceManager.STATUS_RADIO,
                                     bottomSheetBinding.statusRadiogroup.checkedRadioButtonId)
            if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO) == R.id.radio_dg_status) {
                preferenceManager.setInt(PreferenceManager.DG_STATUS_SORT,
                                         bottomSheetBinding.statusChipGroup.checkedChipId)
            }
            else if (preferenceManager.getInt(PreferenceManager.STATUS_RADIO) == R.id.radio_mg_status) {
                preferenceManager.setInt(PreferenceManager.MG_STATUS_SORT,
                                         bottomSheetBinding.statusChipGroup.checkedChipId)
            }
        
            dismiss()
            UiUtils.refreshFragment(navController)*/
        }
    
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
    
        return bottomSheetBinding.root
    }
}