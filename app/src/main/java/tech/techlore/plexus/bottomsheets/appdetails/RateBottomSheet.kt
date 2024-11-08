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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetRateBinding
import tech.techlore.plexus.objects.DeviceState
import tech.techlore.plexus.utils.TextUtils.Companion.hasBlockedWord
import tech.techlore.plexus.utils.TextUtils.Companion.hasEmojis
import tech.techlore.plexus.utils.TextUtils.Companion.hasRepeatedChars
import tech.techlore.plexus.utils.TextUtils.Companion.hasURL

class RateBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetRateBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetRateBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        var job: Job? = null
        val detailsActivity = requireActivity() as AppDetailsActivity
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.isVisible = false
        
        bottomSheetBinding.dgMgText.apply {
            val (statusIcon, statusText) =
                if (DeviceState.isDeviceMicroG) {
                    Pair(ContextCompat.getDrawable(context, R.drawable.ic_microg),
                         "${getString(R.string.microG)} ${getString(R.string.status)}")
                }
                else {
                    Pair(ContextCompat.getDrawable(context, R.drawable.ic_degoogled),
                         "${getString(R.string.de_Googled)} ${getString(R.string.status)}")
                }
            setCompoundDrawablesWithIntrinsicBounds(statusIcon, null, null, null)
            text = statusText
        }
        
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
        
        footerBinding.positiveButton.apply {
            text = getString(R.string.submit)
            setOnClickListener{
                detailsActivity.apply {
                    submitStatusCheckedChipId = bottomSheetBinding.submitStatusChipGroup.checkedChipId
                    submitNotes = bottomSheetBinding.submitNotesText.text.toString()
                    showSubmitBtmSheet()
                }
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