package tech.techlore.plexus.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.Serializable;
import java.util.List;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.AppDetailsActivity;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

public class Utility {

    // SEND ARRAY LISTS WITH INTENT
    public static void SendListsIntent(Activity activityFrom, Class<?> activityTo,
                                      Serializable plexusDataList, Serializable installedAppsList) {

        activityFrom.startActivity(new Intent(activityFrom, activityTo)
                .putExtra("plexusDataList", plexusDataList)
                .putExtra("installedAppsList", installedAppsList));

    }

    // EMPTY LIST VIEW STUB
    public static void EmptyList(ViewStub viewStub) {
            viewStub.inflate();
            viewStub.setVisibility(View.VISIBLE);

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

    // HORIZONTALLY SCROLL TEXT
    // IF TEXT IS TOO LONG
    public static void hScrollText(TextView textView) {

        // SET THESE 2 PARAMETERS FOR HORIZONTALLY SCROLLING TEXT
        textView.setSingleLine();
        textView.setSelected(true);
    }

    // SET BACKGROUND COLOR BASED ON RATING
    public static void RatingColor(Context context, View view, String rating) {

        switch (rating) {

            case "X":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.ratingXColor));
                break;

            case "1":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating1Color));
                break;

            case "2":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating2Color));
                break;

            case "3":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating3Color));
                break;

            case "4":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating4Color));
                break;

        }

    }

    // APP DETAILS ACTIVITY
    public static void AppDetails(Activity activityFrom, String name, String packageName, String version,
                           String dgNotes, String mgNotes, String dgRating, String mgRating) {

        activityFrom.startActivity(new Intent(activityFrom, AppDetailsActivity.class)
                                    .putExtra("name", name)
                                    .putExtra("packageName", packageName)
                                    .putExtra("version", version)
                                    .putExtra("dgRating", dgRating)
                                    .putExtra("mgRating", mgRating)
                                    .putExtra("dgNotes", dgNotes)
                                    .putExtra("mgNotes", mgNotes));

        activityFrom.overridePendingTransition(R.anim.fade_scale_in, R.anim.no_movement);

    }

}
