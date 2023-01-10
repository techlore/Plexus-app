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
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.AppDetailsActivity;

public class IntentUtils {

    // Send array list with intent
    public static void SendListsIntent(Activity activityFrom, Class<?> activityTo,
                                       Serializable plexusDataList, Serializable installedAppsList) {

        activityFrom.startActivity(new Intent(activityFrom, activityTo)
                                    .putExtra("plexusDataList", plexusDataList)
                                    .putExtra("installedAppsList", installedAppsList));

    }

    // App details activity
    public static void AppDetails(Activity activityFrom, String name, String packageName) {
//                                  String plexusVersion, String installedVersion,
//                                  String dgNotes, String mgNotes,
//                                  String dgStatus, String mgStatus

        activityFrom.startActivity(new Intent(activityFrom, AppDetailsActivity.class)
                                    .putExtra("name", name)
                                    .putExtra("packageName", packageName));
//                                    .putExtra("plexusVersion", plexusVersion)
//                                    .putExtra("installedVersion", installedVersion)
//                                    .putExtra("dgStatus", dgStatus)
//                                    .putExtra("mgStatus", mgStatus)
//                                    .putExtra("dgNotes", dgNotes)
//                                    .putExtra("mgNotes", mgNotes)

        activityFrom.overridePendingTransition(R.anim.fade_scale_in, R.anim.no_movement);

    }

    // Refresh fragment
    public static void ReloadFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction().detach(fragment).commitNow();
        fragmentManager.beginTransaction().attach(fragment).commitNow();
    }

    // Open links
    public static void OpenURL(Activity activity, String URL, CoordinatorLayout coordinatorLayout, View anchorView) {

        try
        {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
        }
        // If no browser installed, show snackbar
        catch (ActivityNotFoundException e)
        {
            Snackbar.make(coordinatorLayout, R.string.no_browsers, BaseTransientBottomBar.LENGTH_SHORT)
                    .setAnchorView(anchorView) // Above FAB, bottom bar etc.
                    .show();
        }

    }

    // Share
    public static void Share(Activity activity,
                             String nameString, String packageNameString, /*String plexusVersionString,
                             String dgStatusString, String mgStatusString,
                             String dgNotesString, String mgNotesString,*/
                             String playStoreString) {

        activity.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                                    .setType("text/plain")
                                                    .putExtra(Intent.EXTRA_TEXT,
                                                              activity.getString(R.string.app_name) + ": "
                                                              + nameString
                                                              + "\n"
                                                              + activity.getString(R.string.package_name) + ": "
                                                              + packageNameString
                                                              + "\n"
                                                                /*+ activity.getString(R.string.version) + ": "
                                                                + plexusVersionString
                                                                + "\n"
                                                                + activity.getString(R.string.dg_status) + ": "
                                                                + dgStatusString
                                                                + "\n"
                                                                + activity.getString(R.string.mg_status) + ": "
                                                                + mgStatusString
                                                                + "\n"
                                                                + activity.getString(R.string.de_Googled) + " "
                                                                + activity.getString(R.string.notes) + ": "
                                                                + dgNotesString
                                                                + "\n"
                                                                + activity.getString(R.string.microG) + " "
                                                                + activity.getString(R.string.notes) + ": "
                                                                + mgNotesString
                                                                + "\n"*/
                                                              + activity.getString(R.string.play_store) + ": "
                                                              + playStoreString),
                                                    activity.getString(R.string.share)));

    }

}
