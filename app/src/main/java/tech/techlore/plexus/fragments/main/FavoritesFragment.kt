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

package tech.techlore.plexus.fragments.main

import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.models.mini.MainDataMini
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class FavoritesFragment: BaseMainDataFragment() {
    
    override fun getDataFromDB(): Flow<PagingData<MainDataMini>> {
        return mainRepository.miniFavListFromDB(
            installedFromPref = installedFromChipId,
            statusToggleBtnPref = statusToggleBtnId,
            orderPref = ascDescChipId
        )
    }
    
    override fun isSwipeRefreshEnabled(): Boolean {
        return false
    }
    
    override fun onFavToggled(name: String, packageName: String, isChecked: Boolean) {
        lifecycleScope.launch {
            // Remove item from db
            mainRepository.updateFav(packageName, isChecked)
            Snackbar
                .make(
                    mainActivity.activityBinding.mainCoordLayout,
                    if (isChecked) getString(R.string.added_to_fav, name)
                    else getString(R.string.removed_from_fav, name),
                    BaseTransientBottomBar.LENGTH_SHORT
                )
                .setAnchorView(mainActivity.activityBinding.mainDockedToolbar)
                .setAction(getString(R.string.undo)) {
                    lifecycleScope.launch {
                        // Add item back to db
                        mainRepository.updateFav(packageName, !isChecked)
                        showSnackbar(
                            coordinatorLayout = mainActivity.activityBinding.mainCoordLayout,
                            message =
                                if (!isChecked) getString(R.string.added_to_fav, name)
                                else getString(R.string.removed_from_fav, name),
                            anchorView = mainActivity.activityBinding.mainDockedToolbar
                        )
                    }
                }
                .setCloseIconVisible(true)
                .setCloseIconResource(R.drawable.ic_close)
                .show()
        }
    }
    
}