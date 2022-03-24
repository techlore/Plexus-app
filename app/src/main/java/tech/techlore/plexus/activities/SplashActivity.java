package tech.techlore.plexus.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tech.techlore.plexus.models.PlexusData;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;
    public String jsonData;
    public List<PlexusData> plexusDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        plexusDataList = new ArrayList<>();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

    /*###########################################################################################*/

        executor.execute(() -> {

            // BACKGROUND THREAD WORK
            try {
                doInBackground();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // UI THREAD WORK
            handler.post(() -> {
                try {
                    populateList();
                    startActivity(new Intent(this, MainActivity.class)
                            .putExtra("plexusDataList", (Serializable) plexusDataList));
                    finish();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            });
        });
    }

    private String URLRequest() throws IOException {
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/parveshnarwal/Plexus-Demo/main/new.json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute())
        {
            return Objects.requireNonNull(response.body()).string();
        }

    }

    private void doInBackground() throws IOException {
        okHttpClient = new OkHttpClient();
        jsonData = URLRequest();
    }

    public void populateList() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        plexusDataList = objectMapper.readValue(jsonData, new TypeReference<List<PlexusData>>(){});
    }

}
