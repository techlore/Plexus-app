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

package tech.techlore.plexus.bottomsheets.myratingsdetails

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.common.BaseDeleteBottomSheet
import tech.techlore.plexus.bottomsheets.common.ExceptionErrorBottomSheet
import tech.techlore.plexus.bottomsheets.common.NoNetworkBottomSheet
import tech.techlore.plexus.interfaces.details.RatingDetailDeleteListener
import tech.techlore.plexus.keystore.KeyStoreManager
import tech.techlore.plexus.models.post.rating.DeleteMyRating
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_TOKEN
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet

class DeleteRatingBottomSheet(
    private val ratingId: String,
    private val encTokenBase64: String?,
    private val packageName: String,
    private val ratingDetailDeleteListener: RatingDetailDeleteListener
) : BaseDeleteBottomSheet() {
    
    private var decDeviceToken: String = ""
    private var decDeleteToken: String = ""
    
    override fun getTitleText(): String {
        return getString(R.string.delete_rating)
    }
    
    override fun getDescText(): String {
        return getString(R.string.delete_rating_desc)
    }
    
    override fun onPositiveBtnClick() {
        lifecycleScope.launch {
            if (encTokenBase64 != null) {
                if (hasInternet(requireContext())) {
                    try {
                        val deleteRatingResponse =
                            get<ApiRepository>().deleteMyRating(
                                decDeviceToken.ifEmpty {
                                    get<EncryptedPreferenceManager>().getString(DEVICE_TOKEN) !!
                                },
                                packageName,
                                ratingId,
                                DeleteMyRating(
                                    decDeleteToken.ifEmpty {
                                        get<KeyStoreManager>().decryptToken(encTokenBase64)
                                    }
                                )
                            )
                        
                        if (deleteRatingResponse.status.value != 204) {
                            bottomSheetBinding.deleteProgressIndicator.hide()
                            bottomSheetBinding.deleteDesc.text =
                                getString(R.string.error_occurred_title)
                            footerBinding.positiveButton.apply {
                                isEnabled = true
                                footerBinding.negativeButton.isEnabled = true
                                text = getString(R.string.retry)
                                setOnClickListener {
                                    isEnabled = false
                                    footerBinding.negativeButton.isEnabled = false
                                    onPositiveBtnClick()
                                }
                            }
                        }
                    }
                    catch (e: Exception) {
                        ExceptionErrorBottomSheet(
                            exception = e,
                            negativeBtnText = getString(R.string.cancel),
                            onPositiveBtnClick = { onPositiveBtnClick() },
                            onNegativeBtnClick = {}
                        ).show(parentFragmentManager, "ExceptionErrorBottomSheet")
                    }
                }
                else {
                    NoNetworkBottomSheet(
                        negativeBtnText = getString(R.string.cancel),
                        onPositiveBtnClick = { onPositiveBtnClick() },
                        onNegativeBtnClick = {}
                    ).show(parentFragmentManager, "NoNetworkBottomSheet")
                }
            }
            get<MyRatingsRepository>().deleteSingleRatingDetail(packageName, ratingId)
            dismiss()
            ratingDetailDeleteListener.onRatingDetailDeleted(ratingId)
        }
    }
    
}