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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetDeleteBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding

abstract class BaseDeleteBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetDeleteBinding? = null
    protected val bottomSheetBinding get() = _binding!!
    protected val footerBinding by lazy {
        BottomSheetFooterBinding.bind(bottomSheetBinding.root)
    }
    
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
        
        _binding = BottomSheetDeleteBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val negativeButton = footerBinding.negativeButton
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getTitleText()
        
        bottomSheetBinding.deleteDesc.text = getDescText()
        
        // Delete
        footerBinding.positiveButton.apply {
            setBackgroundColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorError))
            setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnError))
            text = getString(R.string.delete)
            setOnClickListener {
                isEnabled = false
                negativeButton.isEnabled = false
                bottomSheetBinding.deleteProgressIndicator.show()
                onPositiveBtnClick()
            }
        }
        
        // Cancel
        negativeButton.setOnClickListener {
            dismiss()
        }
    }
    
    // Subclasses must override
    protected abstract fun getTitleText(): String
    protected abstract fun getDescText(): String
    protected abstract fun onPositiveBtnClick()
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}