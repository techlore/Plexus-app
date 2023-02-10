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

package tech.techlore.plexus.utils

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetLongClickBinding

class UiUtils {

    companion object {

        // Horizontally scroll text,
        // if text is too long
        fun hScrollText(textView: TextView) {
            textView.setSingleLine()
            textView.isSelected = true
        }

        // Long click bottom sheet
        fun longClickBottomSheet(
            activity: Activity, nameString: String, packageNameString: String,  /*String plexusVersionString,
            String dgRatingString, String mgRatingString, String dgNotesString, String mgNotesString,*/
            coordinatorLayout: CoordinatorLayout, anchorView: View) {

            val bottomSheetDialog = BottomSheetDialog(activity)
            bottomSheetDialog.setCancelable(true)

            val bottomSheetBinding = BottomSheetLongClickBinding.inflate(activity.layoutInflater)
            val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
            val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
            bottomSheetDialog.setContentView(bottomSheetBinding.root)

            val playStoreString = "https://play.google.com/store/apps/details?id=$packageNameString"
            headerBinding.bottomSheetTitle.text = nameString

            // Play store
            bottomSheetBinding.playStore.setOnClickListener {
                bottomSheetDialog.dismiss()
                IntentUtils.openURL(activity, playStoreString, coordinatorLayout, anchorView)
            }

            // Share
            bottomSheetBinding.share.setOnClickListener {
                IntentUtils.share(
                    activity,
                    nameString, packageNameString,  /*plexusVersionString,
                  dgRatingString, mgRatingString,
                  dgNotesString, mgNotesString,*/
                    playStoreString
                )
                bottomSheetDialog.dismiss()
            }
            footerBinding.positiveButton.visibility = View.GONE

            // Cancel
            footerBinding.negativeButton.setOnClickListener { bottomSheetDialog.cancel() }
            bottomSheetDialog.show()
        }
    }
}