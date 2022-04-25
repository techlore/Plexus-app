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

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.ActivityMainBinding;
import tech.techlore.plexus.fragments.settings.SettingsDefaultFragment;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

public class SettingsActivity extends AppCompatActivity {

    private List<PlexusData> storeDataList;
    private List <InstalledApp> storeInstalledList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        Intent intent = getIntent();

    /*############################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(activityBinding.toolbarMain);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activityBinding.toolbarMain.setNavigationOnClickListener(v ->
                onBackPressed());

        // GET LISTS FROM MAIN ACTIVITY
        //noinspection unchecked
        storeDataList = (List<PlexusData>) intent.getSerializableExtra("plexusDataList");
        //noinspection unchecked
        storeInstalledList = (List<InstalledApp>) intent.getSerializableExtra("installedAppsList");

        if (savedInstanceState == null){
            // DEFAULT FRAGMENT
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_host_fragment, new SettingsDefaultFragment())
                    .commitNow();
        }
    }

    // ON BACK PRESSED
    @Override
    public void onBackPressed() {

        // IF NOT ON DEFAULT FRAGMENT, GO TO DEFAULT FRAGMENT
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        }

        // IF ON DEFAULT FRAGMENT,
        // GIVE LISTS BACK TO MAIN ACTIVITY
        // AND FINISH THIS ACTIVITY
        else {
            SendListsIntent(this, MainActivity.class,
                    (Serializable) storeDataList, (Serializable) storeInstalledList);
            finish();
            overridePendingTransition(0, R.anim.fade_out_slide_to_end);
        }
    }

}
