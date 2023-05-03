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

package tech.techlore.plexus.fragments.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tech.techlore.plexus.R

class NoNetworkDialog(
    private val negativeButtonText: String,
    private val positiveButtonClickListener: () -> Unit,
    private val negativeButtonClickListener: () -> Unit
) : DialogFragment() {
    
    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false) // .setCancelable(false) won't work in onCreateDialog
        // Prevent dialog dismiss on back pressed
        dialog?.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // Do nothing
                return@OnKeyListener true
            }
            false
        })
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        
        return MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)
            
            .setTitle(R.string.no_network_title)
            
            .setMessage(R.string.no_network_subtitle)
            
            .setPositiveButton(R.string.retry) { _, _ ->
                positiveButtonClickListener.invoke()
            }
            
            .setNegativeButton(negativeButtonText) { _, _ ->
                negativeButtonClickListener.invoke()
            }
            
            .create()
    }
    
}