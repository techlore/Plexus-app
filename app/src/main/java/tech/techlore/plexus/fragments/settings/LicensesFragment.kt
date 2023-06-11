/*
 * Copyright (c) 2022 Techlore
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

package tech.techlore.plexus.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SettingsActivity
import tech.techlore.plexus.adapters.settings.LicenseItemAdapter
import tech.techlore.plexus.databinding.FragmentLicensesBinding
import tech.techlore.plexus.models.license.License

class LicensesFragment : Fragment() {
    
    private var _binding: FragmentLicensesBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var licenseList: ArrayList<License>
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentLicensesBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        (requireActivity() as SettingsActivity).activityBinding.toolbarBottom.title = getString(R.string.licenses)
        
        licenseList = ArrayList<License>().apply {
            
            // Plexus
            add(License(getString(R.string.plexus),
                        "${getString(R.string.copyright_techlore)}\n\n${getString(R.string.gpl_3_0_license)}",
                        getString(R.string.plexus_license_url)))
    
            // Jackson
            add(License(getString(R.string.jackson),
                        getString(R.string.apache_2_0_license),
                        getString(R.string.jackson_license_url)))
    
            // Retrofit
            add(License(getString(R.string.retrofit),
                        "${getString(R.string.copyright_square)}\n\n${getString(R.string.apache_2_0_license)}",
                        getString(R.string.retrofit_license_url)))
            
            // Glide
            add(License(getString(R.string.glide),
                        getString(R.string.glide_license,
                                  getString(R.string.apache_2_0_license),
                                  getString(R.string.mit_license)),
                        getString(R.string.glide_license_url)))
    
            // FastScroll
            add(License(getString(R.string.fastscroll),
                        getString(R.string.apache_2_0_license),
                        getString(R.string.fastscroll_license_url)))
            
            // jsoup
            add(License(getString(R.string.jsoup),
                        "${getString(R.string.copyright_jsoup)}\n\n${getString(R.string.mit_license)}",
                        getString(R.string.jsoup_license_url)))
            
            // Material Design Icons
            add(License(getString(R.string.md_icons),
                        getString(R.string.apache_2_0_license),
                        getString(R.string.md_icons_license_url)))
    
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
        
        fragmentBinding.licensesRecyclerView.adapter = LicenseItemAdapter(licenseList)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}