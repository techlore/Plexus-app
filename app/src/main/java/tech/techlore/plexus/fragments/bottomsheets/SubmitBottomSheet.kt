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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.awaitResponse
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SubmitActivity
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.BottomSheetSubmitBinding
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.models.post.PostApp
import tech.techlore.plexus.models.post.PostAppRoot
import tech.techlore.plexus.models.post.PostRating
import tech.techlore.plexus.models.post.PostRatingRoot
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEVICE_ROM
import tech.techlore.plexus.utils.DbUtils.Companion.truncatedDgScore
import tech.techlore.plexus.utils.DbUtils.Companion.truncatedMgScore
import tech.techlore.plexus.utils.SystemUtils.Companion.mapAndroidVersionIntToString
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore

@SuppressLint("SetTextI18n")
class SubmitBottomSheet : BottomSheetDialogFragment() {
    
    private lateinit var bottomSheetBinding: BottomSheetSubmitBinding
    private lateinit var submitActivity: SubmitActivity
    private var appCreated = false
    private var ratingCreated = false
    private var postedRatingId: String? = null
    private var iconUrl: String? = null
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
    
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.behavior.isDraggable = false
    
        // Prevent bottom sheet dismiss on back pressed
        bottomSheetDialog.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // Do nothing
                return@OnKeyListener true
            }
            false
        })
        
        bottomSheetBinding = BottomSheetSubmitBinding.inflate(inflater, container, false)
        submitActivity = requireActivity() as SubmitActivity
        submitData()
    
        // Done/Retry
        bottomSheetBinding.doneButton.setOnClickListener {
            if (ratingCreated && !postedRatingId.isNullOrBlank()) {
                dismiss()
                submitActivity.finish()
            }
            else {
                bottomSheetBinding.doneButton.isVisible = false
                bottomSheetBinding.cancelButton.isVisible = false
                changeAnimView(R.raw.lottie_uploading, true)
                submitData()
            }
        }
        
        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener {
            dismiss()
        }
        
        return bottomSheetBinding.root
    }
    
    private fun submitData() {
        lifecycleScope.launch {
            val appManager = requireContext().applicationContext as ApplicationManager
            val apiRepository = appManager.apiRepository
            val mainRepository = appManager.mainRepository
        
            val rating = PostRating(version = submitActivity.installedVersion,
                                    buildNumber = submitActivity.installedBuild,
                                    romName = PreferenceManager(requireContext()).getString(DEVICE_ROM)!!,
                                    romBuild = Build.DISPLAY,
                                    androidVersion = mapAndroidVersionIntToString(Build.VERSION.SDK_INT),
                                    installedFrom = submitActivity.installedFromString,
                                    googleLib = if (appManager.deviceIsMicroG) "micro_g" else "none",
                                    score = mapStatusChipIdToRatingScore(submitActivity.activityBinding.submitStatusChipGroup.checkedChipId),
                                    notes = submitActivity.activityBinding.submitNotesText.text.toString())
        
            val postRatingRoot = PostRatingRoot(rating)
            val postRatingCall = apiRepository.postRating(submitActivity.packageNameString, postRatingRoot)
        
            if (!submitActivity.isInPlexusData) {
                getIconUrl()
                val app = PostApp(name = submitActivity.nameString,
                                  packageName = submitActivity.packageNameString,
                                  iconUrl = iconUrl)
                val postAppRoot = PostAppRoot(app)
                val postAppCall = apiRepository.postApp(postAppRoot)
                postApp(postAppCall)
                if (appCreated) {
                    submitActivity.isInPlexusData = true
                    postRating(postRatingCall)
                }
            }
            else {
                postRating(postRatingCall)
            }
        
            if (ratingCreated && !postedRatingId.isNullOrBlank()) {
                updateMyRatingInDb(rating)
                val singleAppCall = apiRepository.getSingleAppWithScores(submitActivity.packageNameString)
                val singleAppResponse = singleAppCall.awaitResponse()
                if (singleAppResponse.isSuccessful) {
                    singleAppResponse.body()?.let { getSingleAppRoot ->
                        val appData = getSingleAppRoot.appData
                        mainRepository
                            .insertOrUpdatePlexusData(MainData(name = appData.name,
                                                               packageName = appData.packageName,
                                                               iconUrl = appData.iconUrl ?: "",
                                                               dgScore = truncatedDgScore(appData.scores[1].score),
                                                               totalDgRatings = appData.scores[1].totalRatings,
                                                               mgScore = truncatedMgScore(appData.scores[0].score),
                                                               totalMgRatings = appData.scores[0].totalRatings,
                                                               isInPlexusData = true))
                    }
                }
                appManager.submitSuccessful = true
                changeAnimView(R.raw.lottie_success, false)
                bottomSheetBinding.submitStatusText.text = getString(R.string.submit_success)
                bottomSheetBinding.heartView.isVisible = true
                bottomSheetBinding.heartView.playAnimation()
                bottomSheetBinding.thanksText.isVisible = true
                bottomSheetBinding.doneButton.text = getString(R.string.done)
                bottomSheetBinding.doneButton.isVisible = true
                bottomSheetBinding.cancelButton.isVisible = false
            }
        }
    }
    
    private fun changeAnimView(rawRes: Int, loop: Boolean) {
        bottomSheetBinding.animView.setAnimation(rawRes)
        bottomSheetBinding.animView.repeatCount = if (loop) LottieDrawable.INFINITE else 0
        bottomSheetBinding.animView.playAnimation()
    }
    
    private suspend fun postApp(postAppCall: Call<ResponseBody>) {
        val response = withContext(Dispatchers.IO) {
            postAppCall.execute()
        }
        if (response.isSuccessful || response.code() == 422) {
            // Request was successful or app already exists
            appCreated = true
        }
        else {
            // Request failed
            changeAnimView(R.raw.lottie_error, false)
            bottomSheetBinding.submitStatusText.text = "${getString(R.string.submit_error)}: ${response.code()}"
            bottomSheetBinding.doneButton.text = getString(R.string.retry)
            bottomSheetBinding.doneButton.isVisible = true
            bottomSheetBinding.cancelButton.isVisible = true
            submitActivity.activityBinding.submitFab.isEnabled = true
        }
    }
    
    private suspend fun postRating(postRatingCall: Call<ResponseBody>) {
        val response = withContext(Dispatchers.IO) {
            postRatingCall.execute()
        }
        if (response.isSuccessful) {
            // Request was successful
            ratingCreated = true
            val responseBody = response.body()!!.string()
            val jsonObject = JSONObject(responseBody)
            val dataObject = jsonObject.getJSONObject("data")
            postedRatingId = dataObject.getString("id") // Store id of the posted rating
        }
        else {
            // Request failed
            changeAnimView(R.raw.lottie_error, false)
            bottomSheetBinding.submitStatusText.text = "${getString(R.string.submit_error)}: ${response.code()}"
            bottomSheetBinding.doneButton.text = getString(R.string.retry)
            bottomSheetBinding.doneButton.isVisible = true
            bottomSheetBinding.cancelButton.isVisible = true
            submitActivity.activityBinding.submitFab.isEnabled = true
        }
    }
    
    private suspend fun getIconUrl(): String? {
        val document =
            try {
                withContext(Dispatchers.IO) {
                    Jsoup.connect("https://play.google.com/store/apps/details?id=${submitActivity.packageNameString}").get()
                }
            }
            catch (e: HttpStatusException) {
                // If 404 error from Google Play,
                // then try connecting to F-Droid
                try {
                    withContext(Dispatchers.IO) {
                        Jsoup.connect("https://f-droid.org/en/packages/${submitActivity.packageNameString}").get()
                    }
                }
                catch (e: HttpStatusException) {
                    null
                }
            }
        
        if (document != null) {
            val element = document.selectFirst("meta[property=og:image]")
            val url = element?.attr("content")
            // Sometimes on F-Droid when original icon of the app is not provided,
            // we get F-Droid logo as the icon.
            // Example: Fennec (https://f-droid.org/en/packages/org.mozilla.fennec_fdroid)
            // When this happens, assign iconUrl to null
            iconUrl =
                if (url?.startsWith("https://f-droid.org/assets/fdroid-logo") == true) {
                    null
                }
                else {
                    url
                }
        }
        
        return iconUrl
        
    }
    
    private suspend fun updateMyRatingInDb(rating: PostRating) {
        val myRatingsRepository = (requireContext().applicationContext as ApplicationManager).myRatingsRepository
        myRatingsRepository
            .insertOrUpdateMyRatings(MyRating(id = postedRatingId!!,
                                              name = submitActivity.nameString,
                                              packageName = submitActivity.packageNameString,
                                              iconUrl = iconUrl,
                                              version = rating.version,
                                              buildNumber = rating.buildNumber,
                                              romName = rating.romName,
                                              romBuild = rating.romBuild,
                                              androidVersion = rating.androidVersion,
                                              installedFrom = submitActivity.installedFromString,
                                              googleLib = rating.googleLib,
                                              ratingScore = rating.score,
                                              notes = rating.notes))
    }
    
}