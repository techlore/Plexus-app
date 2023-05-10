/*
 * Copyright (c) 2022 Techlore
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

package tech.techlore.plexus.activities

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
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
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivitySubmitBinding
import tech.techlore.plexus.fragments.dialogs.NoNetworkDialog
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.models.post.PostApp
import tech.techlore.plexus.models.post.PostAppRoot
import tech.techlore.plexus.models.post.PostRating
import tech.techlore.plexus.models.post.PostRatingRoot
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEVICE_IS_MICROG
import tech.techlore.plexus.preferences.PreferenceManager.Companion.SUBMIT_SUCCESSFUL
import tech.techlore.plexus.utils.DbUtils.Companion.truncatedDgScore
import tech.techlore.plexus.utils.DbUtils.Companion.truncatedMgScore
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore

class SubmitActivity : AppCompatActivity() {
    
    private lateinit var activityBinding: ActivitySubmitBinding
    private lateinit var currentApp: MainData
    private lateinit var nameString: String
    private lateinit var packageNameString: String
    private lateinit var installedVersion: String
    private var installedVersionBuild = 0
    private var isInPlexusData = true
    private var isMicroG = false
    private lateinit var snackbar: Snackbar
    private var appCreated = false
    private var ratingCreated = false
    private var postedRatingId: String? = null
    private var iconUrl: String? = null
    
    // This is to disable snackbar swipe
    private inner class NoSwipeBehavior : BaseTransientBottomBar.Behavior() {
        override fun canSwipeDismissView(child: View): Boolean {
            return false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivitySubmitBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        nameString = intent.getStringExtra("name")!!
        packageNameString = intent.getStringExtra("packageName")!!
        installedVersion = intent.getStringExtra("installedVersion")!!
        installedVersionBuild = intent.getIntExtra("installedBuild", 0)
        isInPlexusData = intent.getBooleanExtra("isInPlexusData", true)
        isMicroG = PreferenceManager(this).getBoolean(DEVICE_IS_MICROG)
        val regexPattern = """^(?!.*(.+)\1{2,}).*$""".toRegex() // *insert regex meme here*
        // This regex prevents words like AAAAA, BBBBB, ABBBB, ABABABAB etc
        // while still allowing real words like coffee, committee etc.
        val blockedWords = resources.getStringArray(R.array.blocked_words)
        val blockedWordsPattern = blockedWords.joinToString("|") { Regex.escape(it) }
        val blockedWordsRegex =
            "(?i)\\b($blockedWordsPattern)\\b".toRegex(setOf(RegexOption.IGNORE_CASE))// *next regex meme goes here*
        
        snackbar =
            Snackbar
                .make(activityBinding.submitCoordinatorLayout,
                      getString(R.string.please_wait),
                      Snackbar.LENGTH_INDEFINITE)
                .setAnchorView(activityBinding.submitBottomAppBar)
                .setBehavior(NoSwipeBehavior())
        
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.submitBottomAppBar)
        activityBinding.submitBottomAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        // Icon
        try{
            activityBinding.submitAppIcon.setImageDrawable(packageManager.getApplicationIcon(packageNameString))
        }
        catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        
        activityBinding.submitName.text = nameString
        activityBinding.submitPackageName.text = packageNameString
        @SuppressLint("SetTextI18n")
        activityBinding.submitInstalledVersion.text = "${getString(R.string.installed)}: ${installedVersion.ifEmpty { getString(R.string.na) }}"
        activityBinding.dgMgText.text = if (isMicroG) getString(R.string.microG) else getString(R.string.de_Googled)
        
        // Notes
        activityBinding.submitNotesBox.hint = "${getString(R.string.notes)} (${getString(R.string.optional)})"
        activityBinding.submitNotesText.addTextChangedListener(object : TextWatcher {
            
            var delayTimer: CountDownTimer? = null
            
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                
                // Introduce a subtle delay
                // so text is checked after typing is finished
                if (delayTimer != null) {
                    delayTimer!!.cancel()
                }
                
                delayTimer = object : CountDownTimer(300, 100) {
                    
                    override fun onTick(millisUntilFinished: Long) {}
                    
                    // On timer finish, perform task
                    override fun onFinish() {
                        val text = activityBinding.submitNotesText.text.toString()
                        
                        // Check for blocked words
                        val hasBlockedWord = blockedWordsRegex.find(text) != null
                        
                        activityBinding.submitFab.isEnabled =
                            text.isEmpty()
                            || (text.length in 5..300
                                && !hasBlockedWord
                                && regexPattern.matches(text))
                    }
                    
                }.start()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // FAB
        activityBinding.submitFab.setOnClickListener {
            submitData()
        }
    }
    
    private fun submitData() {
        
        lifecycleScope.launch {
            if (hasNetwork(this@SubmitActivity) && hasInternet()) {
    
                activityBinding.submitFab.isEnabled = false
                snackbar.show()
                
                val apiRepository = (applicationContext as ApplicationManager).apiRepository
                val mainRepository = (applicationContext as ApplicationManager).mainRepository
                
                val rating = PostRating(version = installedVersion,
                                        buildNumber = installedVersionBuild,
                                        googleLib = if (isMicroG) "micro_g" else "none",
                                        score = mapStatusChipIdToRatingScore(activityBinding.submitStatusChipGroup.checkedChipId),
                                        notes= activityBinding.submitNotesText.text.toString())
                
                val postRatingRoot = PostRatingRoot(rating)
                val postRatingCall = apiRepository.postRating(packageNameString, postRatingRoot)
                
                if (!isInPlexusData) {
                    getIconUrl()
                    val app = PostApp(name = nameString, packageName = packageNameString, iconUrl = iconUrl)
                    val postAppRoot = PostAppRoot(app)
                    val postAppCall = apiRepository.postApp(postAppRoot)
                    postApp(postAppCall)
                    if (appCreated) {
                        isInPlexusData = true
                        currentApp = mainRepository.getAppByPackage(packageNameString)!!
                        currentApp.isInPlexusData = true
                        mainRepository.updateIsInPlexusData(currentApp)
                        postRating(postRatingCall)
                    }
                }
                else {
                    postRating(postRatingCall)
                }
                
                if (ratingCreated && !postedRatingId.isNullOrBlank()) {
                    updateMyRatingInDb(rating)
                    val singleAppCall = apiRepository.getSingleAppWithScores(packageNameString)
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
                                                                   totalMgRatings = appData.scores[0].totalRatings))
                        }
                    }
                    PreferenceManager(this@SubmitActivity).setBoolean(SUBMIT_SUCCESSFUL, true)
                    submitSnackbar(getString(R.string.submit_success))
                }
            }
            else {
                NoNetworkDialog(negativeButtonText = getString(R.string.cancel),
                                positiveButtonClickListener = { submitData() },
                                negativeButtonClickListener = {})
                    .show(supportFragmentManager, "NoNetworkDialog")
            }
        }
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
            submitSnackbar("${getString(R.string.submit_error)}: ${response.code()}")
            activityBinding.submitFab.isEnabled = true
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
            submitSnackbar("${getString(R.string.submit_error)}: ${response.code()}")
            activityBinding.submitFab.isEnabled = true
        }
    }
    
    private suspend fun getIconUrl(): String? {
        val document =
            try {
                withContext(Dispatchers.IO) {
                    Jsoup.connect("https://play.google.com/store/apps/details?id=$packageNameString").get()
                }
            }
            catch (e: HttpStatusException) {
                // If 404 error from Google Play,
                // then try connecting to F-Droid
                try {
                    withContext(Dispatchers.IO) {
                        Jsoup.connect("https://f-droid.org/en/packages/$packageNameString").get()
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
        val myRatingsRepository = (applicationContext as ApplicationManager).myRatingsRepository
        val myRating = MyRating(id = postedRatingId!!,
                                name = nameString,
                                packageName = packageNameString,
                                iconUrl = iconUrl,
                                version = rating.version,
                                buildNumber = rating.buildNumber,
                                googleLib = rating.googleLib,
                                ratingScore = rating.score,
                                notes = rating.notes)
        myRatingsRepository.insertOrUpdateMyRatings(myRating)
    }
    
    private fun submitSnackbar(message: String) {
        snackbar.setText(message)
        snackbar.duration = BaseTransientBottomBar.LENGTH_SHORT
        snackbar.setAction(getString(R.string.done)) {
            finish()
        }
    }
    
    // Set transition when finishing activity
    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
    }
}