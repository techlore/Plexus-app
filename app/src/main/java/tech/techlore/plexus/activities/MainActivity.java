package tech.techlore.plexus.activities;

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_RADIO_PREF;
import static tech.techlore.plexus.utils.Utility.InflateViewStub;
import static tech.techlore.plexus.utils.Utility.SendListsIntent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.main.InstalledAppsFragment;
import tech.techlore.plexus.fragments.main.PlexusDataFragment;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.preferences.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private Fragment fragment;
    private TabLayout tabLayout;
    public List<PlexusData> dataList;
    public List <InstalledApp> installedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        preferenceManager = new PreferenceManager(this);
        InflateViewStub(findViewById(R.id.tab_layout_view_stub));
        tabLayout = findViewById(R.id.tab_layout);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(findViewById(R.id.toolbar_main));

        tabLayout.setVisibility(View.VISIBLE);

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
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

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
            StartSearch(tabLayout.getSelectedTabPosition());
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

        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.bottom_sheet_sort, null);
        bottomSheetDialog.setContentView(view);

        // TITLE
        ((TextView) view.findViewById(R.id.bottom_sheet_title)).setText(R.string.menu_sort);

        final ChipGroup alphabeticalChipGroup = view.findViewById(R.id.alphabetical_chip_group);
        final RadioGroup ratingRadioGroup = view.findViewById(R.id.rating_radiogroup);
        final ChipGroup ratingChipGroup = view.findViewById(R.id.rating_chip_group);

        // DEFAULT ALPHABETICAL CHECKED CHIP
        if (preferenceManager.getInt(A_Z_SORT_PREF) == 0) {
            preferenceManager.setInt(A_Z_SORT_PREF, R.id.sort_a_z);
        }
        alphabeticalChipGroup.check(preferenceManager.getInt(A_Z_SORT_PREF));

        // RATING RADIO CHECKED BY DEFAULT
        if (preferenceManager.getInt(RATING_RADIO_PREF) == 0) {
            preferenceManager.setInt(RATING_RADIO_PREF, R.id.radio_any_rating);
        }
        ratingRadioGroup.check(preferenceManager.getInt(RATING_RADIO_PREF));

        // RATING CHIP GROUP VISIBILITY
        if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_dg_rating) {

            ratingChipGroup.setVisibility(View.VISIBLE);

            // DG RATING CHIP CHECKED BY DEFAULT
            if (preferenceManager.getInt(DG_RATING_SORT_PREF) == 0) {
                preferenceManager.setInt(DG_RATING_SORT_PREF, R.id.sort_not_tested);
            }
            ratingChipGroup.check(preferenceManager.getInt(DG_RATING_SORT_PREF));
        }

        else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_mg_rating) {

            ratingChipGroup.setVisibility(View.VISIBLE);

            // MG RATING CHIP CHECKED BY DEFAULT
            if (preferenceManager.getInt(MG_RATING_SORT_PREF) == 0) {
                preferenceManager.setInt(MG_RATING_SORT_PREF, R.id.sort_not_tested);
            }
            ratingChipGroup.check(preferenceManager.getInt(MG_RATING_SORT_PREF));
        }

        else {
            ratingChipGroup.setVisibility(View.GONE);
        }

        // ON SELECTING ALPHABETICAL CHIP
        alphabeticalChipGroup.setOnCheckedChangeListener((chipGroup, checkedId) ->
                preferenceManager.setInt(A_Z_SORT_PREF, checkedId)
        );

        // ON SELECTING RATING RADIO
        ratingRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {

            if (checkedId != R.id.radio_any_rating) {
                ratingChipGroup.setVisibility(View.VISIBLE);
                ratingChipGroup.check(R.id.sort_not_tested);
            }
            else {
                ratingChipGroup.setVisibility(View.GONE);
            }
            preferenceManager.setInt(RATING_RADIO_PREF, checkedId);

        });

        // ON SELECTING RATING CHIP
        ratingChipGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_dg_rating) {
                preferenceManager.setInt(DG_RATING_SORT_PREF, checkedId);
            }
            else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_mg_rating) {
                preferenceManager.setInt(MG_RATING_SORT_PREF, checkedId);
            }
        });

        // DONE BUTTON
        view.findViewById(R.id.done_button).setOnClickListener(view12 -> {
            bottomSheetDialog.dismiss();
            getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            getSupportFragmentManager().beginTransaction().attach(fragment).commit();
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