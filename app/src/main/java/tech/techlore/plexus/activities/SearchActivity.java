package tech.techlore.plexus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.search.SearchDataFragment;
import tech.techlore.plexus.fragments.search.SearchInstalledFragment;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

public class SearchActivity extends AppCompatActivity {

    public List<PlexusData> dataList;
    public List<InstalledApp> installedList;
    public SearchView searchView;
    private InputMethodManager inputMethodManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        final MaterialToolbar searchToolbar = findViewById(R.id.toolbar_main);
        searchView = findViewById(R.id.searchView);
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(searchToolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchToolbar.setNavigationOnClickListener(view -> onBackPressed());

        searchView.setVisibility(View.VISIBLE);

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
                installedList = (List<InstalledApp>) intent.getSerializableExtra("installedList");
                DisplayFragment("Search Installed");

            }

        }

        // SHOW KEYBOARD WITH A SUBTLE DELAY
        // TO STOP FLICKERING OF FAB FROM PREVIOUS ACTIVITY
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            searchView.requestFocus();
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_HIDDEN);

        },
                400);

    }

    // SETUP FRAGMENTS
    private void DisplayFragment(String fragmentName) {

        Fragment fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (fragmentName.equals("Search Data")) {
            searchView.setQueryHint(getResources().getString(R.string.search_data));
            fragment = new SearchDataFragment();
        }

        else {
            searchView.setQueryHint(getResources().getString(R.string.search_installed));
            fragment = new SearchInstalledFragment();
        }

        transaction.replace(R.id.activity_host_fragment, fragment).commit();

    }

    // WHEN FINISHING ACTIVITY
    @Override
    public void finish() {

        // IF KEYBOARD IS SHOWN, CLOSE KEYBOARD FIRST
        // THEN FINISH ACTIVITY WITH A SLIGHT DELAY
        if (searchView.hasFocus()) {

            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.RESULT_SHOWN);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        super.finish();
                        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom);

                    },
                    400);

        }

        // ELSE FINISH ACTIVITY IMMEDIATELY
        else {
            super.finish();
            overridePendingTransition(0, R.anim.fade_out_slide_to_bottom);
        }

    }
}
