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

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED;
import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_STATUS_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.FILTER_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_STATUS_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.STATUS_RADIO_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.THEME_PREF;
import static tech.techlore.plexus.utils.IntentUtils.OpenURL;
import static tech.techlore.plexus.utils.IntentUtils.ReloadFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.ActivityMainBinding;
import tech.techlore.plexus.databinding.BottomSheetFooterBinding;
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding;
import tech.techlore.plexus.databinding.BottomSheetSortBinding;
import tech.techlore.plexus.databinding.BottomSheetThemeBinding;
import tech.techlore.plexus.fragments.main.InstalledAppsFragment;
import tech.techlore.plexus.fragments.main.PlexusDataFragment;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.preferences.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding activityBinding;
    public BottomSheetBehavior<CoordinatorLayout> bottomSheetBehavior;
    private int checkedItem = 0, clickedItem = 0; // To set nav view item background, check selected item
    private Fragment fragment;
    private String toolbarTitle;
    private PreferenceManager preferenceManager;
    public List<PlexusData> dataList;
    public List<InstalledApp> installedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        Intent intent = getIntent();
        preferenceManager = new PreferenceManager(this);
        bottomSheetBehavior = BottomSheetBehavior.from(activityBinding.bottomNavContainer);
        
        /*########################################################################################*/

        setSupportActionBar(activityBinding.toolbarBottom);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Get lists from previous activity
        dataList = intent.getParcelableArrayListExtra("plexusDataList");
        installedList = intent.getParcelableArrayListExtra("installedAppsList");

        // Default fragment
        fragment = new PlexusDataFragment();
        clickedItem = R.id.nav_plexus_data;
        checkedItem = clickedItem;
        toolbarTitle = getString(R.string.plexus_data);
        DisplayFragment(fragment, checkedItem);
    
        // Nav view items
        activityBinding.navView.setNavigationItemSelectedListener(navMenuItem -> {
        
            if (navMenuItem.getItemId() == R.id.nav_plexus_data) {
                fragment = new PlexusDataFragment();
                checkedItem = navMenuItem.getItemId();
                toolbarTitle = getString(R.string.plexus_data);
            }
            else if (navMenuItem.getItemId() == R.id.nav_installed_apps) {
                fragment = new InstalledAppsFragment();
                checkedItem = navMenuItem.getItemId();
                toolbarTitle = getString(R.string.installed_apps);
            
            }
            else if (navMenuItem.getItemId() == R.id.nav_fav) {
                checkedItem = navMenuItem.getItemId();
            }
    
            clickedItem = navMenuItem.getItemId();
            bottomSheetBehavior.setState(STATE_COLLAPSED); // Close nav view
            return true;
        });

        // Nav view bottom sheet
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // Perform all onClick actions from nav view
                // after bottom sheet is collapsed
                if (newState == STATE_COLLAPSED) {
                    
                    if (clickedItem == R.id.nav_plexus_data
                        || clickedItem == R.id.nav_installed_apps) {
                        DisplayFragment(fragment, checkedItem);
                    }
                    else if (clickedItem == R.id.nav_report_issue) {
                        OpenURL(MainActivity.this, "https://github.com/techlore/Plexus-app/issues",
                                activityBinding.mainCoordinatorLayout, activityBinding.bottomNavContainer);
                    }
                    else if (clickedItem == R.id.nav_theme) {
                        ThemeBottomSheet();
                    }
                    else if (clickedItem == R.id.nav_about) {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class)
                                              .putExtra("frag", clickedItem));
                    }
    
                    // Set to 0,
                    // otherwise if bottom sheet is dragged up and no item is clicked
                    // then on bottom sheet collapse, same action will be triggered again.
                    clickedItem = 0;
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
    
                activityBinding.dimBg.setAlpha(slideOffset * 2); // Dim background on sliding up
                activityBinding.navView.setCheckedItem(checkedItem); // Always sync checked item on slide
                
                // Hide toolbar title and menu on slide up
                if (slideOffset > 0.03) {
                    activityBinding.toolbarBottom.setTitle(null);
                    activityBinding.toolbarBottom.getMenu().clear();
                }
                else {
                    activityBinding.toolbarBottom.setTitle(toolbarTitle);
                    invalidateMenu();
                }

            }

        });

        // Nav view icon
        activityBinding.toolbarBottom.setNavigationOnClickListener(v -> {

            if (bottomSheetBehavior.getState() == STATE_COLLAPSED) {
                bottomSheetBehavior.setState(STATE_HALF_EXPANDED);
            }
            else if (bottomSheetBehavior.getState() == STATE_HALF_EXPANDED) {
                bottomSheetBehavior.setState(STATE_EXPANDED);
            }
            else if (bottomSheetBehavior.getState() == STATE_EXPANDED) {
                bottomSheetBehavior.setState(STATE_COLLAPSED);
            }

        });

    }

    // Setup fragments
    private void DisplayFragment(Fragment fragment, int checkedItem) {
        
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.activity_host_fragment, fragment)
                .commitNow();
        activityBinding.navView.setCheckedItem(checkedItem);
        activityBinding.toolbarBottom.setTitle(toolbarTitle);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        if (checkedItem == R.id.nav_installed_apps) {
            menu.findItem(R.id.menu_filter).setVisible(true);

            if (preferenceManager.getInt(FILTER_PREF) == 0
                || preferenceManager.getInt(FILTER_PREF) == R.id.menu_all_apps) {
                menu.findItem(R.id.menu_all_apps).setChecked(true);
            }
            else if (preferenceManager.getInt(FILTER_PREF) == R.id.menu_play_apps) {
                menu.findItem(R.id.menu_play_apps).setChecked(true);
            }
            else {
                menu.findItem(R.id.menu_non_play_apps).setChecked(true);
            }

        }
        else if (checkedItem == R.id.nav_about) {
            menu.findItem(R.id.menu_search).setVisible(false);
            menu.findItem(R.id.menu_sort).setVisible(false);
            menu.findItem(R.id.menu_filter).setVisible(false);
        }
        else {
            menu.findItem(R.id.menu_search).setVisible(true);
            menu.findItem(R.id.menu_sort).setVisible(true);
            menu.findItem(R.id.menu_filter).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Search
        // Don't finish main activity,
        // Or else issues when getting list back from search activity
        if (item.getItemId() == R.id.menu_search) {

            Intent searchIntent = new Intent(this, SearchActivity.class);
            searchIntent.putExtra("from", checkedItem);

            // If from Plexus Data, give Plexus Data list
            if (checkedItem == R.id.nav_plexus_data) {
                searchIntent.putParcelableArrayListExtra("plexusDataList", (ArrayList<PlexusData>) dataList);
            }

            // Else give Installed Apps list
            else {
                searchIntent.putParcelableArrayListExtra("installedAppsList", (ArrayList<InstalledApp>) installedList);
            }

            startActivity(searchIntent);
            overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement);
        }

        // Sort
        else if (item.getItemId() == R.id.menu_sort) {
            SortBottomSheet();
        }

        // Filter
        else if (item.getItemId() == R.id.menu_all_apps
                || item.getItemId() == R.id.menu_play_apps
                || item. getItemId() == R.id.menu_non_play_apps) {
            preferenceManager.setInt(FILTER_PREF, item.getItemId());
            ReloadFragment(getSupportFragmentManager(), fragment);
        }

        return true;
    }

    private void SortBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        final BottomSheetSortBinding bottomSheetBinding = BottomSheetSortBinding.inflate(getLayoutInflater());
        final BottomSheetHeaderBinding headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        final BottomSheetFooterBinding footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        headerBinding.bottomSheetTitle.setText(R.string.menu_sort);

        // Default alphabetical checked chip
        if (preferenceManager.getInt(A_Z_SORT_PREF) == 0) {
            preferenceManager.setInt(A_Z_SORT_PREF, R.id.sort_a_z);
        }
        bottomSheetBinding.alphabeticalChipGroup.check(preferenceManager.getInt(A_Z_SORT_PREF));

        // Status radio btn checked by default
        if (preferenceManager.getInt(STATUS_RADIO_PREF) == 0) {
            preferenceManager.setInt(STATUS_RADIO_PREF, R.id.radio_any_status);
        }
        bottomSheetBinding.statusRadiogroup.check(preferenceManager.getInt(STATUS_RADIO_PREF));

        // Status chip group visibility
        if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_dg_status) {

            bottomSheetBinding.statusChipGroup.setVisibility(View.VISIBLE);

            // Default DG status checked chip
            if (preferenceManager.getInt(DG_STATUS_SORT_PREF) == 0) {
                preferenceManager.setInt(DG_STATUS_SORT_PREF, R.id.sort_not_tested);
            }
            bottomSheetBinding.statusChipGroup.check(preferenceManager.getInt(DG_STATUS_SORT_PREF));
        }
        else if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_mg_status) {

            bottomSheetBinding.statusChipGroup.setVisibility(View.VISIBLE);

            // Default MG status checked chip
            if (preferenceManager.getInt(MG_STATUS_SORT_PREF) == 0) {
                preferenceManager.setInt(MG_STATUS_SORT_PREF, R.id.sort_not_tested);
            }
            bottomSheetBinding.statusChipGroup.check(preferenceManager.getInt(MG_STATUS_SORT_PREF));
        }
        else {
            bottomSheetBinding.statusChipGroup.setVisibility(View.GONE);
        }

        // On selecting status radio btn
        bottomSheetBinding.statusRadiogroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {

            if (checkedId != R.id.radio_any_status) {
                bottomSheetBinding.statusChipGroup.setVisibility(View.VISIBLE);
            }
            else {
                bottomSheetBinding.statusChipGroup.setVisibility(View.GONE);
            }

        });

        // Done
        footerBinding.positiveButton.setText(getString(R.string.done));
        footerBinding.positiveButton.setOnClickListener(view12 -> {

            preferenceManager.setInt(A_Z_SORT_PREF, bottomSheetBinding.alphabeticalChipGroup.getCheckedChipId());
            preferenceManager.setInt(STATUS_RADIO_PREF, bottomSheetBinding.statusRadiogroup.getCheckedRadioButtonId());

            if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_dg_status) {
                preferenceManager.setInt(DG_STATUS_SORT_PREF, bottomSheetBinding.statusChipGroup.getCheckedChipId());
            }
            else if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_mg_status) {
                preferenceManager.setInt(MG_STATUS_SORT_PREF, bottomSheetBinding.statusChipGroup.getCheckedChipId());
            }

            bottomSheetDialog.dismiss();
            ReloadFragment(getSupportFragmentManager(), fragment);

        });

        // Cancel
        footerBinding.negativeButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        bottomSheetDialog.show();
    }

    private void ThemeBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        final BottomSheetThemeBinding bottomSheetBinding = BottomSheetThemeBinding.inflate(getLayoutInflater());
        final BottomSheetHeaderBinding headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        final BottomSheetFooterBinding footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        headerBinding.bottomSheetTitle.setText(R.string.theme);

        // Default checked radio btn
        if (preferenceManager.getInt(THEME_PREF) == 0){
            if (Build.VERSION.SDK_INT >= 29){
                preferenceManager.setInt(THEME_PREF, R.id.sys_default);
            }
            else{
                preferenceManager.setInt(THEME_PREF, R.id.light);
            }
        }
        bottomSheetBinding.themeRadiogroup.check(preferenceManager.getInt(THEME_PREF));

        // Show system default option only on SDK 29 and above
        if (Build.VERSION.SDK_INT >= 29){
            bottomSheetBinding.sysDefault.setVisibility(View.VISIBLE);
        }
        else{
            bottomSheetBinding.sysDefault.setVisibility(View.GONE);
        }

        // On selecting option
        bottomSheetBinding.themeRadiogroup
                .setOnCheckedChangeListener((radioGroup, checkedId) -> {

                    if (checkedId == R.id.sys_default) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    else if (checkedId == R.id.light) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    else if (checkedId == R.id.dark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }

                    preferenceManager.setInt(THEME_PREF, checkedId);
                    bottomSheetDialog.dismiss();
                    this.recreate();
                });

        footerBinding.positiveButton.setVisibility(View.GONE);

        // Cancel
        footerBinding.negativeButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        bottomSheetDialog.show();
    }

    @Override
    public void onBackPressed() {

        if (bottomSheetBehavior.getState() != STATE_COLLAPSED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
        else if (checkedItem != R.id.nav_plexus_data) {
            fragment = new PlexusDataFragment();
            clickedItem = R.id.nav_plexus_data;
            checkedItem = clickedItem;
            toolbarTitle = getString(R.string.plexus_data);
            DisplayFragment(fragment, checkedItem);
        }
        else {
            finish();
        }
    }
}