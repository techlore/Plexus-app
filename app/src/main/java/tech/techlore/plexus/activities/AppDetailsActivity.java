package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.Utility.RatingColor;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;

import tech.techlore.plexus.R;

public class AppDetailsActivity extends AppCompatActivity {

    private static String nameString, packageNameString, versionString,
            dgRatingString, mgRatingString, dgNotesString, mgNotesString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        final MaterialToolbar toolbar = findViewById(R.id.toolbar_details);

        Intent intent = getIntent();
        nameString = intent.getStringExtra("name");
        packageNameString = intent.getStringExtra("packageName");
        versionString = intent.getStringExtra("version");
        dgRatingString = intent.getStringExtra("dgRating");
        mgRatingString = intent.getStringExtra("mgRating");
        dgNotesString = intent.getStringExtra("dgNotes");
        mgNotesString = intent.getStringExtra("mgNotes");
        TextView name = findViewById(R.id.name_details);
        TextView packageName = findViewById(R.id.package_name_details);
        TextView version = findViewById(R.id.version_details);
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
        version.setText(versionString);
        dgRating.setText(dgRatingString);
        mgRating.setText(mgRatingString);
        dgNotes.setText(dgNotesString);
        mgNotes.setText(mgNotesString);

        RatingColor(this, dgRatingColor, dgRatingString);
        RatingColor(this, mgRatingColor, mgRatingString);

    }

    // MENU
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_app_details, menu);

        // PLAY STORE
        menu.findItem(R.id.menu_play_store).setOnMenuItemClickListener(item -> {
            try
            {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id="
                                + packageNameString)));
            }
            // IF BROWSERS NOT INSTALLED, SHOW TOAST
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(this, getString(R.string.no_browsers), Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        // SHARE
        menu.findItem(R.id.menu_share).setOnMenuItemClickListener(item -> {
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
                                            + versionString
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
                                            + mgNotesString),
                    getString(R.string.share_via)));
            return true;
        });

        return true;
    }

    // SET TRANSITION WHEN FINISHING ACTIVITY
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_scale_out);
    }

}
