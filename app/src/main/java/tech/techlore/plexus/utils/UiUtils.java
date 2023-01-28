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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import tech.techlore.plexus.R;
import tech.techlore.plexus.databinding.BottomSheetFooterBinding;
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding;
import tech.techlore.plexus.databinding.BottomSheetLongClickBinding;

public class UiUtils {

    // Horizontally scroll text,
    // if text is too long
    public static void hScrollText(TextView textView) {
        textView.setSingleLine();
        textView.setSelected(true);
    }

    // Long click bottom sheet
    public static void LongClickBottomSheet(Activity activity, String nameString, String packageNameString, /*String plexusVersionString,
                                            String dgRatingString, String mgRatingString,
                                            String dgNotesString, String mgNotesString,*/
                                            CoordinatorLayout coordinatorLayout, View anchorView){

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetDialog.setCancelable(true);

        final BottomSheetLongClickBinding bottomSheetBinding = BottomSheetLongClickBinding.inflate(activity.getLayoutInflater());
        final BottomSheetHeaderBinding headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        final BottomSheetFooterBinding footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        final String playStoreString = "https://play.google.com/store/apps/details?id=" + packageNameString;

        headerBinding.bottomSheetTitle.setText(nameString);

        // Play store
        bottomSheetBinding.playStore.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            OpenURL(activity, playStoreString, coordinatorLayout, anchorView);
        });

        // Share
        bottomSheetBinding.share.setOnClickListener(v -> {
            Share(activity,
                  nameString, packageNameString, /*plexusVersionString,
                  dgRatingString, mgRatingString,
                  dgNotesString, mgNotesString,*/
                  playStoreString);
            bottomSheetDialog.dismiss();
        });

        footerBinding.positiveButton.setVisibility(View.GONE);

        // Cancel
        footerBinding.negativeButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        bottomSheetDialog.show();
    }

}
