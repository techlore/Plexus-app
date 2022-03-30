package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.Utility.RatingColor;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;

import tech.techlore.plexus.R;

public class AppDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        Intent intent = getIntent();
        final MaterialToolbar toolbar = findViewById(R.id.toolbar_details);
        final String nameString = intent.getStringExtra("name");
        final String packageNameString = intent.getStringExtra("packageName");
        final String plexusVersionString = intent.getStringExtra("plexusVersion");
        final String installedVersionString = intent.getStringExtra("installedVersion");
        final String dgRatingString = intent.getStringExtra("dgRating");
        final String mgRatingString = intent.getStringExtra("mgRating");
        final String dgNotesString = intent.getStringExtra("dgNotes");
        final String mgNotesString = intent.getStringExtra("mgNotes");
        final String playStoreString = "https://play.google.com/store/apps/details?id=" + packageNameString;
        TextView name = findViewById(R.id.name_details);
        TextView packageName = findViewById(R.id.package_name_details);
        TextView plexusVersion = findViewById(R.id.plexus_version_details);
        TextView installedVersion = findViewById(R.id.installed_version_details);
        TextView dgRating = findViewById(R.id.dg_rating_details);
        TextView mgRating = findViewById(R.id.mg_rating_details);
        TextView dgNotes = findViewById(R.id.dg_notes);
        TextView mgNotes = findViewById(R.id.mg_notes);
        ImageView dgRatingColor = findViewById(R.id.dg_rating_color);
        ImageView mgRatingColor = findViewById(R.id.mg_rating_color);

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // SET DATA RECEIVED
        name.setText(nameString);
        packageName.setText(packageNameString);
        plexusVersion.setText(plexusVersionString);
        if (installedVersionString != null) {
            findViewById(R.id.plexus_ver_text).setVisibility(View.VISIBLE);
            findViewById(R.id.installed_ver_layout).setVisibility(View.VISIBLE);
            installedVersion.setText(installedVersionString);
        }
        dgRating.setText(dgRatingString);
        mgRating.setText(mgRatingString);
        dgNotes.setText(dgNotesString);
        mgNotes.setText(mgNotesString);

        RatingColor(this, dgRatingColor, dgRatingString);
        RatingColor(this, mgRatingColor, mgRatingString);

        // PLAY STORE LINK
        findViewById(R.id.play_store)
                .setOnClickListener(v -> {
                    try
                    {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(playStoreString)));
                    }
                    // IF BROWSERS NOT INSTALLED, SHOW TOAST
                    catch (ActivityNotFoundException e)
                    {
                        Toast.makeText(this, getString(R.string.no_browsers), Toast.LENGTH_SHORT).show();
                    }
                });

        // SHARE
        findViewById(R.id.share)
                .setOnClickListener(v ->
                        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT,
                                        getResources().getString(R.string.application) + ": "
                                        + nameString
                                        + "\n"
                                        + getResources().getString(R.string.package_name) + ": "
                                        + packageNameString
                                        + "\n"
                                        + getResources().getString(R.string.version) + ": "
                                        + plexusVersionString
                                        + "\n"
                                        + getResources().getString(R.string.dg_rating) + ": "
                                        + dgRatingString
                                        + "\n"
                                        + getResources().getString(R.string.mg_rating) + ": "
                                        + mgRatingString
                                        + "\n"
                                        + getResources().getString(R.string.de_Googled) + " "
                                        + getResources().getString(R.string.notes) + ": "
                                        + dgNotesString
                                        + "\n"
                                        + getResources().getString(R.string.microG) + " "
                                        + getResources().getString(R.string.notes) + ": "
                                        + mgNotesString
                                        + "\n"
                                        + getResources().getString(R.string.play_store) + ": "
                                        + playStoreString),
                        getString(R.string.share_via))));

    }

    // SET TRANSITION WHEN FINISHING ACTIVITY
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_scale_out);
    }

}
