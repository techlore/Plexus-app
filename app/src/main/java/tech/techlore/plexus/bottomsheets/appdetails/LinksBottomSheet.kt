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
    private val nameString: String,
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
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = nameString
        
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
        
        // VPN Toolkit
        bottomSheetBinding.vpnToolkit.apply {
            if (nameString.contains("VPN", ignoreCase = true)
                || packageNameString.contains("VPN", ignoreCase = true)) {
                visibility = View.VISIBLE
                setOnClickListener {
                    dismiss()
                    requireActivity().openURL(getString(R.string.vpn_toolkit_url))
                }
            }
        }
        
        // Share
        // Temporarily disable share
        /*bottomSheetBinding.share.setOnClickListener {
            dismiss()
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT,
                              """
                              ${getString(R.string.app)}: $nameString
                              ${getString(R.string.package_name)}: $packageNameString
                              ${getString(R.string.de_Googled)}: $dgStatus
                              ${getString(R.string.microG)}: $mgStatus
                              ${getString(R.string.google_play_store)}: $playStoreString
                              ${getString(R.string.fdroid)}: $fdroidString
                              ${getString(R.string.exodus)}: $exodusString
                              """.trimIndent()), getString(R.string.menu_share)))
        }*/
        
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
