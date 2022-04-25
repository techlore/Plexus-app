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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.Serializable;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.AppDetailsActivity;

public class IntentUtils {

    // SEND ARRAY LISTS WITH INTENT
    public static void SendListsIntent(Activity activityFrom, Class<?> activityTo,
                                       Serializable plexusDataList, Serializable installedAppsList) {

        activityFrom.startActivity(new Intent(activityFrom, activityTo)
                                    .putExtra("plexusDataList", plexusDataList)
                                    .putExtra("installedAppsList", installedAppsList));

    }

    // APP DETAILS ACTIVITY
    public static void AppDetails(Activity activityFrom, String name, String packageName,
                                  String plexusVersion, String installedVersion,
                                  String dgNotes, String mgNotes,
                                  String dgRating, String mgRating) {

        activityFrom.startActivity(new Intent(activityFrom, AppDetailsActivity.class)
                                    .putExtra("name", name)
                                    .putExtra("packageName", packageName)
                                    .putExtra("plexusVersion", plexusVersion)
                                    .putExtra("installedVersion", installedVersion)
                                    .putExtra("dgRating", dgRating)
                                    .putExtra("mgRating", mgRating)
                                    .putExtra("dgNotes", dgNotes)
                                    .putExtra("mgNotes", mgNotes));

        activityFrom.overridePendingTransition(R.anim.fade_scale_in, R.anim.no_movement);

    }

    // OPEN LINKS
    public static void OpenURL(Activity activity, String URL) {

        try
        {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
        }
        // IF BROWSERS NOT INSTALLED, SHOW TOAST
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(activity, activity.getResources().getString(R.string.no_browsers), Toast.LENGTH_SHORT).show();
        }

    }

    // SHARE
    public static void Share(Activity activity,
                             String nameString, String packageNameString, String plexusVersionString,
                             String dgRatingString, String mgRatingString,
                             String dgNotesString, String mgNotesString,
                             String playStoreString) {

        activity.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                                    .setType("text/plain")
                                                    .putExtra(Intent.EXTRA_TEXT,
                                                                activity.getString(R.string.application) + ": "
                                                                + nameString
                                                                + "\n"
                                                                + activity.getString(R.string.package_name) + ": "
                                                                + packageNameString
                                                                + "\n"
                                                                + activity.getString(R.string.version) + ": "
                                                                + plexusVersionString
                                                                + "\n"
                                                                + activity.getString(R.string.dg_rating) + ": "
                                                                + dgRatingString
                                                                + "\n"
                                                                + activity.getString(R.string.mg_rating) + ": "
                                                                + mgRatingString
                                                                + "\n"
                                                                + activity.getString(R.string.de_Googled) + " "
                                                                + activity.getString(R.string.notes) + ": "
                                                                + dgNotesString
                                                                + "\n"
                                                                + activity.getString(R.string.microG) + " "
                                                                + activity.getString(R.string.notes) + ": "
                                                                + mgNotesString
                                                                + "\n"
                                                                + activity.getString(R.string.play_store) + ": "
                                                                + playStoreString),
                                                    activity.getString(R.string.share)));

    }

}
