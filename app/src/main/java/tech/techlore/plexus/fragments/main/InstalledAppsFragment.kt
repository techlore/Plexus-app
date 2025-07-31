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
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.INSTALLED_FROM_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
import tech.techlore.plexus.repositories.database.MainDataRepository
import kotlin.collections.ArrayList

class InstalledAppsFragment : BaseMainDataFragment() {
    
    override suspend fun getDataFromDB(): ArrayList<MainDataMinimal> {
        return miniRepository.miniInstalledAppsListFromDB(
            installedFromPref = prefManager.getInt(INSTALLED_FROM_SORT),
            statusToggleBtnPref = prefManager.getInt(STATUS_TOGGLE),
            orderPref = prefManager.getInt(A_Z_SORT)
        )
    }
    
    override fun onSwipeRefresh() {
        lifecycleScope.launch {
            get<MainDataRepository>().installedAppsIntoDB(requireContext())
            fragmentBinding.swipeRefreshLayout.isRefreshing = false
            mainDataItemAdapter.submitList(getDataFromDB())
        }
    }
    
}