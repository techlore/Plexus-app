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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetLinksBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class LinksBottomSheet(
    private val packageNameString: String
) : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetLinksBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetLinksBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        val playStoreString = "${getString(R.string.google_play_url)}$packageNameString"
        val fdroidString = "${getString(R.string.fdroid_url)}$packageNameString/"
        val exodusString = "${getString(R.string.exodus_url)}$packageNameString/"
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text =
            getString(R.string.menu_links)
        
        // Google Play Store
        bottomSheetBinding.playStore.setOnClickListener {
            dismiss()
            requireActivity().openURL(playStoreString)
        }
        
        // F-Droid
        bottomSheetBinding.fdroid.setOnClickListener {
            dismiss()
            requireActivity().openURL(fdroidString)
        }
        
        // Exodus Privacy
        bottomSheetBinding.exodus.setOnClickListener {
            dismiss()
            requireActivity().openURL(exodusString)
        }
        
        footerBinding.positiveButton.visibility = View.GONE
        
        // Cancel
        footerBinding.negativeButton.apply {
            text = getString(R.string.dismiss)
            setOnClickListener { dismiss() }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
