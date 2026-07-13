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

package tech.techlore.plexus.bottomsheets.appdetails

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.databinding.BottomSheetUploadBinding
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.models.post.app.PostApp
import tech.techlore.plexus.models.post.app.PostAppRoot
import tech.techlore.plexus.models.post.rating.PostMyRating
import tech.techlore.plexus.models.post.rating.PostMyRatingRoot
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_TOKEN
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.objects.DeviceState
import tech.techlore.plexus.keystore.KeyStoreManager
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore

@SuppressLint("SetTextI18n")
class UploadBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetUploadBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var detailsActivity: AppDetailsActivity
    private val encPrefManager by inject<EncryptedPreferenceManager>()
    private lateinit var deviceToken: String
    private val apiRepository by inject<ApiRepository>()
    private lateinit var postAppRoot: PostAppRoot
    private lateinit var rating: PostMyRating
    private lateinit var postMyRatingRoot: PostMyRatingRoot
    private var iconUrl: String? = null
    private var ratingCreated = false
    private var postedRatingId: String = ""
    private var encDeleteTokenB64: String? = null
    private var postedRatingDateTime = ""
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setCanceledOnTouchOutside(false)
            behavior.isDraggable = false
            
            // Prevent bottom sheet dismiss on back pressed
            setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // Do nothing
                    return@OnKeyListener true
                }
                false
            })
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetUploadBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        detailsActivity = requireActivity() as AppDetailsActivity
        deviceToken = encPrefManager.getString(DEVICE_TOKEN)!!
        
        postAppRoot =
            PostAppRoot(
                PostApp(
                    name = detailsActivity.app.name,
                    packageName = detailsActivity.app.packageName,
                    iconUrl = iconUrl
                )
            )
        
        rating =
            PostMyRating(
                version = detailsActivity.app.installedVersion,
                buildNumber = detailsActivity.app.installedBuild,
                romName = encPrefManager.getString(DEVICE_ROM)!!,
                romBuild = Build.DISPLAY,
                androidVersion = DeviceState.androidVersion,
                installedFrom = detailsActivity.app.installedFrom,
                ratingType = if (DeviceState.isDeviceMicroG) "micro_g" else "native",
                score = mapStatusChipIdToRatingScore(detailsActivity.submitStatusCheckedChipId),
                notes = detailsActivity.submitNotes
            )
        
        postMyRatingRoot = PostMyRatingRoot(rating)
        
        uploadData()
        
        // Done/Retry
        bottomSheetBinding.doneButton.apply {
            setOnClickListener {
                if (ratingCreated && postedRatingId.isNotBlank()) {
                    dismiss()
                    detailsActivity.finishAfterTransition()
                }
                else {
                    isVisible = false
                    bottomSheetBinding.cancelButton.isVisible = false
                    changeAnimView(R.raw.lottie_uploading, true)
                    uploadData()
                }
            }
        }
        
        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { dismiss() }
    }
    
    private fun uploadData() {
        lifecycleScope.launch {
            if (!detailsActivity.app.isInPlexusData) {
                iconUrl = getIconUrl()
                postAppRoot.postApp.iconUrl = iconUrl
                postApp()
            }
            else postRating()
            
            if (ratingCreated && postedRatingId.isNotBlank()) {
                updateMyRatingInDb(rating)
                get<MainDataRepository>().updateSingleApp(packageName = detailsActivity.app.packageName)
                DataState.isDataUpdated = true
                changeAnimView(R.raw.lottie_success, true, scale = 1.7f)
                bottomSheetBinding.submitStatusText.text = getString(R.string.submit_success)
                bottomSheetBinding.heartView.apply {
                    isVisible = true
                    playAnimation()
                }
                bottomSheetBinding.thanksText.isVisible = true
                bottomSheetBinding.doneButton.apply {
                    text = getString(R.string.done)
                    isVisible = true
                }
                bottomSheetBinding.cancelButton.isVisible = false
            }
        }
    }
    
    private fun changeAnimView(rawRes: Int,
                               loop: Boolean,
                               scale: Float = 1.5f) {
        bottomSheetBinding.animView.apply {
            setAnimation(rawRes)
            repeatCount = if (loop) LottieDrawable.INFINITE else 0
            scaleX = scale
            scaleY = scale
            playAnimation()
        }
    }
    
    private suspend fun postApp() {
        val postAppResponse = withContext(Dispatchers.IO) {
            apiRepository.postApp(deviceToken, postAppRoot)
        }
        when (postAppResponse.status.value) {
            401 -> {
                // Unauthorized, renew token
                renewDeviceToken()
                postApp()
            }
            in (200 .. 299), 422 -> {
                // Request was successful or app already exists
                postRating()
            }
            else -> onPostFailed(postAppResponse.status.value.toString()) // Request failed
        }
    }
    
    private suspend fun postRating() {
        val postRatingResponse =
            apiRepository.postMyRating(deviceToken, detailsActivity.app.packageName, postMyRatingRoot)
        
        when (postRatingResponse.status.value) {
            401 -> {
                // Unauthorized, renew token
                renewDeviceToken()
                postRating()
            }
            in (200 .. 299) -> {
                // Request was successful
                ratingCreated = true
                val responseBody = postRatingResponse.bodyAsText()
                val jsonElement = Json.parseToJsonElement(responseBody)
                val dataObject = jsonElement.jsonObject["data"]?.jsonObject
                dataObject?.let {
                    postedRatingId = it["id"]!!.jsonPrimitive.content
                    postedRatingDateTime = it["rated_at"]!!.jsonPrimitive.content
                    encDeleteTokenB64 =
                        get<KeyStoreManager>().encryptToken(
                            it["delete_token"]!!.jsonPrimitive.content
                        )
                }
            }
            else -> onPostFailed(postRatingResponse.status.value.toString()) // Request failed
        }
    }
    
    private fun onPostFailed(responseCode: String) {
        changeAnimView(R.raw.lottie_error, false)
        bottomSheetBinding.submitStatusText.text = "${getString(R.string.submit_error)}: $responseCode"
        bottomSheetBinding.doneButton.apply {
            text = getString(R.string.retry)
            isVisible = true
        }
        bottomSheetBinding.cancelButton.isVisible = true
    }
    
    private suspend fun parseIconUrlFromWebpage(url: String): String? {
        try {
            val document = Ksoup.parseGetRequest(url)
            return Ksoup.parseMetaData(document).ogImage
        }
        catch (_: Exception) {
            return null
        }
    }
    
    private suspend fun getIconUrl(): String? {
        val url = parseIconUrlFromWebpage("${getString(R.string.fdroid_url)}${detailsActivity.app.packageName}/")
        
        // Sometimes on F-Droid when the original icon of the app is not provided,
        // we get the F-Droid logo as the icon.
        // Example: Briar (https://f-droid.org/en/packages/org.briarproject.briar.android/)
        // When this happens, check if icon exists on Google Play Store
        return if (url?.startsWith("https://f-droid.org/assets/fdroid-logo") == true) {
            parseIconUrlFromWebpage("${getString(R.string.google_play_url)}${detailsActivity.app.packageName}")
        }
        else url
    }
    
    private suspend fun renewDeviceToken() {
        try {
            val renewDeviceTokenResponse = apiRepository.renewDevice(deviceToken)
            renewDeviceTokenResponse.deviceTokenData?.let {
                deviceToken = it.token
                encPrefManager.setString(DEVICE_TOKEN, it.token)
            } ?: throw Exception(renewDeviceTokenResponse.errors?.errorDetail ?: getString(R.string.submit_error))
        }
        catch (e: Exception) {
            onPostFailed(e.toString())
        }
    }
    
    private suspend fun updateMyRatingInDb(rating: PostMyRating) {
        val myRatingDetails =
            MyRatingDetails(
                id = postedRatingId,
                encDeleteTokenBase64 = encDeleteTokenB64,
                version = rating.version,
                buildNumber = rating.buildNumber,
                romName = rating.romName,
                romBuild = rating.romBuild,
                androidVersion = rating.androidVersion,
                installedFrom = detailsActivity.app.installedFrom,
                googleLib = rating.ratingType,
                myRatingScore = rating.score,
                notes = rating.notes,
                myRatingDateTime = postedRatingDateTime
            )
        
        get<MyRatingsRepository>().insertOrUpdateMyRatings(
            name = detailsActivity.app.name,
            packageName = detailsActivity.app.packageName,
            iconUrl = iconUrl,
            myRatingDetails = myRatingDetails
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}