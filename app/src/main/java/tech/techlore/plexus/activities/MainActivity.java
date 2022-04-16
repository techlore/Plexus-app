package tech.techlore.plexus.activities;

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_RADIO_PREF;
import static tech.techlore.plexus.utils.IntentUtils.SendListsIntent;
import static tech.techlore.plexus.utils.UiUtils.ReloadFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.ActivityMainBinding;
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding;
import tech.techlore.plexus.databinding.BottomSheetSortBinding;
import tech.techlore.plexus.databinding.TabLayoutBinding;
import tech.techlore.plexus.fragments.main.InstalledAppsFragment;
import tech.techlore.plexus.fragments.main.PlexusDataFragment;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.preferences.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    public Fragment fragment;
    private TabLayoutBinding tabLayoutBinding;
    public List<PlexusData> dataList;
    public List <InstalledApp> installedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        Intent intent = getIntent();
        preferenceManager = new PreferenceManager(this);
        tabLayoutBinding = TabLayoutBinding.bind(activityBinding.tabLayoutViewStub.inflate());

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(activityBinding.toolbarMain);

        // GET LISTS FROM PREVIOUS ACTIVITY
        //noinspection unchecked
        dataList = (List<PlexusData>) intent.getSerializableExtra("plexusDataList");
        //noinspection unchecked
        installedList = (List<InstalledApp>) intent.getSerializableExtra("installedAppsList");

        // DEFAULT FRAGMENT
        if (savedInstanceState == null) {
            DisplayFragment(0);
        }

        // TAB LAYOUT
        tabLayoutBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                DisplayFragment(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

    }

    // SETUP FRAGMENTS
    private void DisplayFragment(int selectedTab) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (selectedTab == 0) {
            fragment = new PlexusDataFragment();
            transaction.setCustomAnimations(R.anim.slide_from_start, R.anim.slide_to_end,
                    R.anim.slide_from_end, R.anim.slide_to_start);
        }

        else {
            fragment = new InstalledAppsFragment();
            transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                    R.anim.slide_from_start, R.anim.slide_to_end);
        }

        transaction.replace(R.id.activity_host_fragment, fragment)
                .commitNow();

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

    // MENU
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        // SEARCH
        // DON'T FINISH MAIN ACTIVITY,
        // OR ELSE ISSUES WHEN GETTING LIST BACK FROM SEARCH ACTIVITY
        menu.findItem(R.id.menu_search).setOnMenuItemClickListener(item -> {
            StartSearch(tabLayoutBinding.tabLayout.getSelectedTabPosition());
            return true;
        });

        // SORT
        menu.findItem(R.id.menu_sort).setOnMenuItemClickListener(item -> {
            SortBottomSheet();
            return true;
        });

        // HELP
        menu.findItem(R.id.menu_help).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, HelpActivity.class));
            overridePendingTransition(R.anim.fade_in_slide_from_end, R.anim.no_movement);
            return true;
        });

        // SETTINGS
        menu.findItem(R.id.menu_settings).setOnMenuItemClickListener(menuItem -> {

            // GIVE BOTH LISTS TO SETTINGS ACTIVITY TO HOLD
            SendListsIntent(this, SettingsActivity.class,
                    (Serializable) dataList, (Serializable) installedList);
            finish();
            overridePendingTransition(R.anim.fade_in_slide_from_end, R.anim.no_movement);
            return true;
        });

        return true;
    }

    // SORT BOTTOM SHEET
    private void SortBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(false);

        final BottomSheetSortBinding bottomSheetBinding = BottomSheetSortBinding.inflate(getLayoutInflater());
        final BottomSheetHeaderBinding headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
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

        // DONE BUTTON
        bottomSheetBinding.doneButton.setOnClickListener(view12 -> {
            bottomSheetDialog.dismiss();
            ReloadFragment(getSupportFragmentManager(), fragment);
        });

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