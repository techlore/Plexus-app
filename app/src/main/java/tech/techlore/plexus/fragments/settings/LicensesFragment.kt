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
    private lateinit var licenseAdapter: LicenseItemAdapter
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
                        "https://github.com/techlore/Plexus-app/blob/main/LICENSE"))
    
            // Jackson
            add(License(getString(R.string.jackson),
                        getString(R.string.apache_2_0_license),
                        "https://github.com/FasterXML/jackson-module-kotlin/blob/2.15/LICENSE"))
    
            // Retrofit
            add(License(getString(R.string.retrofit),
                        "${getString(R.string.copyright_square)}\n\n${getString(R.string.apache_2_0_license)}",
                        "https://github.com/square/retrofit/blob/master/LICENSE.txt"))
    
            // FastScroll
            add(License(getString(R.string.fastscroll),
                        getString(R.string.apache_2_0_license),
                        "https://github.com/zhanghai/AndroidFastScroll/blob/master/LICENSE"))
    
            
            // Material Design Icons
            add(License(getString(R.string.md_icons),
                        getString(R.string.apache_2_0_license),
                        "https://github.com/Templarian/MaterialDesign/blob/master/LICENSE"))
        }
        
        licenseAdapter = LicenseItemAdapter(licenseList)
        fragmentBinding.licensesRecyclerView.adapter = licenseAdapter
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}