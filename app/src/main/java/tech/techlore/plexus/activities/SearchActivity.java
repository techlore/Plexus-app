package tech.techlore.plexus.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.search.SearchFragment;
import tech.techlore.plexus.models.PlexusData;

public class SearchActivity extends AppCompatActivity {

    public List<PlexusData> list;
    public SearchView searchView;
    public TabLayout searchTabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final MaterialToolbar toolbar = findViewById(R.id.toolbar_search);
        searchTabLayout = findViewById(R.id.search_tab_layout);
        searchView = findViewById(R.id.searchView);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // GET LIST FROM MAIN ACTIVITY
        //noinspection unchecked
        list = (List<PlexusData>) getIntent().getSerializableExtra("appsList");

        // DEFAULT FRAGMENT
        if (savedInstanceState == null) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
            Fragment fragment = new SearchFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }

    }

    @Override
    public void onBackPressed() {

        // IF NOT ON DEFAULT FRAGMENT, GO TO DEFAULT FRAGMENT
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
            searchTabLayout.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
        }

        // IF ON DEFAULT FRAGMENT, FINISH ACTIVITY
        else {
            finish();
            overridePendingTransition(0, R.anim.fade_out_slide_to_bottom);
        }

    }
}
