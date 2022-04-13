package tech.techlore.plexus.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import tech.techlore.plexus.R;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

public class ListUtils {

    // POPULATE PLEXUS DATA LIST
    public static List<PlexusData> PopulateDataList(String jsonData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonData, new TypeReference<List<PlexusData>>() {});
    }

    // SCAN ALL INSTALLED APPS AND POPULATE RESPECTIVE LIST
    public static void ScanInstalledApps(Context context, List<PlexusData> plexusDataList, List<InstalledApp> installedAppsList) {

        PackageManager packageManager = context.getPackageManager();

        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {

            InstalledApp installedApp = new InstalledApp();
            String plexusVersion = "NA", dgRating = "X", mgRating = "X", dgNotes = "X", mgNotes = "X";

            // NO SYSTEM APPS
            // ONLY SCAN FOR USER INSTALLED APPS
            // OR SYSTEM APPS THAT WERE UPDATED BY USER
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1
                    || (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) !=0) {

                installedApp.setName(String.valueOf(appInfo.loadLabel(packageManager)));
                installedApp.setPackageName(appInfo.packageName);

                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                    installedApp.setInstalledVersion(packageInfo.versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                // SEARCH FOR THE PACKAGE NAME IN PLEXUS DATA
                // TO SET PLEXUS VERSION, RATINGS AND NOTES
                for (PlexusData plexusData : plexusDataList) {

                    if (plexusData.packageName.contains(appInfo.packageName)) {
                        plexusVersion = plexusData.version;
                        dgRating = plexusData.dgRating;
                        mgRating = plexusData.mgRating;
                        dgNotes = plexusData.dgNotes;
                        mgNotes = plexusData.mgNotes;
                    }

                }

                installedApp.setPlexusVersion(plexusVersion);
                installedApp.setDgRating(dgRating);
                installedApp.setMgRating(mgRating);
                installedApp.setDgNotes(dgNotes);
                installedApp.setMgNotes(mgNotes);
                installedAppsList.add(installedApp);
            }
        }

    }

    // PLEXUS DATA RATING SORT
    public static void PlexusDataRatingSort(int preferenceKey, PlexusData plexusData,
                                            String rating, List<PlexusData> plexusDataList) {

        if (preferenceKey == 0
                || preferenceKey == R.id.sort_not_tested) {

            if (rating.equals("X")) {
                plexusDataList.add(plexusData);
            }

        }
        else if (preferenceKey == R.id.sort_unusable && rating.equals("1")) {

            plexusDataList.add(plexusData);
        }
        else if (preferenceKey == R.id.sort_acceptable && rating.equals("2")) {

            plexusDataList.add(plexusData);
        }
        else if (preferenceKey == R.id.sort_good && rating.equals("3")) {

            plexusDataList.add(plexusData);
        }
        else if (preferenceKey == R.id.sort_perfect && rating.equals("4")) {

            plexusDataList.add(plexusData);
        }

    }

    // INSTALLED APPS RATING SORT
    public static void InstalledAppsRatingSort(int preferenceKey, InstalledApp installedApp,
                                               String rating, List<InstalledApp> installedAppsList) {

        if (preferenceKey == 0
                || preferenceKey == R.id.sort_not_tested) {

            if (rating.equals("X")) {
                installedAppsList.add(installedApp);
            }

        }
        else if (preferenceKey == R.id.sort_unusable && rating.equals("1")) {

            installedAppsList.add(installedApp);
        }
        else if (preferenceKey == R.id.sort_acceptable && rating.equals("2")) {

            installedAppsList.add(installedApp);
        }
        else if (preferenceKey == R.id.sort_good && rating.equals("3")) {

            installedAppsList.add(installedApp);
        }
        else if (preferenceKey == R.id.sort_perfect && rating.equals("4")) {

            installedAppsList.add(installedApp);
        }

    }

}
