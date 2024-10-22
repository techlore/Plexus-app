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

package tech.techlore.plexus.fragments.bottomsheets.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SettingsActivity
import tech.techlore.plexus.adapters.settings.LicenseItemAdapter
import tech.techlore.plexus.databinding.BottomSheetFooterBinding
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding
import tech.techlore.plexus.databinding.BottomSheetLicensesBinding
import tech.techlore.plexus.models.license.License

class LicensesBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetLicensesBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var licenseList: ArrayList<License>
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetLicensesBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.third_party_licenses)
        
        licenseList = ArrayList<License>().apply {
    
            // Retrofit
            add(License(getString(R.string.retrofit),
                        "${getString(R.string.copyright_square)}\n\n${getString(R.string.apache_2_0_license)}",
                        getString(R.string.retrofit_license_url)))
            
            // Coil
            add(License(getString(R.string.coil),
                        "${getString(R.string.copyright_coil)}\n\n${getString(R.string.apache_2_0_license)}",
                        getString(R.string.coil_license_url)))
    
            // FastScroll
            add(License(getString(R.string.fastscroll),
                        "${getString(R.string.copyright_fastscroll)}\n\n${getString(R.string.apache_2_0_license)}",
                        getString(R.string.fastscroll_license_url)))
            
            // jsoup
            add(License(getString(R.string.jsoup),
                        "${getString(R.string.copyright_jsoup)}\n\n${getString(R.string.mit_license)}",
                        getString(R.string.jsoup_license_url)))
    
            // lottie-android
            add(License(getString(R.string.lottie_android),
                        getString(R.string.apache_2_0_license),
                        getString(R.string.lottie_android_license_url)))
    
            // LottieFiles
            add(License(getString(R.string.lottie_files),
                        getString(R.string.lottie_files_license),
                        getString(R.string.lottie_files_license_url)))
    
            // F-Droid icon
            add(License(getString(R.string.fdroid_icon),
                        getString(R.string.cc_3_0_license),
                        getString(R.string.fdroid_icon_license_url)))
            
            // Exodus icon
            add(License(getString(R.string.exodus_icon),
                        getString(R.string.agpl_3_0_license),
                        getString(R.string.exodus_icon_license_url)))
    
            // microG icon
            add(License(getString(R.string.microG_icon),
                        getString(R.string.apache_2_0_license),
                        getString(R.string.microG_icon_license_url)))
            
            // Liberapay icon
            add(License(getString(R.string.liberapay_icon),
                        getString(R.string.cc0_1_0_universal_public_domain_license),
                        getString(R.string.liberapay_icon_license_url)))
    
            // Monero icon
            add(License(getString(R.string.monero_icon),
                        getString(R.string.cc_3_0_license),
                        getString(R.string.monero_icon_license_url)))
        }
        
        bottomSheetBinding.licensesRecyclerView.adapter = LicenseItemAdapter(licenseList,
                                                                             requireActivity() as SettingsActivity)
        
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).apply {
            positiveButton.isVisible = false
            negativeButton.apply {
                text = getString(R.string.dismiss)
                setOnClickListener { dismiss() }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}