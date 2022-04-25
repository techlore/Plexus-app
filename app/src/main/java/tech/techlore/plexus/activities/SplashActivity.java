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
import static tech.techlore.plexus.utils.NetworkUtils.URLResponse;
import static tech.techlore.plexus.utils.ListUtils.PopulateDataList;
import static tech.techlore.plexus.utils.ListUtils.ScanInstalledApps;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.DialogFooterBinding;
import tech.techlore.plexus.databinding.DialogNoNetworkBinding;
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

    // NO NETWORK DIALOG
    private void NoNetworkDialog() {
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        dialog.setCancelable(false);

        dialog.getWindow().setBackgroundDrawable(ContextCompat
                .getDrawable(this, R.drawable.shape_rounded_corners));
        dialog.getWindow().getDecorView().setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bottomSheetColor));

        final DialogNoNetworkBinding dialogBinding = DialogNoNetworkBinding.inflate(getLayoutInflater());
        final DialogFooterBinding footerBinding = DialogFooterBinding.bind(dialogBinding.getRoot());
        dialog.setContentView(dialogBinding.getRoot());

        // POSITIVE BUTTON
        footerBinding.positiveButton
                .setOnClickListener(view1 -> {
                    FetchData();
                    dialog.dismiss();
                });

        // NEGATIVE BUTTON
        footerBinding.negativeButton.setText(getString(R.string.exit));
        footerBinding.negativeButton.setOnClickListener(view12 -> {
                    dialog.cancel();
                    finishAndRemoveTask();
                });

        // SHOW DIALOG WITH CUSTOM ANIMATION
        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
    }

    private void FetchData(){

        Handler handler = new Handler(Looper.getMainLooper());

        if (HasNetwork(this)) {

            Executors.newSingleThreadExecutor().execute(() -> {

                // BACKGROUND THREAD WORK
                if (HasInternet()) {

                    try {
                        jsonData = URLResponse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // UI THREAD WORK
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
