/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.fragments.bottomsheets

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
    
    private var _binding: BottomSheetNoNetworkBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setCanceledOnTouchOutside(false)
            behavior.isDraggable = false
            
            // Prevent bottom sheet dismiss on back pressed
            setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // Do nothing
                    return@OnKeyListener true
                }
                false
            })
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
    
        _binding = BottomSheetNoNetworkBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).apply {
            dragHandle.isVisible = false
            bottomSheetTitle.apply {
                text = getString(R.string.no_network_title)
                // Set margin top to 12 dp, since drag handle is not visible now
                val params = layoutParams as ViewGroup.MarginLayoutParams
                val topMargin = (12 * requireContext().resources.displayMetrics.density).toInt()
                params.setMargins(params.leftMargin, topMargin, params.rightMargin, params.bottomMargin)
                requestLayout()
            }
        }
    
        // Retry
        footerBinding.positiveButton.apply {
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_retry)
            text = getString(R.string.retry)
            setOnClickListener {
                dismiss()
                positiveButtonClickListener.invoke()
            }
        }
    
        // Exit/Cancel
        footerBinding.negativeButton.apply {
            text = negativeButtonText
            setOnClickListener {
                dismiss()
                negativeButtonClickListener.invoke()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}