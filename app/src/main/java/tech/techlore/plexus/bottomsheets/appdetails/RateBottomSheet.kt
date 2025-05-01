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

import android.content.DialogInterface
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
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetRateBinding
import tech.techlore.plexus.objects.AppState
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.CONF_BEFORE_SUBMIT
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL
import tech.techlore.plexus.utils.TextUtils.Companion.hasBlockedWord
import tech.techlore.plexus.utils.TextUtils.Companion.hasEmojis
import tech.techlore.plexus.utils.TextUtils.Companion.hasRepeatedChars
import tech.techlore.plexus.utils.TextUtils.Companion.hasURL
import tech.techlore.plexus.utils.UiUtils.Companion.setDgMgTextWithIcon

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
        
        bottomSheetBinding.commonIssuesCard.setOnClickListener {
            requireActivity().openURL(getString(R.string.common_issues_url))
        }
        
        bottomSheetBinding.dgMgText.setDgMgTextWithIcon(requireContext(), appendStatusString = true)
        
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
                        && !charSequence.hasBlockedWord(requireContext())
                        && !charSequence.hasRepeatedChars()
                        && !charSequence.hasEmojis()
                        && !charSequence.hasURL())
            }
        }
        
        footerBinding.positiveButton.apply {
            isEnabled = false
            text = getString(R.string.submit)
            setOnClickListener {
                detailsActivity.apply {
                    submitStatusCheckedChipId = bottomSheetBinding.submitStatusChipGroup.checkedChipId
                    submitNotes = bottomSheetBinding.submitNotesText.text.toString()
                }
                if (get<PreferenceManager>().getBoolean(CONF_BEFORE_SUBMIT, defValue = false)) {
                    ConfirmSubmitBottomSheet().show(parentFragmentManager, "ConfirmSubmitBottomSheet")
                }
                else detailsActivity.showSubmitBottomSheet()
            }
        }
        
        footerBinding.negativeButton.setOnClickListener {
            dismiss()
        }
    }
    
    override fun onDismiss(dialog: DialogInterface) {
        AppState.apply {
            if (isVerificationSuccessful) isVerificationSuccessful = false
        }
        super.onDismiss(dialog)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}