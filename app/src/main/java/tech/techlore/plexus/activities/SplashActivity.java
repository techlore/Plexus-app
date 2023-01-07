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

import static tech.techlore.plexus.utils.IntentUtils.SendListsIntent;
import static tech.techlore.plexus.utils.NetworkUtils.HasInternet;
import static tech.techlore.plexus.utils.NetworkUtils.HasNetwork;
import static tech.techlore.plexus.utils.NetworkUtils.GETReq;
import static tech.techlore.plexus.utils.ListUtils.PopulateDataList;
import static tech.techlore.plexus.utils.ListUtils.ScanInstalledApps;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import tech.techlore.plexus.R;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private List<PlexusData> plexusDataList;
    private List<InstalledApp> installedAppsList;
    private static String jsonData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        plexusDataList = new ArrayList<>();
        installedAppsList = new ArrayList<>();

        /*###########################################################################################*/

        FetchData();

    }

    private void NoNetworkDialog() {

        new MaterialAlertDialogBuilder(this, R.style.DialogTheme)

                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_subtitle)

                .setPositiveButton(R.string.retry, (dialog, which) ->
                        FetchData())

                .setNegativeButton(R.string.exit, (dialog, which) ->
                        finishAndRemoveTask())
                
                .setCancelable(false)

                .show();

    }

    private void FetchData(){

        Handler handler = new Handler(Looper.getMainLooper());

        if (HasNetwork(this)) {

            Executors.newSingleThreadExecutor().execute(() -> {

                // Background thread work
                if (HasInternet()) {

                    try {
                        jsonData = GETReq();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // UI Thread work
                    handler.post(() -> {
                        try {
                            plexusDataList = PopulateDataList(jsonData);
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
