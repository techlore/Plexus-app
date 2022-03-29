package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.Utility.RatingColor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import tech.techlore.plexus.R;

public class AppDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        final MaterialToolbar toolbar = findViewById(R.id.toolbar_details);

        Intent intent = getIntent();
        TextView name = findViewById(R.id.name_details);
        TextView packageName = findViewById(R.id.package_name_details);
        TextView version = findViewById(R.id.version_details);
        TextView dgNotes = findViewById(R.id.dg_notes);
        TextView mgNotes = findViewById(R.id.mg_notes);
        TextView dgRating = findViewById(R.id.dg_rating_details);
        TextView mgRating = findViewById(R.id.mg_rating_details);
        ImageView dgRatingColor = findViewById(R.id.dg_rating_color);
        ImageView mgRatingColor = findViewById(R.id.mg_rating_color);

        /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // SET DATA RECEIVED
        if (intent  != null) {

            name.setText(intent.getStringExtra("name"));
            packageName.setText(intent.getStringExtra("packageName"));
            version.setText(intent.getStringExtra("version"));
            dgNotes.setText(intent.getStringExtra("dgNotes"));
            mgNotes.setText(intent.getStringExtra("mgNotes"));
            dgRating.setText(intent.getStringExtra("dgRating"));
            mgRating.setText(intent.getStringExtra("mgRating"));

            RatingColor(this, dgRatingColor, intent.getStringExtra("dgRating"));
            RatingColor(this, mgRatingColor, intent.getStringExtra("mgRating"));

        }

    }

    // SET TRANSITION WHEN FINISHING ACTIVITY
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_scale_out);
    }

}
