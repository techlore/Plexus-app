package tech.techlore.plexus.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.main.AppDetailsFragment;
import tech.techlore.plexus.fragments.search.SearchFragment;
import tech.techlore.plexus.models.App;

public class SearchActivity extends AppCompatActivity {

    public List<App> list;
    private Fragment fragment, fragmentHide;
    public SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final MaterialToolbar toolbar = findViewById(R.id.toolbar_search);
        searchView = findViewById(R.id.searchView);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // GET LIST FROM MAIN ACTIVITY
        //noinspection unchecked
        list = (List<App>) getIntent().getSerializableExtra("appsList");

        // DEFAULT FRAGMENT
        if (savedInstanceState == null) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
            searchView.setVisibility(View.VISIBLE);
            fragment = new SearchFragment();
            fragmentHide = fragment;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }

    }

    // APP DETAILS FRAGMENT
    public void AppDetails(String name, String packageName, String version,
                           String dgNotes, String mgNotes, String dgRating, String mgRating) {

        searchView.setVisibility(View.GONE);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
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

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                     R.anim.slide_from_start, R.anim.slide_to_end)
                .hide(fragmentHide) // HIDE THE PREVIOUS FRAGMENT, NOT REPLACE, OR ELSE LIST ISSUES ON BACK PRESSED
                .add(R.id.activity_host_fragment, fragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onBackPressed() {

        // IF NOT ON DEFAULT FRAGMENT, GO TO DEFAULT FRAGMENT
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
            searchView.setVisibility(View.VISIBLE);
        }

        // IF ON DEFAULT FRAGMENT, FINISH ACTIVITY
        else {
            finish();
            overridePendingTransition(0, R.anim.fade_out_slide_to_bottom);
        }

    }
}
