package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.IntentUtils.SendListsIntent;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.settings.SettingsDefaultFragment;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

public class SettingsActivity extends AppCompatActivity {

    public MaterialToolbar toolbarSettings;
    private List<PlexusData> storeDataList;
    private List <InstalledApp> storeInstalledList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        toolbarSettings = findViewById(R.id.toolbar_main);

    /*############################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbarSettings);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarSettings.setNavigationOnClickListener(v ->
                onBackPressed());

        // GET LISTS FROM MAIN ACTIVITY
        //noinspection unchecked
        storeDataList = (List<PlexusData>) intent.getSerializableExtra("plexusDataList");
        //noinspection unchecked
        storeInstalledList = (List<InstalledApp>) intent.getSerializableExtra("installedAppsList");

        if (savedInstanceState == null){
            // DEFAULT FRAGMENT
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_host_fragment, new SettingsDefaultFragment())
                    .commitNow();
        }
    }

    // ON BACK PRESSED
    @Override
    public void onBackPressed() {

        // IF NOT ON DEFAULT FRAGMENT, GO TO DEFAULT FRAGMENT
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        }

        // IF ON DEFAULT FRAGMENT,
        // GIVE LISTS BACK TO MAIN ACTIVITY
        // AND FINISH THIS ACTIVITY
        else {
            SendListsIntent(this, MainActivity.class,
                    (Serializable) storeDataList, (Serializable) storeInstalledList);
            finish();
            overridePendingTransition(0, R.anim.fade_out_slide_to_end);
        }
    }

}
