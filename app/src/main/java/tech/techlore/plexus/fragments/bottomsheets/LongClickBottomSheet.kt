/*
 * Copyright (c) 2022-present Techlore
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

package tech.techlore.plexus.fragments.bottomsheets

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetLongClickBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL
import tech.techlore.plexus.utils.IntentUtils.Companion.share

class LongClickBottomSheet(
    private val activity: Activity,
    private val nameString: String,
    private val packageNameString: String,
    /*String plexusVersionString,
    String dgRatingString,
    String mgRatingString,
    String dgNotesString,
    String mgNotesString,*/
    private val coordinatorLayout: CoordinatorLayout,
    private val anchorView: View
) : BottomSheetDialogFragment() {
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        val bottomSheetBinding = BottomSheetLongClickBinding.inflate(inflater, container, false)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        val playStoreString = "https://play.google.com/store/apps/details?id=$packageNameString"
        
        headerBinding.bottomSheetTitle.text = nameString
        
        // Play store
        bottomSheetBinding.playStore.setOnClickListener {
            dismiss()
            openURL(activity, playStoreString, coordinatorLayout, anchorView)
        }
        
        // Share
        bottomSheetBinding.share.setOnClickListener {
            share(activity,
                  nameString,
                  packageNameString,
                /*plexusVersionString,
                  dgRatingString,
                  mgRatingString,
                  dgNotesString,
                  mgNotesString,*/
                  playStoreString)
            
            dismiss()
        }
        footerBinding.positiveButton.visibility = View.GONE
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
        
        return bottomSheetBinding.root
    }
}
