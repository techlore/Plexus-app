package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.Utility.SendListsIntent;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

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
import tech.techlore.plexus.R;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;
    private String jsonData;
    private List<PlexusData> plexusDataList;
    private List<InstalledApp> installedAppsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        plexusDataList = new ArrayList<>();
        installedAppsList = new ArrayList<>();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        /*###########################################################################################*/

        executor.execute(() -> {

            // BACKGROUND THREAD WORK
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

        // SCAN INSTALLED APPS
        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {

            InstalledApp installedApp = new InstalledApp();
            String dgRating = "X", mgRating = "X", dgNotes = "X", mgNotes = "X";

            // NO SYSTEM APPS
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {

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
}
