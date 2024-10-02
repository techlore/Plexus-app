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

package tech.techlore.plexus.fragments.bottomsheets.submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetStartSubmitBinding
import tech.techlore.plexus.utils.TextUtils.Companion.hasBlockedWord
import tech.techlore.plexus.utils.TextUtils.Companion.hasEmojis
import tech.techlore.plexus.utils.TextUtils.Companion.hasRepeatedChars
import tech.techlore.plexus.utils.TextUtils.Companion.hasURL

class StartSubmitBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetStartSubmitBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetStartSubmitBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        var job: Job? = null
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.isVisible = false
        
        // Chip group
        bottomSheetBinding.submitStatusChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            footerBinding.positiveButton.isEnabled = checkedIds.isNotEmpty()
            bottomSheetBinding.submitNotesBox.isEnabled = checkedIds.isNotEmpty()
        }
        
        // Notes
        val maxTextLength: Int
        bottomSheetBinding.submitNotesBox.apply {
            hint = "${getString(R.string.notes)} (${getString(R.string.optional)})"
            maxTextLength = counterMaxLength
        }
        bottomSheetBinding.submitNotesText.doOnTextChanged { charSequence, _, _, _ ->
            job?.cancel()
            job = lifecycleScope.launch {
                delay(300)
                footerBinding.positiveButton.isEnabled =
                    charSequence!!.isEmpty()
                    || (charSequence.length in 5..maxTextLength
                        && !hasBlockedWord(requireContext(), charSequence)
                        && !hasRepeatedChars(charSequence)
                        && !hasEmojis(charSequence)
                        && !hasURL(charSequence))
            }
        }
        
        footerBinding.negativeButton.setOnClickListener {
                ConfirmSubmitBottomSheet().show(parentFragmentManager, "ConfirmSubmitBottomSheet")
            }
        }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}