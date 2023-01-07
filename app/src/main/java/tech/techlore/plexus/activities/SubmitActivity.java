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

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.techlore.plexus.databinding.ActivitySubmitBinding;

public class SubmitActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        tech.techlore.plexus.databinding.ActivitySubmitBinding activityBinding = ActivitySubmitBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());
        
        // FAB
        activityBinding.submitFab.setOnClickListener(v -> {
            
            OkHttpClient client = new OkHttpClient();
            
            // Create a JSON object with the desired data
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode application = mapper.createObjectNode();
            application.put("name", "TestApp4");
            application.put("package", "test.package.4");
            
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
            
            Executors.newSingleThreadExecutor().execute(() -> {
                
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
                
            });
        });
        
    }
    
}
