package tech.techlore.plexus.activities;

import static tech.techlore.plexus.preferences.PreferenceManager.SORT_PREF;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
    public List<PlexusData> dataList;
    public List <InstalledApp> installedList;
    public ExtendedFloatingActionButton searchFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        preferenceManager = new PreferenceManager(this);
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        searchFab = findViewById(R.id.search_fab);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(findViewById(R.id.toolbar_main));

        tabLayout.setVisibility(View.VISIBLE);
        searchFab.setVisibility(View.VISIBLE);

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

        // SEARCH FAB
        // DON'T FINISH MAIN ACTIVITY,
        // OR ELSE ISSUES WHEN GETTING LIST BACK FROM SEARCH ACTIVITY
        searchFab.setOnClickListener(v ->
                StartSearch(tabLayout.getSelectedTabPosition()));

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
        overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement);

    }

    // MENU
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        // RATING INFO
        menu.findItem(R.id.menu_rating_info).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(this, RatingInfoActivity.class));
            overridePendingTransition(R.anim.fade_in_slide_from_end, R.anim.no_movement);
            return true;
        });

        // SORT
        menu.findItem(R.id.menu_sort).setOnMenuItemClickListener(item -> {
            SortBottomSheet();
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
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.bottom_sheet_options, null);
        bottomSheetDialog.setContentView(view);

        final RadioGroup sortRadioGroup = view.findViewById(R.id.options_radiogroup);

        // TITLE
        ((TextView) view.findViewById(R.id.bottom_sheet_title)).setText(R.string.menu_sort);

        view.findViewById(R.id.option_1).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.option_2)).setText(R.string.a_z);
        ((TextView) view.findViewById(R.id.option_3)).setText(R.string.z_a);

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(SORT_PREF) == 0) {
            preferenceManager.setInt(SORT_PREF, R.id.option_2);
        }
        sortRadioGroup.check(preferenceManager.getInt(SORT_PREF));

        // ON SELECTING OPTION
        sortRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            preferenceManager.setInt(SORT_PREF, checkedId);
            bottomSheetDialog.dismiss();
            getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            getSupportFragmentManager().beginTransaction().attach(fragment).commit();
        });

        // CANCEL BUTTON
        view.findViewById(R.id.cancel_button).setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

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