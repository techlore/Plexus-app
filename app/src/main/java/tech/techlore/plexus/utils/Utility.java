package tech.techlore.plexus.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.Serializable;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.AppDetailsActivity;

public class Utility {

    // SEND ARRAY LISTS WITH INTENT
    public static void SendListsIntent(Activity activityFrom, Class<?> activityTo,
                                      Serializable plexusDataList, Serializable installedAppsList) {

        activityFrom.startActivity(new Intent(activityFrom, activityTo)
                .putExtra("plexusDataList", plexusDataList)
                .putExtra("installedAppsList", installedAppsList));

    }

    // HORIZONTALLY SCROLL TEXT
    // IF TEXT IS TOO LONG
    public static void hScrollText(TextView textView) {

        // SET THESE 2 PARAMETERS FOR HORIZONTALLY SCROLLING TEXT
        textView.setSingleLine();
        textView.setSelected(true);
    }

    // SET BACKGROUND COLOR BASED ON SCORES
    public static void ScoreColor(Context context, View view, String score) {

        switch (score) {

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
