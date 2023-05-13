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

import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetNoNetworkBinding

class NoNetworkBottomSheet(
    private val negativeButtonText: String,
    private val positiveButtonClickListener: () -> Unit,
    private val negativeButtonClickListener: () -> Unit
) : BottomSheetDialogFragment() {
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
    
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.behavior.isDraggable = false
    
        // Prevent bottom sheet dismiss on back pressed
        bottomSheetDialog.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // Do nothing
                return@OnKeyListener true
            }
            false
        })
    
        val bottomSheetBinding = BottomSheetNoNetworkBinding.inflate(inflater, container, false)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        // Title
        headerBinding.bottomSheetTitle.text = getString(R.string.no_network_title)
        
        // Retry
        footerBinding.positiveButton.text = getString(R.string.retry)
        footerBinding.positiveButton.setOnClickListener {
            dismiss()
            positiveButtonClickListener.invoke()
        }
        
        // Exit/Cancel
        footerBinding.negativeButton.text = negativeButtonText
        footerBinding.negativeButton.setOnClickListener {
            dismiss()
            negativeButtonClickListener.invoke()
        }
        
        return bottomSheetBinding.root
    }
    
}