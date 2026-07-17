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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import tech.techlore.plexus.models.mini.MainDataMini

class InstalledAppsFragment : BaseMainDataFragment() {
    
    override fun getDataFromDB(): Flow<PagingData<MainDataMini>> {
        return mainRepository.miniInstalledAppsListFromDB(
            installedFromPref = installedFromChipId,
            statusToggleBtnPref = statusToggleBtnId,
            orderPref = ascDescChipId
        )
    }
    
    override fun onSwipeRefresh() {
        lifecycleScope.launch {
            mainRepository.installedAppsIntoDB(requireContext())
            mainDataItemAdapter.refresh()
            fragmentBinding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
}