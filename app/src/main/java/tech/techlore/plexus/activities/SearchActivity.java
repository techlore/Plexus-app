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

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.ActivityMainBinding;
import tech.techlore.plexus.databinding.SearchViewBinding;
import tech.techlore.plexus.fragments.search.SearchDataFragment;
import tech.techlore.plexus.fragments.search.SearchInstalledFragment;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

public class SearchActivity extends AppCompatActivity {

    public List<PlexusData> dataList;
    public List<InstalledApp> installedList;
    public SearchViewBinding searchViewBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        Intent intent = getIntent();
        searchViewBinding = SearchViewBinding.bind(activityBinding.searchViewStub.inflate());

        /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(activityBinding.toolbarMain);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activityBinding.toolbarMain.setNavigationOnClickListener(view -> onBackPressed());

        // DEFAULT FRAGMENT
        if (savedInstanceState == null) {

            // IF FROM PLEXUS DATA TAB
            // DISPLAY SEARCH PLEXUS DATA FRAGMENT
            if (Objects.equals(intent.getExtras().getInt("from"), 0)){

                // GET PLEXUS DATA LIST FROM MAIN ACTIVITY
                //noinspection unchecked
                dataList =  (List<PlexusData>) intent.getSerializableExtra("plexusDataList");
                DisplayFragment("Search Data");

            }

            // ELSE DISPLAY SEARCH INSTALLED APPS FRAGMENT
            else {

                // GET INSTALLED APPS LIST FROM MAIN ACTIVITY
                //noinspection unchecked
                installedList = (List<InstalledApp>) intent.getSerializableExtra("installedAppsList");
                DisplayFragment("Search Installed");

            }

        }

    }

    // SETUP FRAGMENTS
    private void DisplayFragment(String fragmentName) {

        Fragment fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (fragmentName.equals("Search Data")) {
            searchViewBinding.searchView.setQueryHint(getResources().getString(R.string.menu_search)
                                    + " "
                                    + getResources().getString(R.string.plexus_data));
            fragment = new SearchDataFragment();
        }

        else {
            searchViewBinding.searchView.setQueryHint(getResources().getString(R.string.menu_search)
                                    + " "
                                    + getResources().getString(R.string.installed_apps));
            fragment = new SearchInstalledFragment();
        }

        transaction.replace(R.id.activity_host_fragment, fragment).commit();

    }

    // SET TRANSITION WHEN FINISHING ACTIVITY
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out_slide_to_top);

    }
}
