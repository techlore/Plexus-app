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

package tech.techlore.plexus.bottomsheets.common

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetErrorBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx

abstract class BaseErrorBottomSheet : BottomSheetDialogFragment() {
    
    // This is used to set the description text
    // either directly to a string, or from a string resource
    protected sealed class TextOrRes {
        data class Res(@param:StringRes val resId: Int) : TextOrRes()
        data class Text(val string: String) : TextOrRes()
    }
    
    private var _binding: BottomSheetErrorBinding? = null
    private val bottomSheetBinding get() = _binding!!
    protected abstract val titleTextResId: Int
    protected abstract val descriptionText: TextOrRes
    protected abstract val negativeButtonText: CharSequence
    protected abstract val positiveBtnClickAction: () -> Unit
    protected abstract val negativeBtnClickAction: () -> Unit
    
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
        
        _binding = BottomSheetErrorBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).apply {
            dragHandle.isVisible = false
            bottomSheetTitle.apply {
                text = getString(titleTextResId)
                // Set margin top to 12 dp, since drag handle is not visible now
                val params = layoutParams as ViewGroup.MarginLayoutParams
                val topMargin = convertDpToPx(requireContext(), 12f)
                params.setMargins(params.leftMargin, topMargin, params.rightMargin, params.bottomMargin)
                requestLayout()
            }
        }
        
        // Description
        bottomSheetBinding.descText.setTextOrRes(descriptionText)
        
        // Retry
        footerBinding.positiveButton.apply {
            text = getString(R.string.retry)
            setOnClickListener {
                dismiss()
                positiveBtnClickAction()
            }
        }
        
        // Exit/Cancel
        footerBinding.negativeButton.apply {
            text = negativeButtonText
            setOnClickListener {
                dismiss()
                negativeBtnClickAction()
            }
        }
    }
    
    private fun MaterialTextView.setTextOrRes(value: TextOrRes) {
        text =
            when (value) {
                is TextOrRes.Res  -> getString(value.resId)
                is TextOrRes.Text -> value.string
            }
    }
    
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
