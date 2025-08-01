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
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.common.ExceptionErrorBottomSheet
import tech.techlore.plexus.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.preferences.PreferenceManager.Companion.A_Z_SORT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.STATUS_TOGGLE
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork

class PlexusDataFragment : BaseMainDataFragment() {
    
    override suspend fun getDataFromDB(): ArrayList<MainDataMinimal> {
        return miniRepository.miniPlexusDataListFromDB(
            statusToggleBtnPref = prefManager.getInt(STATUS_TOGGLE),
            orderPref = prefManager.getInt(A_Z_SORT)
        )
    }
    
    override fun onSwipeRefresh() {
        lifecycleScope.launch {
            if (hasNetwork(requireContext()) && hasInternet()) {
                try {
                    get<MainDataRepository>().plexusDataIntoDB()
                    mainDataList = getDataFromDB()
                    mainDataItemAdapter.submitList(mainDataList)
                    fragmentBinding.swipeRefreshLayout.isRefreshing = false
                }
                catch (e: Exception) {
                    ExceptionErrorBottomSheet(
                        exception = e,
                        negativeBtnText = getString(R.string.cancel),
                        onPositiveBtnClick = { onSwipeRefresh() },
                        onNegativeBtnClick = {
                            fragmentBinding.swipeRefreshLayout.isRefreshing = false
                        }
                    ).show(parentFragmentManager, "ExceptionErrorBottomSheet")
                }
            }
            else {
                NoNetworkBottomSheet(
                    negativeBtnText = getString(R.string.cancel),
                    onPositiveBtnClick = { onSwipeRefresh() },
                    onNegativeBtnClick = {
                        fragmentBinding.swipeRefreshLayout.isRefreshing = false
                    }
                ).show(parentFragmentManager, "NoNetworkBottomSheet")
            }
        }
    }
    
}