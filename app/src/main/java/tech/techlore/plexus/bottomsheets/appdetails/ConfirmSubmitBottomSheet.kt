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

package tech.techlore.plexus.bottomsheets.appdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetDeleteBinding
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.interfaces.details.SubmitConfirmClickListener

// Reuse "Delete Account" bottom sheet layout
class ConfirmSubmitBottomSheet(
    private val appName: String,
    private val submitConfirmClickListener: SubmitConfirmClickListener
) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetDeleteBinding? = null
    private val bottomSheetBinding get() = _binding !!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetDeleteBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = appName
        
        bottomSheetBinding.deleteDesc.text = getString(R.string.about_to_submit)
        bottomSheetBinding.deleteProgressIndicator.isVisible = false
        
        // Proceed
        footerBinding.positiveButton.apply {
            text = getString(R.string.proceed)
            setOnClickListener {
                dismiss()
                submitConfirmClickListener.onSubmitConfirmed()
            }
        }
        
        // Cancel
        footerBinding.negativeButton.setOnClickListener {
            dismiss()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}