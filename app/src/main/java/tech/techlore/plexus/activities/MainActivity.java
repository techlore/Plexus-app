package tech.techlore.plexus.activities;

import static tech.techlore.plexus.preferences.PreferenceManager.THEME_PREF;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.AboutFragment;
import tech.techlore.plexus.fragments.AppDetailsFragment;
import tech.techlore.plexus.fragments.MainFragment;
import tech.techlore.plexus.fragments.RatingInfoFragment;
import tech.techlore.plexus.models.App;
import tech.techlore.plexus.preferences.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private Fragment fragment;
    public ExtendedFloatingActionButton extFab;
    public List<App> list;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(this);
        final MaterialToolbar toolbar = findViewById(R.id.toolbar_main);
        extFab = findViewById(R.id.ext_fab_main);

        /*===========================================================================================*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // GET LIST FROM SPLASH ACTIVITY
        list = (List<App>) getIntent().getSerializableExtra("appsList");

        // DEFAULT FRAGMENT
        if (savedInstanceState==null) {
            DisplayFragment("Main");
        }

        // EXT FAB
        // OPEN SEARCH ACTIVITY
        extFab.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            finish();
            overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement);
        });

    }

    // SETUP FRAGMENTS
    public void DisplayFragment(String fragmentName) {
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

        switch (fragmentName) {

            case "Main":
                fragment = new MainFragment();
                break;

            case "Rating Info":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_rating_info);
                fragment = new RatingInfoFragment();
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end);
                break;

            case "About":
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_about);
                fragment= new AboutFragment();
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end);
                break;
        }

        // HIDE BACK ICON ON MAIN FRAGMENT
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() >= 1);

        // SHOW EXT FAB ONLY ON MAIN FRAGMENT
        if (fragmentName.equals("Main")){
            extFab.show();
        }
        else{
            extFab.hide();
        }

        transaction
                .replace(R.id.activity_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    // SEPARATE FUNCTION FOR APP DETAILS FRAGMENT
    public void AppDetails(String name, String packageName, String version,
                           String dgNotes, String mgNotes, String dgRating, String mgRating) {

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.details);
        fragment = new AppDetailsFragment();

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("packageName", packageName);
        args.putString("version", version);
        args.putString("dgNotes", dgNotes);
        args.putString("mgNotes", mgNotes);
        args.putString("dgRating", dgRating);
        args.putString("mgRating", mgRating);
        fragment.setArguments(args);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        extFab.hide();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                     R.anim.slide_from_start, R.anim.slide_to_end)
                .replace(R.id.activity_host_fragment, fragment)
                .addToBackStack(null)
                .commit();

    }

    // MENU
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        // SHOW MENU ICONS ONLY IN MAIN FRAGMENT
        menu.findItem(R.id.action_settings).setVisible(getSupportFragmentManager().getBackStackEntryCount() == 1);
        menu.findItem(R.id.menu_score_info).setVisible(getSupportFragmentManager().getBackStackEntryCount() == 1);

        // SCORES INFO
        menu.findItem(R.id.menu_score_info).setOnMenuItemClickListener(item -> {
            DisplayFragment("Rating Info");
            return true;
        });

        // THEME
        menu.findItem(R.id.menu_theme).setOnMenuItemClickListener(item -> {
            ThemeBottomSheet();
            return true;
        });

        // REPORT AN ISSUE
        menu.findItem(R.id.menu_report_issue).setOnMenuItemClickListener(item -> {
            try
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/techlore/Plexus-app/issues")));
            }
            // IF BROWSERS NOT INSTALLED, SHOW TOAST
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(getApplicationContext(), getString(R.string.no_browsers), Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        // ABOUT
        menu.findItem(R.id.menu_about).setOnMenuItemClickListener(item -> {
            DisplayFragment("About");
            return true;
        });

        return true;
    }

    // THEME BOTTOM SHEET
    private void ThemeBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        @SuppressLint("InflateParams") final View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_theme, null);
        bottomSheetDialog.setContentView(view);

        final RadioGroup themeRadioGroup = view.findViewById(R.id.options_radiogroup);

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(THEME_PREF)==0){
            if (Build.VERSION.SDK_INT>=29){
                preferenceManager.setInt(THEME_PREF, R.id.option_default);
            }
            else{
                preferenceManager.setInt(THEME_PREF, R.id.option_light);
            }
        }
        themeRadioGroup.check(preferenceManager.getInt(THEME_PREF));

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(R.string.menu_theme);

        // CANCEL BUTTON
        view.findViewById(R.id.cancel_button).setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // SHOW SYSTEM DEFAULT OPTION ONLY ON SDK 29 AND ABOVE
        if (Build.VERSION.SDK_INT>=29){
            view.findViewById(R.id.option_default).setVisibility(View.VISIBLE);
        }
        else{
            view.findViewById(R.id.option_default).setVisibility(View.GONE);
        }

        // ON SELECTING OPTION
        ((RadioGroup)view.findViewById(R.id.options_radiogroup))
                .setOnCheckedChangeListener((radioGroup, checkedId) -> {

                    if (checkedId == R.id.option_default) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    else if (checkedId == R.id.option_light) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }

                    else if (checkedId == R.id.option_dark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }

                    preferenceManager.setInt(THEME_PREF, checkedId);
                    bottomSheetDialog.dismiss();
                    this.recreate();
                });

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    // ON BACK PRESSED
    @Override
    public void onBackPressed() {

        // IF NOT ON DEFAULT FRAGMENT, GO TO DEFAULT FRAGMENT
        if (getSupportFragmentManager().getBackStackEntryCount()>1){
            getSupportFragmentManager().popBackStack();
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            extFab.show();
        }

        // IF ON DEFAULT FRAGMENT, FINISH ACTIVITY
        else {
            finish();
        }
    }
}