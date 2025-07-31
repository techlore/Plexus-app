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
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.INSTALLED_FROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class FavoritesFragment: BaseMainDataFragment() {
    
    private var lastRemovedIndex: Int = -1
    
    override suspend fun getDataFromDB(): ArrayList<MainDataMinimal> {
        return miniRepository.miniFavListFromDB(
            installedFromPref = prefManager.getInt(INSTALLED_FROM_SORT),
            statusToggleBtnPref = prefManager.getInt(STATUS_TOGGLE),
            orderPref = prefManager.getInt(A_Z_SORT)
        )
    }
    
    override fun isSwipeRefreshEnabled(): Boolean {
        return false
    }
    
    override fun onFavToggled(item: MainDataMinimal, isChecked: Boolean) {
        item.isFav = isChecked
        lifecycleScope.launch {
            get<MainDataMinimalRepository>().updateFav(item)
            // Remove item from view
            if (!isChecked) {
                mainDataItemAdapter.submitList(
                    withContext(Dispatchers.Default) {
                        mainDataItemAdapter.currentList.toMutableList().apply {
                            indexOfFirst { it.packageName == item.packageName }
                                .takeIf { it >= 0 }
                                ?.let{ index ->
                                    lastRemovedIndex = index
                                    removeAt(index)
                                }
                        }
                    }
                )
            }
            Snackbar
                .make(
                    mainActivity.activityBinding.mainCoordLayout,
                    if (isChecked) getString(R.string.added_to_fav, item.name)
                    else getString(R.string.removed_from_fav, item.name),
                    BaseTransientBottomBar.LENGTH_SHORT
                )
                .setAnchorView(mainActivity.activityBinding.mainDockedToolbar)
                .setAction(getString(R.string.undo)) {
                    item.isFav = !isChecked
                    lifecycleScope.launch {
                        get<MainDataMinimalRepository>().updateFav(item)
                        // Add item back to view
                        mainDataItemAdapter.submitList(
                            withContext(Dispatchers.Default) {
                                mainDataItemAdapter.currentList
                                    .toMutableList()
                                    .apply {
                                        add(
                                            index = lastRemovedIndex.coerceIn(0..size),
                                            element = item
                                        )
                                    }
                            }
                        )
                    }
                    showSnackbar(
                        coordinatorLayout = mainActivity.activityBinding.mainCoordLayout,
                        message =
                            if (!isChecked) getString(R.string.added_to_fav, item.name)
                            else getString(R.string.removed_from_fav, item.name),
                        anchorView = mainActivity.activityBinding.mainDockedToolbar
                    )
                }
                .show()
        }
    }
    
}