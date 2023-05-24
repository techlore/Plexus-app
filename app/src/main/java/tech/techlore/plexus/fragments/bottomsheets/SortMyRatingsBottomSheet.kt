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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetSortMyRatingsBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_ANDROID_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_INSTALLED_FROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_ROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_STATUS_CHIP
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_STATUS_RADIO
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MY_RATINGS_VERSION_SORT
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment

class SortMyRatingsBottomSheet(private val navController: NavController) : BottomSheetDialogFragment() {
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
    
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        
        val bottomSheetBinding = BottomSheetSortMyRatingsBinding.inflate(inflater, container, false)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val preferenceManager = PreferenceManager(requireContext())
        
        lifecycleScope.launch {
            val myRatingsList = (requireContext().applicationContext as ApplicationManager).myRatingsRepository.getMyRatingsList()
            val differentVersionsList: List<String>
            val differentRomsList: List<String>
            val differentAndroidsList: List<String>
            
            withContext(Dispatchers.Default) {
                // Get different versions, ROMs & androids from ratings list
                // and store them in a separate list
                val uniqueVersions = HashSet<String>()
                myRatingsList.forEach { ratings ->
                    uniqueVersions.add("${ratings.version} (${ratings.buildNumber})")
                }
                differentVersionsList = listOf(getString(R.string.any)) + uniqueVersions.toList()
    
                val uniqueRoms = HashSet<String>()
                myRatingsList.forEach { ratings ->
                    uniqueRoms.add(ratings.romName)
                }
                differentRomsList = listOf(getString(R.string.any)) + uniqueRoms.toList()
    
                val uniqueAndroids = HashSet<String>()
                myRatingsList.forEach { ratings ->
                    uniqueAndroids.add(ratings.androidVersion)
                }
                differentAndroidsList = listOf(getString(R.string.any)) + uniqueAndroids.toList()
            }
    
            withContext(Dispatchers.Main) {
                headerBinding.bottomSheetTitle.text = getString(R.string.menu_sort)
    
                // Default alphabetical checked chip
                if (preferenceManager.getInt(MY_RATINGS_A_Z_SORT) == 0) {
                    preferenceManager.setInt(MY_RATINGS_A_Z_SORT, R.id.my_ratings_sort_a_z)
                }
                bottomSheetBinding.myRatingsAlphabeticalChipGroup.check(preferenceManager.getInt(MY_RATINGS_A_Z_SORT))
    
                // Version dropdown
                if (preferenceManager.getString(MY_RATINGS_VERSION_SORT).isNullOrEmpty()) {
                    preferenceManager.setString(MY_RATINGS_VERSION_SORT, differentVersionsList[0])
                }
                bottomSheetBinding.myRatingsVersionDropdownMenu.setText(preferenceManager.getString(MY_RATINGS_VERSION_SORT))
                val versionAdapter = ArrayAdapter(requireContext(),
                                                  R.layout.item_dropdown_menu,
                                                  differentVersionsList)
                bottomSheetBinding.myRatingsVersionDropdownMenu.setAdapter(versionAdapter)
    
                // Rom dropdown
                if (preferenceManager.getString(MY_RATINGS_ROM_SORT).isNullOrEmpty()) {
                    preferenceManager.setString(MY_RATINGS_ROM_SORT, differentRomsList[0])
                }
                bottomSheetBinding.myRatingsRomDropdownMenu.setText(preferenceManager.getString(MY_RATINGS_ROM_SORT))
                val romAdapter = ArrayAdapter(requireContext(),
                                              R.layout.item_dropdown_menu,
                                              differentRomsList)
                bottomSheetBinding.myRatingsRomDropdownMenu.setAdapter(romAdapter)
    
                // Android dropdown
                if (preferenceManager.getString(MY_RATINGS_ANDROID_SORT).isNullOrEmpty()) {
                    preferenceManager.setString(MY_RATINGS_ANDROID_SORT, differentAndroidsList[0])
                }
                bottomSheetBinding.myRatingsAndroidDropdownMenu.setText(preferenceManager.getString(MY_RATINGS_ANDROID_SORT))
                val androidAdapter = ArrayAdapter(requireContext(),
                                                  R.layout.item_dropdown_menu,
                                                  differentAndroidsList)
                bottomSheetBinding.myRatingsAndroidDropdownMenu.setAdapter(androidAdapter)
    
                // Installed from chip checked by default
                if (preferenceManager.getInt(MY_RATINGS_INSTALLED_FROM_SORT) == 0) {
                    preferenceManager.setInt(MY_RATINGS_INSTALLED_FROM_SORT, R.id.my_ratings_chip_installed_any)
                }
                bottomSheetBinding.myRatingsInstalledFromChipGroup.check(preferenceManager.getInt(MY_RATINGS_INSTALLED_FROM_SORT))
    
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
                footerBinding.positiveButton.setOnClickListener {
                    preferenceManager.setInt(MY_RATINGS_A_Z_SORT,
                                             bottomSheetBinding.myRatingsAlphabeticalChipGroup.checkedChipId)
                    preferenceManager.setString(MY_RATINGS_VERSION_SORT,
                                                bottomSheetBinding.myRatingsVersionDropdownMenu.text.toString())
                    preferenceManager.setString(MY_RATINGS_ROM_SORT,
                                                bottomSheetBinding.myRatingsRomDropdownMenu.text.toString())
                    preferenceManager.setString(MY_RATINGS_ANDROID_SORT,
                                                bottomSheetBinding.myRatingsAndroidDropdownMenu.text.toString())
                    preferenceManager.setInt(MY_RATINGS_INSTALLED_FROM_SORT,
                                             bottomSheetBinding.myRatingsInstalledFromChipGroup.checkedChipId)
                    preferenceManager.setInt(MY_RATINGS_STATUS_RADIO,
                                             bottomSheetBinding.myRatingsStatusRadiogroup.checkedRadioButtonId)
                    preferenceManager.setInt(MY_RATINGS_STATUS_CHIP,
                                             bottomSheetBinding.myRatingsStatusChipGroup.checkedChipId)
        
                    dismiss()
                    refreshFragment(navController)
                }
    
                // Cancel
                footerBinding.negativeButton.setOnClickListener { dismiss() }
            }
        }
        
        return bottomSheetBinding.root
    }
}