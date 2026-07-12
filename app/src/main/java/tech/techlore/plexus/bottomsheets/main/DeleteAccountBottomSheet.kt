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

package tech.techlore.plexus.bottomsheets.main

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.bottomsheets.common.BaseDeleteBottomSheet
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ID
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_TOKEN
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.IS_REGISTERED
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class DeleteAccountBottomSheet : BaseDeleteBottomSheet() {
    
    override fun getTitleText(): String {
        return getString(R.string.delete_account)
    }
    
    override fun getDescText(): String {
        return getString(R.string.delete_account_desc)
    }
    
    override fun onPositiveBtnClick() {
        EncryptedPreferenceManager(requireContext()).apply {
            deleteString(DEVICE_TOKEN)
            deleteString(DEVICE_ID)
            deleteString(DEVICE_ROM)
            setBoolean(IS_REGISTERED, false)
            lifecycleScope.launch {
                get<MyRatingsRepository>().deleteAllMyRatings()
            }
        }
        dismiss()
        (requireActivity() as MainActivity).apply {
            if (selectedNavItem == R.id.nav_my_ratings) {
                onNavViewItemSelected(defaultSelectedNavItem, true)
            }
            showSnackbar(activityBinding.mainCoordLayout,
                         getString(R.string.deleted_account_successfully),
                         activityBinding.mainDockedToolbar)
        }
    }
}