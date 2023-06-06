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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetMoreOptionsBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL
import tech.techlore.plexus.utils.IntentUtils.Companion.share

class MoreOptionsBottomSheet(
    private val nameString: String,
    private val packageNameString: String,
    private val dgStatus: String,
    private val mgStatus: String,
    private val coordinatorLayout: CoordinatorLayout,
    private val anchorView: View
) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetMoreOptionsBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var headerBinding: BottomSheetHeaderBinding
    private lateinit var footerBinding: BottomSheetFooterBinding
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
    
        _binding = BottomSheetMoreOptionsBinding.inflate(inflater, container, false)
        headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    
        val playStoreString = "https://play.google.com/store/apps/details?id=$packageNameString"
        val fdroidString = "https://f-droid.org/packages/$packageNameString/"
        
        headerBinding.bottomSheetTitle.text = nameString
    
        // Play store
        bottomSheetBinding.playStore.setOnClickListener {
            dismiss()
            openURL(requireActivity(), playStoreString, coordinatorLayout, anchorView)
        }
    
        // F-Droid
        bottomSheetBinding.fdroid.setOnClickListener {
            dismiss()
            openURL(requireActivity(), fdroidString, coordinatorLayout, anchorView)
        }
    
        // Share
        bottomSheetBinding.share.setOnClickListener {
            share(requireActivity(),
                  nameString,
                  packageNameString,
                  dgStatus,
                  mgStatus,
                  playStoreString,
                  fdroidString)
        
            dismiss()
        }
        footerBinding.positiveButton.visibility = View.GONE
    
        // Cancel
        footerBinding.negativeButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
