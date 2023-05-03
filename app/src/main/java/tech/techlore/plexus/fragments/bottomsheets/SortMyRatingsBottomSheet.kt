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
import tech.techlore.plexus.databinding.BottomSheetSortMyRatingsBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_STATUS_CHIP
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_STATUS_RADIO
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment

class SortMyRatingsBottomSheet(private val navController: NavController) : BottomSheetDialogFragment() {
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        val bottomSheetBinding = BottomSheetSortMyRatingsBinding.inflate(inflater, container, false)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val preferenceManager = PreferenceManager(requireContext())
        
        headerBinding.bottomSheetTitle.text = getString(R.string.menu_sort)
        
        // Default alphabetical checked chip
        if (preferenceManager.getInt(MY_RATINGS_A_Z_SORT) == 0) {
            preferenceManager.setInt(MY_RATINGS_A_Z_SORT, R.id.my_ratings_sort_a_z)
        }
        bottomSheetBinding.myRatingsAlphabeticalChipGroup.check(preferenceManager.getInt(MY_RATINGS_A_Z_SORT))
        
        // Status radio btn checked by default
        if (preferenceManager.getInt(MY_RATINGS_STATUS_RADIO) == 0) {
            preferenceManager.setInt(MY_RATINGS_STATUS_RADIO, R.id.my_ratings_radio_any_status)
        }
        bottomSheetBinding.myRatingsStatusRadiogroup.check(preferenceManager.getInt(MY_RATINGS_STATUS_RADIO))
        
        // Status chip group
        bottomSheetBinding.myRatingsStatusChipGroup.isVisible = preferenceManager.getInt(MY_RATINGS_STATUS_RADIO) != R.id.my_ratings_radio_any_status
        if (preferenceManager.getInt(MY_RATINGS_STATUS_CHIP) == 0) {
            preferenceManager.setInt(MY_RATINGS_STATUS_CHIP, R.id.my_ratings_sort_any)
        }
        bottomSheetBinding.myRatingsStatusChipGroup.check(preferenceManager.getInt(MY_RATINGS_STATUS_CHIP))
        
        // On selecting status radio btn
        bottomSheetBinding.myRatingsStatusRadiogroup.setOnCheckedChangeListener { _, checkedId: Int ->
            if (checkedId != R.id.my_ratings_radio_any_status) {
                bottomSheetBinding.myRatingsStatusChipGroup.visibility = View.VISIBLE
            }
            else {
                bottomSheetBinding.myRatingsStatusChipGroup.visibility = View.GONE
            }
        }
        
        // Done
        footerBinding.positiveButton.text = getString(R.string.done)
        footerBinding.positiveButton.setOnClickListener {
            preferenceManager.setInt(MY_RATINGS_A_Z_SORT,
                                     bottomSheetBinding.myRatingsAlphabeticalChipGroup.checkedChipId)
            preferenceManager.setInt(MY_RATINGS_STATUS_RADIO,
                                     bottomSheetBinding.myRatingsStatusRadiogroup.checkedRadioButtonId)
            preferenceManager.setInt(MY_RATINGS_STATUS_CHIP,
                                     bottomSheetBinding.myRatingsStatusChipGroup.checkedChipId)
            
            dismiss()
            refreshFragment(navController)
        }
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
        
        return bottomSheetBinding.root
    }
}