/*
 * Copyright (c) 2022 Techlore
 *
 *  This file is part of Plexus.
 *
 *  Plexus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plexus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plexus.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.utils;

import static tech.techlore.plexus.utils.IntentUtils.OpenURL;
import static tech.techlore.plexus.utils.IntentUtils.Share;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.adapters.ViewPagerAdapter;
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding;
import tech.techlore.plexus.databinding.BottomSheetLongClickBinding;
import tech.techlore.plexus.databinding.DialogFooterBinding;

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
    public static void ReloadViewPagerFragment(ViewPager2 viewPager2, ViewPagerAdapter viewPagerAdapter, int position) {

        viewPager2.setAdapter(null);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setCurrentItem(position);

    }

    // LONG CLICK BOTTOM SHEET
    public static void LongClickBottomSheet(Activity activity, String nameString, String packageNameString, String plexusVersionString,
                                      String dgRatingString, String mgRatingString,
                                      String dgNotesString, String mgNotesString){

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        final BottomSheetLongClickBinding bottomSheetBinding = BottomSheetLongClickBinding.inflate(activity.getLayoutInflater());
        final BottomSheetHeaderBinding headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        final DialogFooterBinding footerBinding = DialogFooterBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        final String playStoreString = "https://play.google.com/store/apps/details?id=" + packageNameString;

        // TITLE
        headerBinding.bottomSheetTitle.setText(nameString);

        // PLAY STORE
        bottomSheetBinding.playStore.setOnClickListener(v -> {
            OpenURL(activity, playStoreString);
            bottomSheetDialog.dismiss();
        });

        // SHARE
        bottomSheetBinding.share.setOnClickListener(v -> {
            Share(activity,
                  nameString, packageNameString, plexusVersionString,
                  dgRatingString, mgRatingString,
                  dgNotesString, mgNotesString,
                  playStoreString);
            bottomSheetDialog.dismiss();
        });

        // POSITIVE BUTTON
        footerBinding.positiveButton.setVisibility(View.GONE);

        // NEGATIVE BUTTON
        footerBinding.negativeButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

}
