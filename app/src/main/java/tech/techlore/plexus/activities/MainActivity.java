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

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_RADIO_PREF;
import static tech.techlore.plexus.utils.IntentUtils.SendListsIntent;
import static tech.techlore.plexus.utils.UiUtils.ReloadViewPagerFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.adapters.ViewPagerAdapter;
import tech.techlore.plexus.databinding.ActivityMainBinding;
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding;
import tech.techlore.plexus.databinding.BottomSheetSortBinding;
import tech.techlore.plexus.databinding.DialogFooterBinding;
import tech.techlore.plexus.databinding.TabLayoutBinding;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.preferences.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding activityBinding;
    public ViewPagerAdapter viewPagerAdapter;
    private PreferenceManager preferenceManager;
    private TabLayoutBinding tabLayoutBinding;
    public List<PlexusData> dataList;
    public List<InstalledApp> installedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        Intent intent = getIntent();
        preferenceManager = new PreferenceManager(this);
        tabLayoutBinding = TabLayoutBinding.bind(activityBinding.tabLayoutViewStub.inflate());
        viewPagerAdapter = new ViewPagerAdapter(this);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(activityBinding.toolbarMain);

        // GET LISTS FROM PREVIOUS ACTIVITY
        //noinspection unchecked
        dataList = (List<PlexusData>) intent.getSerializableExtra("plexusDataList");
        //noinspection unchecked
        installedList = (List<InstalledApp>) intent.getSerializableExtra("installedAppsList");

        activityBinding.viewPager.setAdapter(viewPagerAdapter);

        // SLIDING TAB LAYOUT WITH VIEWPAGER2
        new TabLayoutMediator(tabLayoutBinding.tabLayout, activityBinding.viewPager,
                              true, (tab, position) -> {

            if (position == 0){
                tab.setText(R.string.plexus_data);
            }
            else {
                tab.setText(R.string.installed_apps);
            }

        }).attach();

    }

    // SEARCH ACTIVITY INTENT
    public void StartSearch(int selectedTab) {

        Intent searchIntent = new Intent(this, SearchActivity.class);
        searchIntent.putExtra("from", selectedTab);

        // IF FROM PLEXUS DATA TAB,
        // GIVE PLEXUS DATA LIST TO SEARCH ACTIVITY
        if (selectedTab == 0) {
            searchIntent.putExtra("plexusDataList", (Serializable) dataList);
        }

        // ELSE GIVE INSTALLED APPS LIST TO SEARCH ACTIVITY
        else {
            searchIntent.putExtra("installedAppsList", (Serializable) installedList);
        }

        startActivity(searchIntent);
        overridePendingTransition(R.anim.fade_in_slide_from_top, R.anim.no_movement);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // SEARCH
        // DON'T FINISH MAIN ACTIVITY,
        // OR ELSE ISSUES WHEN GETTING LIST BACK FROM SEARCH ACTIVITY
        if (item.getItemId() == R.id.menu_search) {
            StartSearch(tabLayoutBinding.tabLayout.getSelectedTabPosition());
        }

        // SORT
        else if (item.getItemId() == R.id.menu_sort) {
            SortBottomSheet();
        }

        // HELP
        else if (item.getItemId() == R.id.menu_help) {
            startActivity(new Intent(this, HelpActivity.class));
            overridePendingTransition(R.anim.fade_in_slide_from_end, R.anim.no_movement);
        }

        // SETTINGS
        else if (item.getItemId() == R.id.menu_settings) {

            // GIVE BOTH LISTS TO SETTINGS ACTIVITY TO HOLD
            SendListsIntent(this, SettingsActivity.class,
                    (Serializable) dataList, (Serializable) installedList);
            finish();
            overridePendingTransition(R.anim.fade_in_slide_from_end, R.anim.no_movement);
        }

        return true;
    }

    // SORT BOTTOM SHEET
    private void SortBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(false);

        final BottomSheetSortBinding bottomSheetBinding = BottomSheetSortBinding.inflate(getLayoutInflater());
        final BottomSheetHeaderBinding headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        final DialogFooterBinding footerBinding = DialogFooterBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        // TITLE
        headerBinding.bottomSheetTitle.setText(R.string.menu_sort);

        // DEFAULT ALPHABETICAL CHECKED CHIP
        if (preferenceManager.getInt(A_Z_SORT_PREF) == 0) {
            preferenceManager.setInt(A_Z_SORT_PREF, R.id.sort_a_z);
        }
        bottomSheetBinding.alphabeticalChipGroup.check(preferenceManager.getInt(A_Z_SORT_PREF));

        // RATING RADIO CHECKED BY DEFAULT
        if (preferenceManager.getInt(RATING_RADIO_PREF) == 0) {
            preferenceManager.setInt(RATING_RADIO_PREF, R.id.radio_any_rating);
        }
        bottomSheetBinding.ratingRadiogroup.check(preferenceManager.getInt(RATING_RADIO_PREF));

        // RATING CHIP GROUP VISIBILITY
        if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_dg_rating) {

            bottomSheetBinding.ratingChipGroup.setVisibility(View.VISIBLE);

            // DG RATING CHIP CHECKED BY DEFAULT
            if (preferenceManager.getInt(DG_RATING_SORT_PREF) == 0) {
                preferenceManager.setInt(DG_RATING_SORT_PREF, R.id.sort_not_tested);
            }
            bottomSheetBinding.ratingChipGroup.check(preferenceManager.getInt(DG_RATING_SORT_PREF));
        }

        else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_mg_rating) {

            bottomSheetBinding.ratingChipGroup.setVisibility(View.VISIBLE);

            // MG RATING CHIP CHECKED BY DEFAULT
            if (preferenceManager.getInt(MG_RATING_SORT_PREF) == 0) {
                preferenceManager.setInt(MG_RATING_SORT_PREF, R.id.sort_not_tested);
            }
            bottomSheetBinding.ratingChipGroup.check(preferenceManager.getInt(MG_RATING_SORT_PREF));
        }

        else {
            bottomSheetBinding.ratingChipGroup.setVisibility(View.GONE);
        }

        // ON SELECTING ALPHABETICAL CHIP
        bottomSheetBinding.alphabeticalChipGroup.setOnCheckedChangeListener((chipGroup, checkedId) ->
                preferenceManager.setInt(A_Z_SORT_PREF, checkedId)
        );

        // ON SELECTING RATING RADIO
        bottomSheetBinding.ratingRadiogroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {

            if (checkedId != R.id.radio_any_rating) {
                bottomSheetBinding.ratingChipGroup.setVisibility(View.VISIBLE);
                bottomSheetBinding.ratingChipGroup.check(R.id.sort_not_tested);
            }
            else {
                bottomSheetBinding.ratingChipGroup.setVisibility(View.GONE);
            }
            preferenceManager.setInt(RATING_RADIO_PREF, checkedId);

        });

        // ON SELECTING RATING CHIP
        bottomSheetBinding.ratingChipGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_dg_rating) {
                preferenceManager.setInt(DG_RATING_SORT_PREF, checkedId);
            }
            else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_mg_rating) {
                preferenceManager.setInt(MG_RATING_SORT_PREF, checkedId);
            }
        });

        // POSITIVE BUTTON
        footerBinding.positiveButton.setText(getString(R.string.done));
        footerBinding.positiveButton.setOnClickListener(view12 -> {
                    bottomSheetDialog.dismiss();
                    ReloadViewPagerFragment(activityBinding.viewPager, viewPagerAdapter, tabLayoutBinding.tabLayout.getSelectedTabPosition());
        });

        // NEGATIVE BUTTON
        footerBinding.negativeButton.setVisibility(View.GONE);

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // ON BACK PRESSED
    @Override
    public void onBackPressed() {
        finish();
    }
}