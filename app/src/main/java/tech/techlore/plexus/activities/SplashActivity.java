package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.Utility.SendListsIntent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tech.techlore.plexus.R;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;
    private String jsonData;
    private List<PlexusData> plexusDataList;
    private List<InstalledApp> installedAppsList;
    private ExecutorService executor;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        plexusDataList = new ArrayList<>();
        installedAppsList = new ArrayList<>();
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        /*###########################################################################################*/

        FetchData();

    }

    // CHECK NETWORK AVAILABILITY
    private boolean HasNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED);

    }

    // CHECK IF NETWORK HAS INTERNET CONNECTION
    private boolean HasInternet() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("github.com", 443),
                    1500);
            socket.close();

            return true;
        }
        catch (IOException e) {
            return false;
        }
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
        view.findViewById(R.id.dialog_negative_button)
                .setOnClickListener(view12 -> {
                    dialog.cancel();
                    finishAndRemoveTask();
                });

        // SHOW DIALOG WITH CUSTOM ANIMATION
        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
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

    private void DoInBackground() throws IOException {
        okHttpClient = new OkHttpClient();
        jsonData = URLRequest();
    }

    // POPULATE PLEXUS DATA LIST
    private void PopulateDataList() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        plexusDataList = objectMapper.readValue(jsonData, new TypeReference<List<PlexusData>>(){});
    }

    // SCAN ALL INSTALLED APPS AND POPULATE RESPECTIVE LIST
    private void ScanInstalledApps() {

        PackageManager packageManager = getPackageManager();

        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {

            InstalledApp installedApp = new InstalledApp();
            String dgRating = "X", mgRating = "X", dgNotes = "X", mgNotes = "X";

            // NO SYSTEM APPS
            // ONLY SCAN FOR USER INSTALLED APPS
            // OR SYSTEM APPS THAT WERE UPDATED BY USER
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1
                || (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) !=0) {

                installedApp.setName(String.valueOf(appInfo.loadLabel(packageManager)));
                installedApp.setPackageName(appInfo.packageName);

                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                    installedApp.setVersion(packageInfo.versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                // SEARCH FOR THE PACKAGE NAME IN PLEXUS DATA
                // TO SET RATINGS AND NOTES
                for (PlexusData plexusData : plexusDataList) {

                    if (plexusData.packageName.contains(appInfo.packageName)) {
                        dgRating = plexusData.dgRating;
                        mgRating = plexusData.mgRating;
                        dgNotes = plexusData.dgNotes;
                        mgNotes = plexusData.mgNotes;
                    }

                }

                installedApp.setDgRating(dgRating);
                installedApp.setMgRating(mgRating);
                installedApp.setDgNotes(dgNotes);
                installedApp.setMgNotes(mgNotes);
                installedAppsList.add(installedApp);
            }
        }

    }

    private void FetchData(){

        if (HasNetwork()) {

            executor.execute(() -> {

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
                            ScanInstalledApps();
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
