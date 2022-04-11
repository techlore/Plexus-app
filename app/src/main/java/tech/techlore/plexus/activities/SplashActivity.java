package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.Utility.HasInternet;
import static tech.techlore.plexus.utils.Utility.HasNetwork;
import static tech.techlore.plexus.utils.Utility.ScanInstalledApps;
import static tech.techlore.plexus.utils.Utility.SendListsIntent;
import static tech.techlore.plexus.utils.Utility.URLRequest;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import tech.techlore.plexus.R;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private String jsonData;
    private List<PlexusData> plexusDataList;
    private List<InstalledApp> installedAppsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        plexusDataList = new ArrayList<>();
        installedAppsList = new ArrayList<>();

        /*###########################################################################################*/

        FetchData();

    }

    // NO NETWORK DIALOG
    private void NoNetworkDialog() {
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.setCancelable(false);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.dialog_no_network, null);
        dialog.getWindow().setBackgroundDrawable(ContextCompat
                .getDrawable(this, R.drawable.shape_rounded_corners));
        dialog.getWindow().getDecorView().setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bottomSheetColor));
        dialog.setContentView(view);

        // POSITIVE BUTTON
        view.findViewById(R.id.dialog_positive_button)
                .setOnClickListener(view1 -> {
                    FetchData();
                    dialog.dismiss();
                });

        // NEGATIVE BUTTON
        TextView negativeButton = view.findViewById(R.id.dialog_negative_button);
        negativeButton.setText(getString(R.string.exit));
        negativeButton.setOnClickListener(view12 -> {
                    dialog.cancel();
                    finishAndRemoveTask();
                });

        // SHOW DIALOG WITH CUSTOM ANIMATION
        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
    }

    private void DoInBackground() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        jsonData = URLRequest(okHttpClient);
    }

    // POPULATE PLEXUS DATA LIST
    private void PopulateDataList() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        plexusDataList = objectMapper.readValue(jsonData, new TypeReference<List<PlexusData>>(){});
    }

    private void FetchData(){

        Handler handler = new Handler(Looper.getMainLooper());

        if (HasNetwork(this)) {

            Executors.newSingleThreadExecutor().execute(() -> {

                // BACKGROUND THREAD WORK
                if (HasInternet()) {

                    try {
                        DoInBackground();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // UI THREAD WORK
                    handler.post(() -> {
                        try {
                            PopulateDataList();
                            ((TextView)findViewById(R.id.progress_text)).setText(R.string.scan_installed);
                            ScanInstalledApps(this, plexusDataList, installedAppsList);
                            SendListsIntent(this, MainActivity.class,
                                    (Serializable) plexusDataList, (Serializable) installedAppsList);
                            finish();
                            overridePendingTransition(R.anim.slide_from_end, R.anim.slide_to_start);
                        }

                        catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });
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
}
