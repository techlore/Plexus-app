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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.get
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetHelpBinding
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.IS_FIRST_LAUNCH
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class HelpBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetHelpBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetHelpBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.menu_help)
        
        // Apps submit procedure
        bottomSheetBinding.appsSubmitProcCard.setOnClickListener {
            openURL(requireActivity(), getString(R.string.apps_submit_proc_url))
        }
        
        // FAQ
        bottomSheetBinding.faqsCard.setOnClickListener {
            openURL(requireActivity(), getString(R.string.faqs_url))
        }
        
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).apply {
            positiveButton.isVisible = false
            negativeButton.apply {
                text = getString(R.string.dismiss)
                setOnClickListener {
                    dismiss()
                }
            }
        }
        
    }
    
    override fun onDismiss(dialog: DialogInterface) {
        get<PreferenceManager>().apply {
            if (getBoolean(IS_FIRST_LAUNCH)) {
                setBoolean(IS_FIRST_LAUNCH, false)
                requireActivity().finish()
            }
        }
        super.onDismiss(dialog)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}