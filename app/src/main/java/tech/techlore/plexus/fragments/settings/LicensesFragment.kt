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
import tech.techlore.plexus.databinding.FragmentLicensesBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class LicensesFragment : Fragment() {
    
    private var _binding: FragmentLicensesBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var settingsActivity: SettingsActivity
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentLicensesBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        settingsActivity = requireActivity() as SettingsActivity
        settingsActivity.activityBinding.toolbarBottom.title = getString(R.string.licenses)
        
        // Plexus
        fragmentBinding.plexus.setOnClickListener {
            openURL(requireActivity(),
                    "https://github.com/techlore/Plexus-app/blob/main/LICENSE",
                    settingsActivity.activityBinding.settingsCoordLayout,
                    settingsActivity.activityBinding.toolbarBottom)
        }
        
        // Jackson
        fragmentBinding.retrofit.setOnClickListener {
            openURL(requireActivity(),
                    "https://github.com/square/retrofit/blob/master/LICENSE.txt",
                    settingsActivity.activityBinding.settingsCoordLayout,
                    settingsActivity.activityBinding.toolbarBottom)
        }
        
        // Plexus
        fragmentBinding.fastscroll.setOnClickListener {
            openURL(requireActivity(),
                    "https://github.com/zhanghai/AndroidFastScroll/blob/master/LICENSE",
                    settingsActivity.activityBinding.settingsCoordLayout,
                    settingsActivity.activityBinding.toolbarBottom)
        }
        
        // Material Design Icons
        fragmentBinding.mdIcons.setOnClickListener {
            openURL(requireActivity(),
                    "https://github.com/Templarian/MaterialDesign/blob/master/LICENSE",
                    settingsActivity.activityBinding.settingsCoordLayout,
                    settingsActivity.activityBinding.toolbarBottom)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        settingsActivity.activityBinding.toolbarBottom.title = getString(R.string.about)
        _binding = null
    }
}