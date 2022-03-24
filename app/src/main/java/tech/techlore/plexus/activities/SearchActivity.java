package tech.techlore.plexus.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.search.SearchDataFragment;
import tech.techlore.plexus.fragments.search.SearchInstalledFragment;
import tech.techlore.plexus.models.PlexusData;

public class SearchActivity extends AppCompatActivity {

    public List<PlexusData> list;
    public SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        final MaterialToolbar toolbar = findViewById(R.id.toolbar_search);
        searchView = findViewById(R.id.searchView);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // GET LIST FROM MAIN ACTIVITY
        //noinspection unchecked
        list = (List<PlexusData>) intent.getSerializableExtra("plexusDataList");

        // DEFAULT FRAGMENT
        if (savedInstanceState == null) {

            if (intent.getStringExtra("from").equals("plexusData")) {
                DisplayFragment("Search Data");
            }
            else {
                DisplayFragment("Search Installed");
            }

        }

    }

    // SETUP FRAGMENTS
    private void DisplayFragment(String fragmentName) {

        Fragment fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (fragmentName.equals("Search Data")) {
            searchView.setQueryHint(getResources().getString(R.string.search_data));
            fragment = new SearchDataFragment();
            transaction.setCustomAnimations(R.anim.slide_from_start, R.anim.slide_to_end,
                    R.anim.slide_from_end, R.anim.slide_to_start);
        }

        else {
            searchView.setQueryHint(getResources().getString(R.string.search_installed));
            fragment = new SearchInstalledFragment();
            transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                    R.anim.slide_from_start, R.anim.slide_to_end);
        }

        transaction.replace(R.id.activity_host_fragment, fragment)
                .commit();

    }

    // SET TRANSITION WHEN FINISHING ACTIVITY
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom);
    }
}
