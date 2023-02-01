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

import static tech.techlore.plexus.utils.IntentUtils.OpenURL;
import static tech.techlore.plexus.utils.IntentUtils.Share;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding;

public class AppDetailsActivity extends AppCompatActivity {

    private ActivityAppDetailsBinding activityBinding;
    private String nameString, packageNameString, plexusVersionString,
            dgStatusString, mgStatusString, dgNotesString, mgNotesString, playStoreString;
    private Drawable icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = ActivityAppDetailsBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        Intent intent = getIntent();
        nameString = intent.getStringExtra("name");
        packageNameString = intent.getStringExtra("packageName");
        //plexusVersionString = intent.getStringExtra("plexusVersion");
        final String installedVersionString = intent.getStringExtra("installedVersion");
        /*dgStatusString = intent.getStringExtra("dgStatus");
        mgStatusString = intent.getStringExtra("mgStatus");
        dgNotesString = intent.getStringExtra("dgNotes");
        mgNotesString = intent.getStringExtra("mgNotes");*/
        playStoreString = "https://play.google.com/store/apps/details?id=" + packageNameString;
        
        /*########################################################################################*/

        setSupportActionBar(activityBinding.bottomAppBar);
        activityBinding.bottomAppBar.setNavigationOnClickListener(v -> onBackPressed());
        
        RequestManager requestManager = Glide.with(getApplicationContext());
        RequestBuilder<Drawable> requestBuilder;
        if (installedVersionString != null) {
            
            activityBinding.plexusText.setVisibility(View.VISIBLE);
            activityBinding.installedVerLayout.setVisibility(View.VISIBLE);
            activityBinding.detailsInstalledVersion.setText(installedVersionString);
            try {
                requestBuilder = requestManager.load(getPackageManager().getApplicationIcon(packageNameString));
            }
            catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            requestBuilder = requestManager
                                .load("")
                                .placeholder(R.drawable.ic_apk)
                                .onlyRetrieveFromCache(true); // Image will always be in cache
                                                                  // since it's loaded in Plexus Data fragment
        }
        
        requestBuilder.into(activityBinding.detailsAppIcon);
    
        activityBinding.detailsName.setText(nameString);
        activityBinding.detailsPackageName.setText(packageNameString);
        //activityBinding.plexusVersionDetails.setText(plexusVersionString);
        
        /*activityBinding.dgNotes.setText(dgNotesString);
        activityBinding.mgNotes.setText(mgNotesString);

        BgColor(this, activityBinding.dgText, dgStatusString);
        BgColor(this, activityBinding.mgText, mgStatusString);

        BadgeColor(this, activityBinding.dgBadgeDetails, dgStatusString);
        BadgeColor(this, activityBinding.mgBadgeDetails, mgStatusString);*/
        
        // FAB
        activityBinding.fab.setOnClickListener(v -> {
            startActivity(new Intent(AppDetailsActivity.this, SubmitActivity.class)
                                  .putExtra("name", nameString)
                                  .putExtra("packageName", packageNameString));
            overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Play store URL
        if (item.getItemId() == R.id.menu_play_store) {

            OpenURL(this, playStoreString, activityBinding.appDetailsCoordinatorLayout, activityBinding.bottomAppBar);
        }

        // Share
        else if (item.getItemId() == R.id.menu_share) {
            Share(this,
                    nameString, packageNameString, /*plexusVersionString,
                    dgStatusString, mgStatusString,
                    dgNotesString, mgNotesString,*/
                    playStoreString);
        }
    
        if (item.getItemId() == R.id.menu_help) {
            startActivity(new Intent(AppDetailsActivity.this, SettingsActivity.class)
                                  .putExtra("frag", R.id.menu_help));
        }

        return true;
    }

    // Set transition when finishing activity
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_scale_out);
    }

}
