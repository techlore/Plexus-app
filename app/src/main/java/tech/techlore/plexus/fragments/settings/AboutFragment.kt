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

package tech.techlore.plexus.fragments.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tech.techlore.plexus.BuildConfig
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SettingsActivity
import tech.techlore.plexus.databinding.FragmentAboutBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class AboutFragment : Fragment() {
    
    private var _binding: FragmentAboutBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val settingsActivity = requireActivity() as SettingsActivity
        settingsActivity.activityBinding.toolbarBottom.title = getString(R.string.about)
        
        // Version
        @SuppressLint("SetTextI18n")
        fragmentBinding.aboutVersion.text = "${getString(R.string.version)}: ${BuildConfig.VERSION_NAME}"
        
        
        // Privacy policy
        fragmentBinding.privacyPolicy.setOnClickListener {
                openURL(requireActivity(),
                        getString(R.string.plexus_privacy_policy_url),
                        settingsActivity.activityBinding.settingsCoordLayout,
                        settingsActivity.activityBinding.toolbarBottom)
            }
        
        // Licenses
        fragmentBinding.licenses.setOnClickListener {
                settingsActivity.navController.navigate(R.id.action_aboutFragment_to_licensesFragment)
            }
    
        // Support us
        fragmentBinding.supportUs.setOnClickListener {
            settingsActivity.navController.navigate(R.id.action_aboutFragment_to_supportUsFragment)
        }
        
        // View on GitHub
        fragmentBinding.viewOnGitHub.setOnClickListener {
            openURL(requireActivity(),
                        getString(R.string.plexus_github_url),
                        settingsActivity.activityBinding.settingsCoordLayout,
                        settingsActivity.activityBinding.toolbarBottom)
            }
        
        // Visit Techlore
        fragmentBinding.visitTechlore.setOnClickListener {
            openURL(requireActivity(),
                        getString(R.string.techlore_website_url),
                        settingsActivity.activityBinding.settingsCoordLayout,
                        settingsActivity.activityBinding.toolbarBottom)
            }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}