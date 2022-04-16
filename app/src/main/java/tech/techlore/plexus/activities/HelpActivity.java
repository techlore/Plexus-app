package tech.techlore.plexus.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.ActivityMainBinding;
import tech.techlore.plexus.fragments.help.HelpFragment;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding activityBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

    /*############################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(activityBinding.toolbarMain);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_help);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activityBinding.toolbarMain.setNavigationOnClickListener(v ->
                onBackPressed());

        // DISPLAY FRAGMENT
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_host_fragment, new HelpFragment())
                    .commitNow();
        }

    }

    // ON BACK PRESSED
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.fade_out_slide_to_end);

    }

}
