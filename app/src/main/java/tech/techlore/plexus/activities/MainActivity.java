package tech.techlore.plexus.activities;

import static tech.techlore.plexus.preferences.PreferenceManager.SORT_PREF;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.main.MainDefaultFragment;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.preferences.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private Fragment fragment;
    public int selectedTab = 0; // HELPS KEEPING CHILD FRAG POSITION WHEN PARENT FRAG REATTACHED WHILE USING SORT
    public List<PlexusData> dataList;
    public List <InstalledApp> installedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(this);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(findViewById(R.id.toolbar_main));
        findViewById(R.id.searchView).setVisibility(View.GONE); // HIDE SEARCH VIEW
        findViewById(R.id.divider).setVisibility(View.GONE); // HIDE DIVIDER

        // GET LIST FROM SPLASH ACTIVITY
        //noinspection unchecked
        dataList = (List<PlexusData>) getIntent().getSerializableExtra("plexusDataList");

        // DEFAULT FRAGMENT
        if (savedInstanceState == null) {
            fragment = new MainDefaultFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }

    }

    // MENU
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        // SHOW MENU ICONS ONLY IN MAIN FRAGMENT
        menu.findItem(R.id.menu_overflow).setVisible(getSupportFragmentManager().getBackStackEntryCount() == 1);
        menu.findItem(R.id.menu_rating_info).setVisible(getSupportFragmentManager().getBackStackEntryCount() == 1);

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
            startActivity(new Intent(this, SettingsActivity.class)
                    .putExtra("plexusDataList", (Serializable) dataList));
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