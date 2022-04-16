package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.IntentUtils.OpenURL;
import static tech.techlore.plexus.utils.UiUtils.RatingColor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding;

public class AppDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityAppDetailsBinding activityBinding = ActivityAppDetailsBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        Intent intent = getIntent();
        final String nameString = intent.getStringExtra("name");
        final String packageNameString = intent.getStringExtra("packageName");
        final String plexusVersionString = intent.getStringExtra("plexusVersion");
        final String installedVersionString = intent.getStringExtra("installedVersion");
        final String dgRatingString = intent.getStringExtra("dgRating");
        final String mgRatingString = intent.getStringExtra("mgRating");
        final String dgNotesString = intent.getStringExtra("dgNotes");
        final String mgNotesString = intent.getStringExtra("mgNotes");
        final String playStoreString = "https://play.google.com/store/apps/details?id=" + packageNameString;

    /*###########################################################################################*/

        // TOOLBAR AS ACTIONBAR
        setSupportActionBar(activityBinding.toolbarDetails);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        activityBinding.toolbarDetails.setNavigationOnClickListener(v -> onBackPressed());

        // SET DATA RECEIVED
        activityBinding.nameDetails.setText(nameString);
        activityBinding.packageNameDetails.setText(packageNameString);
        activityBinding.plexusVersionDetails.setText(plexusVersionString);
        if (installedVersionString != null) {
            activityBinding.plexusText.setVisibility(View.VISIBLE);
            activityBinding.installedVerLayout.setVisibility(View.VISIBLE);
            activityBinding.installedVersionDetails.setText(installedVersionString);
        }
        activityBinding.dgRatingDetails.setText(dgRatingString);
        activityBinding.mgRatingDetails.setText(mgRatingString);
        activityBinding.dgNotes.setText(dgNotesString);
        activityBinding.mgNotes.setText(mgNotesString);

        RatingColor(this, activityBinding.dgRatingColor, dgRatingString);
        RatingColor(this, activityBinding.mgRatingColor, mgRatingString);

        // PLAY STORE URL
        activityBinding.playStore
                .setOnClickListener(v ->
                    OpenURL(this, playStoreString));

        // SHARE
        activityBinding.share
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
