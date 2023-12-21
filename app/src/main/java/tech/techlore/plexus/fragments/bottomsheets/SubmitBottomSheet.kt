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

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SubmitActivity
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.BottomSheetSubmitBinding
import tech.techlore.plexus.models.get.responses.VerifyDeviceResponseRoot
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.models.post.app.PostApp
import tech.techlore.plexus.models.post.app.PostAppRoot
import tech.techlore.plexus.models.post.rating.PostRating
import tech.techlore.plexus.models.post.rating.PostRatingRoot
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.preferences.EncryptedPreferenceManager.Companion.DEVICE_TOKEN
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore

@SuppressLint("SetTextI18n")
class SubmitBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetSubmitBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var submitActivity: SubmitActivity
    private lateinit var encPreferenceManager: EncryptedPreferenceManager
    private lateinit var deviceToken: String
    private lateinit var appManager: ApplicationManager
    private lateinit var apiRepository: ApiRepository
    private lateinit var postAppRoot: PostAppRoot
    private lateinit var rating: PostRating
    private lateinit var postRatingRoot: PostRatingRoot
    private var iconUrl: String? = null
    private var ratingCreated = false
    private var postedRatingId: String? = null
    
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
        
        _binding = BottomSheetSubmitBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        submitActivity = requireActivity() as SubmitActivity
        encPreferenceManager = EncryptedPreferenceManager(requireContext())
        deviceToken = encPreferenceManager.getString(DEVICE_TOKEN)!!
        appManager = requireContext().applicationContext as ApplicationManager
        apiRepository = appManager.apiRepository
        postAppRoot = PostAppRoot(PostApp(name = submitActivity.nameString,
                                          packageName = submitActivity.packageNameString,
                                          iconUrl = iconUrl))
        rating = PostRating(version = submitActivity.installedVersion,
                            buildNumber = submitActivity.installedBuild,
                            romName = encPreferenceManager.getString(DEVICE_ROM)!!,
                            romBuild = Build.DISPLAY,
                            androidVersion = getAndroidVersionString(),
                            installedFrom = submitActivity.installedFromString,
                            ratingType = if (appManager.deviceIsMicroG) "micro_g" else "native",
                            score = mapStatusChipIdToRatingScore(submitActivity.activityBinding.submitStatusChipGroup.checkedChipId),
                            notes = submitActivity.activityBinding.submitNotesText.text.toString())
        postRatingRoot = PostRatingRoot(rating)
        
        submitData()
        
        // Done/Retry
        bottomSheetBinding.doneButton.apply {
            setOnClickListener {
                if (ratingCreated && !postedRatingId.isNullOrBlank()) {
                    dismiss()
                    submitActivity.finish()
                }
                else {
                    isVisible = false
                    bottomSheetBinding.cancelButton.isVisible = false
                    changeAnimView(R.raw.lottie_uploading, true)
                    submitData()
                }
            }
        }
        
        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { dismiss() }
    }
    
    private fun submitData() {
        lifecycleScope.launch {
            val mainRepository = appManager.mainRepository
            
            if (!submitActivity.isInPlexusData) {
                getIconUrl()
                postApp()
            }
            else {
                postRating()
            }
            
            if (ratingCreated && !postedRatingId.isNullOrBlank()) {
                updateMyRatingInDb(rating)
                mainRepository.updateSingleApp(context = requireContext(),
                                               packageName = submitActivity.packageNameString)
                appManager.isDataUpdated = true
                changeAnimView(R.raw.lottie_success, false)
                bottomSheetBinding.submitStatusText.text = getString(R.string.submit_success)
                bottomSheetBinding.heartView.apply {
                    isVisible = true
                    playAnimation()
                }
                bottomSheetBinding.thanksText.isVisible = true
                bottomSheetBinding.doneButton.apply {
                    icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_done)
                    text = getString(R.string.done)
                    isVisible = true
                }
                bottomSheetBinding.cancelButton.isVisible = false
            }
        }
    }
    
    private fun changeAnimView(rawRes: Int, loop: Boolean) {
        bottomSheetBinding.animView.setAnimation(rawRes)
        bottomSheetBinding.animView.repeatCount = if (loop) LottieDrawable.INFINITE else 0
        bottomSheetBinding.animView.playAnimation()
    }
    
    private suspend fun postApp() {
        val response = withContext(Dispatchers.IO) {
            apiRepository.postApp(deviceToken, postAppRoot).execute()
        }
        if (response.code() == 401) {
            // Unauthorized, renew token
            renewDeviceToken()
            postApp()
        }
        else if (response.isSuccessful || response.code() == 422) {
            // Request was successful or app already exists
            submitActivity.isInPlexusData = true
            postRating()
        }
        else {
            // Request failed
            postFailed(response.code())
        }
    }
    
    private suspend fun postRating() {
        val response = withContext(Dispatchers.IO) {
            apiRepository
                .postRating(deviceToken,
                            submitActivity.packageNameString,
                            postRatingRoot)
                .execute()
        }
        if (response.code() == 401) {
            // Unauthorized, renew token
            renewDeviceToken()
            postRating()
        }
        else if (response.isSuccessful) {
            // Request was successful
            ratingCreated = true
            val responseBody = response.body()!!.string()
            val jsonObject = JSONObject(responseBody)
            val dataObject = jsonObject.getJSONObject("data")
            postedRatingId = dataObject.getString("id") // Store id of the posted rating
        }
        else {
            // Request failed
            postFailed(response.code())
        }
    }
    
    private fun postFailed(responseCode: Int) {
        changeAnimView(R.raw.lottie_error, false)
        bottomSheetBinding.submitStatusText.text = "${getString(R.string.submit_error)}: $responseCode"
        bottomSheetBinding.doneButton.apply {
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_retry)
            text = getString(R.string.retry)
            isVisible = true
        }
        bottomSheetBinding.cancelButton.isVisible = true
        submitActivity.activityBinding.submitFab.isEnabled = true
    }
    
    private suspend fun getIconUrl(): String? {
        var document: Document
        val fdroidUrl = "${getString(R.string.fdroid_url)}${submitActivity.packageNameString}/"
        
        try {
            document =
                withContext(Dispatchers.IO) {
                    Jsoup.connect(fdroidUrl).get()
                }
            val element = document.selectFirst("meta[property=og:image]")
            val url = element?.attr("content")
            // Sometimes on F-Droid when the original icon of the app is not provided,
            // we get the F-Droid logo as the icon.
            // Example: Fennec (https://f-droid.org/en/packages/org.mozilla.fennec_fdroid)
            // When this happens, assign throw 404 error
            if (url?.startsWith("https://f-droid.org/assets/fdroid-logo") == true) {
                throw HttpStatusException("Icon not found on F-Droid", 404, fdroidUrl)
            }
            else {
                iconUrl = url
            }
        }
        catch (e: HttpStatusException) {
            // If 404 error from F-Droid or icon not found,
            // then try connecting to Google Play
            try {
                document =
                    withContext(Dispatchers.IO) {
                        Jsoup.connect("${getString(R.string.google_play_url)}${submitActivity.packageNameString}").get()
                    }
                val element = document.selectFirst("meta[property=og:image]")
                iconUrl = element?.attr("content")
            } catch (e: HttpStatusException) {
                iconUrl = null
            }
        }
        
        return iconUrl
    }
    
    private suspend fun renewDeviceToken() {
        val response = withContext(Dispatchers.IO) {
            apiRepository.renewDevice(deviceToken).execute()
        }
        if (response.isSuccessful) {
            val verifyDeviceResponse =
                response.body()?.string()?.let {
                    jacksonObjectMapper().readValue(it, VerifyDeviceResponseRoot::class.java)
                }
            encPreferenceManager.setString(DEVICE_TOKEN, verifyDeviceResponse?.deviceToken!!.token)
            deviceToken = encPreferenceManager.getString(DEVICE_TOKEN)!!
        }
        else {
            postFailed(response.code())
        }
    }
    
    private fun getAndroidVersionString(): String {
        return when(Build.VERSION.SDK_INT) {
            23 -> "6.0"
            24 -> "7.0"
            25 -> "7.1.1"
            26 -> "8.0"
            27 -> "8.1"
            28 -> "9.0"
            29 -> "10.0"
            30 -> "11.0"
            31 -> "12.0"
            32 -> "12.1"
            33 -> "13.0"
            34 -> "14.0"
            else -> "NA" // Should never reach here
        }
    }
    
    private suspend fun updateMyRatingInDb(rating: PostRating) {
        val myRatingsRepository = (requireContext().applicationContext as ApplicationManager).myRatingsRepository
        val myRatingDetails = MyRatingDetails(id = postedRatingId!!,
                                              version = rating.version,
                                              buildNumber = rating.buildNumber,
                                              romName = rating.romName,
                                              romBuild = rating.romBuild,
                                              androidVersion = rating.androidVersion,
                                              installedFrom = submitActivity.installedFromString,
                                              googleLib = rating.ratingType,
                                              myRatingScore = rating.score,
                                              notes = rating.notes)
        
        myRatingsRepository
            .insertOrUpdateMyRatings(name = submitActivity.nameString,
                                     packageName = submitActivity.packageNameString,
                                     iconUrl = iconUrl,
                                     myRatingDetails = myRatingDetails)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}