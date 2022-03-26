package tech.techlore.plexus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.settings.SettingsDefaultFragment;
import tech.techlore.plexus.models.PlexusData;

public class SettingsActivity extends AppCompatActivity {

    public MaterialToolbar toolbarSettings;
    public List<PlexusData> storeDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbarSettings = findViewById(R.id.toolbar_main);

    /*############################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbarSettings);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarSettings.setNavigationOnClickListener(v ->
                onBackPressed());

        findViewById(R.id.searchView).setVisibility(View.GONE); // HIDE SEARCH VIEW

        // GET LIST FROM MAIN ACTIVITY
        //noinspection unchecked
        storeDataList = (List<PlexusData>) getIntent().getSerializableExtra("plexusDataList");

        if (savedInstanceState == null){
            // DEFAULT FRAGMENT
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_host_fragment, new SettingsDefaultFragment())
                    .commitNow();
        }
    }

    @Override
    public void onBackPressed() {

        // IF NOT ON DEFAULT FRAGMENT, GO TO DEFAULT FRAGMENT
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        }

        // IF ON DEFAULT FRAGMENT, GO TO MAIN ACTIVITY
        else {
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra("plexusDataList", (Serializable) storeDataList));
            finish();
            overridePendingTransition(0, R.anim.fade_out_slide_to_end);
        }
    }

}
