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
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import retrofit2.Call
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.ActivitySubmitBinding
import tech.techlore.plexus.fragments.dialogs.NoNetworkDialog
import tech.techlore.plexus.models.get.main.MainData
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.models.post.PostApp
import tech.techlore.plexus.models.post.PostAppRoot
import tech.techlore.plexus.models.post.PostRating
import tech.techlore.plexus.models.post.PostRatingRoot
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.DEVICE_IS_MICROG
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipToRatingScore
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar
import kotlin.coroutines.CoroutineContext

class SubmitActivity : AppCompatActivity(), CoroutineScope {
    
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private lateinit var activityBinding: ActivitySubmitBinding
    private lateinit var currentApp: MainData
    private lateinit var nameString: String
    private lateinit var packageNameString: String
    private lateinit var installedVersion: String
    private var installedVersionBuild = 0
    private var isInPlexusData = true
    private var isMicroG = false
    private var appCreated = false
    private var ratingCreated = false
    private var postedRatingId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivitySubmitBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        nameString = intent.getStringExtra("name")!!
        packageNameString = intent.getStringExtra("packageName")!!
        installedVersion = intent.getStringExtra("installedVersion")!!
        installedVersionBuild = intent.getIntExtra("installedVersionBuild", 0)
        isInPlexusData = intent.getBooleanExtra("isInPlexusData", true)
        isMicroG = PreferenceManager(this).getBoolean(DEVICE_IS_MICROG)
        val regexPattern = """^(?!.*(.+)\1{2,}).*$""".toRegex() // *insert regex meme here*
        // This regex prevents words like AAAAA, BBBBB, ABBBB, ABABABAB etc
        // while still allowing real words like coffee, committee etc.
        val blockedWords = resources.getStringArray(R.array.blocked_words)
        val blockedWordsPattern = blockedWords.joinToString("|") { Regex.escape(it) }
        val blockedWordsRegex =
            "(?i)\\b($blockedWordsPattern)\\b".toRegex(setOf(RegexOption.IGNORE_CASE))// *next regex meme goes here*
        
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.submitBottomAppBar)
        activityBinding.submitBottomAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        activityBinding.submitName.text = nameString
        activityBinding.submitPackageName.text = packageNameString
        @SuppressLint("SetTextI18n")
        activityBinding.submitInstalledVersion.text = "${getString(R.string.installed)}: ${installedVersion.ifEmpty { getString(R.string.not_tested_title) }}"
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
                            || ((text.length > 4)
                                && text.length <= 300
                                && !hasBlockedWord
                                && regexPattern.matches(text))
                    }
                    
                }.start()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // FAB
        activityBinding.submitFab.setOnClickListener {
            activityBinding.submitFab.isEnabled = false
            submitData()
        }
    }
    
    private fun submitData() {
        
        launch {
            if (hasNetwork(this@SubmitActivity) && hasInternet()) {
                
                val apiRepository = (applicationContext as ApplicationManager).apiRepository
                
                val rating = PostRating(version = installedVersion,
                                        buildNumber = installedVersionBuild,
                                        googleLib = if (isMicroG) "micro_g" else "none",
                                        score = mapStatusChipToRatingScore(activityBinding.submitStatusChipGroup.checkedChipId),
                                        notes= activityBinding.submitNotesText.text.toString())
                
                val postRatingRoot = PostRatingRoot(rating)
                val postRatingCall = apiRepository.postRating(packageNameString, postRatingRoot)
                
                if (!isInPlexusData) {
                    val app = PostApp(name = nameString, packageName = packageNameString, iconUrl = getIconUrl())
                    val postAppRoot = PostAppRoot(app)
                    val postAppCall = apiRepository.postApp(postAppRoot)
                    postApp(postAppCall)
                    if (appCreated) {
                        isInPlexusData = true
                        val mainRepository = (applicationContext as ApplicationManager).mainRepository
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
                    updateRatingInDb(rating)
                    showSnackbar(activityBinding.submitCoordinatorLayout,
                                 getString(R.string.submit_success),
                                 activityBinding.submitBottomAppBar)
                }
            }
            else {
                NoNetworkDialog(negativeButtonText = getString(R.string.exit),
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
            showSnackbar(activityBinding.submitCoordinatorLayout,
                         "${getString(R.string.submit_error)}: ${response.code()}",
                         activityBinding.submitBottomAppBar)
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
            postedRatingId = dataObject.getString("id")
        }
        else {
            // Request failed
            showSnackbar(activityBinding.submitCoordinatorLayout,
                         "${getString(R.string.submit_error)}: ${response.code()}",
                         activityBinding.submitBottomAppBar)
            activityBinding.submitFab.isEnabled = true
        }
    }
    
    private suspend fun getIconUrl(): String {
        val document = withContext(Dispatchers.IO) {
            Jsoup.connect("https://play.google.com/store/apps/details?id=$packageNameString")
                .get()
        }
        val element = document.selectFirst("meta[content^=https://play-lh]")
        return element!!.attr("content")
        
    }
    
    private suspend fun updateRatingInDb(rating: PostRating) {
        val myRatingsRepository = (applicationContext as ApplicationManager).myRatingsRepository
        val myRating = MyRating(id = postedRatingId!!,
                                packageName = packageNameString,
                                version = rating.version,
                                buildNumber = rating.buildNumber,
                                googleLib = rating.googleLib,
                                ratingScore = rating.score,
                                notes = rating.notes)
        myRatingsRepository.insertOrUpdateMyRatings(myRating)
    }
    
    // Set transition when finishing activity
    override fun finish() {
        super.finish()
        job.cancel()
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
    }
}