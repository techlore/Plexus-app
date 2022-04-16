package tech.techlore.plexus.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import tech.techlore.plexus.R;

public class UiUtils {

    // INFLATE VIEW STUB
    public static void InflateViewStub(ViewStub viewStub) {
            viewStub.inflate();
    }

    // HORIZONTALLY SCROLL TEXT
    // IF TEXT IS TOO LONG
    public static void hScrollText(TextView textView) {
        textView.setSingleLine();
        textView.setSelected(true);
    }

    // SET BACKGROUND COLOR BASED ON RATING
    public static void RatingColor(Context context, View view, String rating) {

        switch (rating) {

            case "X":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.ratingXColor));
                break;

            case "1":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating1Color));
                break;

            case "2":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating2Color));
                break;

            case "3":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating3Color));
                break;

            case "4":
                view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.rating4Color));
                break;

        }

    }

    // RELOAD FRAGMENT
    public static void ReloadFragment(FragmentManager fragmentManager, Fragment fragment) {

        fragmentManager.beginTransaction().detach(fragment).commit();
        fragmentManager.beginTransaction().attach(fragment).commit();

    }

}
