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

package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.NetworkUtils.HasInternet;
import static tech.techlore.plexus.utils.NetworkUtils.HasNetwork;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.ActivitySubmitBinding;

public class SubmitActivity extends AppCompatActivity {
    
    private ActivitySubmitBinding activityBinding;
    private String nameString, packageNameString, plexusVersionString,
            dgStatusString, mgStatusString, dgNotesString, mgNotesString;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        activityBinding = ActivitySubmitBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());
    
        Intent intent = getIntent();
        nameString = intent.getStringExtra("name");
        packageNameString = intent.getStringExtra("packageName");
    
        /*########################################################################################*/
        
        setSupportActionBar(activityBinding.submitBottomAppBar);
        activityBinding.submitBottomAppBar.setNavigationOnClickListener(v -> onBackPressed());
    
        activityBinding.submitName.setText(nameString);
        activityBinding.submitPackageName.setText(packageNameString);
        
        // FAB
        activityBinding.submitFab.setEnabled(false); // Temporary, will be removed
        activityBinding.submitFab.setOnClickListener(v -> SubmitData());
        
    }
    
    private void NoNetworkDialog() {
    
        new MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_subtitle)
            
                .setPositiveButton(R.string.retry, (dialog, which) ->
                        SubmitData())
            
                .setNegativeButton(R.string.cancel, (dialog, which) ->
                        dialog.dismiss())

                .setCancelable(false)
            
                .show();
        
    }
    
    private void SubmitData(){
        
        Handler handler = new Handler(Looper.getMainLooper());
        
        if (HasNetwork(this)) {
            
            Executors.newSingleThreadExecutor().execute(() -> {
                
                // Background thread work
                if (HasInternet()) {
    
                    OkHttpClient client = new OkHttpClient();
    
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
                    }
                    
                    // UI Thread work
                    handler.post(() ->
                         Snackbar.make(activityBinding.submitCoordinatorLayout,
                                       getString(R.string.submit_success),
                                       BaseTransientBottomBar.LENGTH_SHORT)
                                 .setAnchorView(activityBinding.submitBottomAppBar) // Above FAB, bottom bar etc.
                                 .show());
                }
                else {
                    handler.post(this::NoNetworkDialog);
                }
            });
        }
        else {
            NoNetworkDialog();
        }
        
    }
    
    // Set transition when finishing activity
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom);
    }
    
}
