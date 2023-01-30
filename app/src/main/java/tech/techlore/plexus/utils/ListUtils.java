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

package tech.techlore.plexus.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import tech.techlore.plexus.R;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.models.Root;

public class ListUtils {

    // Populate Plexus data list
    public static List<PlexusData> PopulateDataList(String jsonData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Root root = objectMapper.readValue(jsonData, Root.class);
        return root.data;
    }

    // Scan all installed apps and populate respective list
    public static void ScanInstalledApps(Context context, List<PlexusData> plexusDataList, List<InstalledApp> installedAppsList) {

        PackageManager packageManager = context.getPackageManager();

        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {

            InstalledApp installedApp = new InstalledApp();
            String plexusVersion = "NA", dgStatus = "X", mgStatus = "X", dgNotes = "X", mgNotes = "X";

            // No system apps
            // Only scan for user installed apps
            // OR system apps updated by user
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1
                 || (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                
                installedApp.setName(String.valueOf(appInfo.loadLabel(packageManager)));
                installedApp.setPackageName(appInfo.packageName);

                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                    installedApp.setInstalledVersion(packageInfo.versionName);
                }
                catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                // Search for package name in Plexus data
                // To set Plexus version, status & notes
                for (PlexusData plexusData : plexusDataList) {

                    if (plexusData.packageName.contains(appInfo.packageName)) {
//                        plexusVersion = plexusData.version;
//                        dgStatus = plexusData.dgStatus;
//                        mgStatus = plexusData.mgStatus;
//                        dgNotes = plexusData.dgNotes;
//                        mgNotes = plexusData.mgNotes;
                    }

                }

                installedApp.setPlexusVersion(plexusVersion);
                installedApp.setDgRating(dgStatus);
                installedApp.setMgRating(mgStatus);
                installedApp.setDgNotes(dgNotes);
                installedApp.setMgNotes(mgNotes);
                installedAppsList.add(installedApp);
            }
        }

    }

    // Plexus data status sort
    public static void PlexusDataStatusSort(int preferenceKey, PlexusData plexusData,
                                            String status, List<PlexusData> plexusDataList) {

        if (preferenceKey == 0
                || preferenceKey == R.id.sort_not_tested) {

            if (status.equals("X")) {
                plexusDataList.add(plexusData);
            }

        }
        else if (preferenceKey == R.id.sort_unusable && status.equals("1")) {

            plexusDataList.add(plexusData);
        }
        else if (preferenceKey == R.id.sort_acceptable && status.equals("2")) {

            plexusDataList.add(plexusData);
        }
        else if (preferenceKey == R.id.sort_good && status.equals("3")) {

            plexusDataList.add(plexusData);
        }
        else if (preferenceKey == R.id.sort_perfect && status.equals("4")) {

            plexusDataList.add(plexusData);
        }

    }

    // Installed apps status sort
    public static void InstalledAppsStatusSort(int preferenceKey, InstalledApp installedApp,
                                               String status, List<InstalledApp> installedAppsList) {

        if (preferenceKey == 0
                || preferenceKey == R.id.sort_not_tested) {

            if (status.equals("X")) {
                installedAppsList.add(installedApp);
            }

        }
        else if (preferenceKey == R.id.sort_unusable && status.equals("1")) {

            installedAppsList.add(installedApp);
        }
        else if (preferenceKey == R.id.sort_acceptable && status.equals("2")) {

            installedAppsList.add(installedApp);
        }
        else if (preferenceKey == R.id.sort_good && status.equals("3")) {

            installedAppsList.add(installedApp);
        }
        else if (preferenceKey == R.id.sort_perfect && status.equals("4")) {

            installedAppsList.add(installedApp);
        }

    }

}
