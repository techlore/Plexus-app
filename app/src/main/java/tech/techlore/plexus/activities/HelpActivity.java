package tech.techlore.plexus.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.fragments.help.HelpFragment;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MaterialToolbar toolbarRatingInfo = findViewById(R.id.toolbar_main);

    /*############################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbarRatingInfo);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_help);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarRatingInfo.setNavigationOnClickListener(v ->
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
