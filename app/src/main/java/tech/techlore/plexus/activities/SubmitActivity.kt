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

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivitySubmitBinding
import tech.techlore.plexus.models.Application
import tech.techlore.plexus.utils.ApiUtils.Companion.createService
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class SubmitActivity : AppCompatActivity(), CoroutineScope {
    
    private lateinit var activityBinding: ActivitySubmitBinding
    private val job = Job()
    private lateinit var nameString: String
    private lateinit var packageNameString: String
    /*private lateinit var plexusVersionString: String
    private lateinit var dgStatusString: String
    private lateinit var mgStatusString: String
    private lateinit var dgNotesString: String
    private lateinit var mgNotesString: String*/
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivitySubmitBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        nameString = intent.getStringExtra("name")!!
        packageNameString = intent.getStringExtra("packageName")!!
        
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.submitBottomAppBar)
        activityBinding.submitBottomAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        activityBinding.submitName.text = nameString
        activityBinding.submitPackageName.text = packageNameString
        
        // FAB
        //activityBinding.submitFab.isEnabled = false // Temporary, will be removed
        activityBinding.submitFab.setOnClickListener { submitData() }
    }
    
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    
    private fun noNetworkDialog() {
        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            
            .setTitle(R.string.dialog_title)
            
            .setMessage(R.string.dialog_subtitle)
            
            .setPositiveButton(R.string.retry) { _: DialogInterface, _: Int ->
                submitData() }
            
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
                dialog.dismiss() }
            
            .setCancelable(false)
            
            .show()
    }
    
    private fun submitData() {
        
        launch {
            if (hasNetwork(this@SubmitActivity) && hasInternet()) {
                val application = Application(name = nameString,
                                              packageName = packageNameString)
                val call = createService().sendApplication(application)
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        // handle the response here
                    }
        
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // handle the failure here
                    }
                })
            }
            else {
                noNetworkDialog()
            }
        }
        
        /*val handler = Handler(Looper.getMainLooper())
        
        if (hasNetwork(this)) {
            
            Executors.newSingleThreadExecutor().execute {
                
                // Background thread work
                if (hasInternet()) {
                    
                    val application = Application(name = nameString,
                                                  packageName = packageNameString)
                    val call = createService().sendApplication(application)
                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            // handle the response here
                        }
        
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            // handle the failure here
                        }
                    })


*//*                    OkHttpClient client = new OkHttpClient();

                    // Create a JSON object with the data
                    ObjectMapper mapper = new ObjectMapper();

                    ObjectNode application = mapper.createObjectNode();
                    application.put("name", nameString);
                    application.put("package", packageNameString);

                    ObjectNode data = mapper.createObjectNode();
                    data.set("application", application);

                    // Convert the JSON object to a string
                    String json = null;
                    try {
                        json = mapper.writeValueAsString(data);
                    }
                    catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    // Create a request body with the JSON string as the content
                    assert json != null;
                    RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

                    // Create a request builder and set the request method to POST
                    Request request = new Request.Builder()
                            .url("https://plexus.fly.dev/api/v1/applications")
                            .post(requestBody)
                            .build();

                    try {
                        client.newCall(request).execute();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }*//*
                    
                    // UI Thread work
                    handler.post {
                        Snackbar.make(activityBinding.submitCoordinatorLayout,
                                      getString(R.string.submit_success),
                                      BaseTransientBottomBar.LENGTH_SHORT)
                            .setAnchorView(activityBinding.submitBottomAppBar) // Above FAB, bottom bar etc.
                            .show()
                    }
                }
                else {
                    handler.post { noNetworkDialog() }
                }
            }
        }
        else {
            noNetworkDialog()
        }*/
    }
    
    // Set transition when finishing activity
    override fun finish() {
        super.finish()
        job.cancel()
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom)
    }
}