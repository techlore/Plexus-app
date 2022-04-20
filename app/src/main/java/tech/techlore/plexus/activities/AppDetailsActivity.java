package tech.techlore.plexus.activities;

import static tech.techlore.plexus.utils.IntentUtils.OpenURL;
import static tech.techlore.plexus.utils.IntentUtils.Share;
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
        activityBinding.playStoreImg
                .setOnClickListener(v ->
                    OpenURL(this, playStoreString));

        // SHARE
        activityBinding.shareImg
                .setOnClickListener(v ->
                        Share(this,
                                nameString, packageNameString, plexusVersionString,
                                dgRatingString, mgRatingString,
                                dgNotesString, mgNotesString,
                                playStoreString));

    }

    // SET TRANSITION WHEN FINISHING ACTIVITY
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_scale_out);
    }

}
