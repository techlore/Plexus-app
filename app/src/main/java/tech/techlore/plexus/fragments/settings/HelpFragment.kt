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

package tech.techlore.plexus.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SettingsActivity
import tech.techlore.plexus.databinding.FragmentHelpBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class HelpFragment : Fragment() {
    
    private var _binding: FragmentHelpBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val settingsActivity = requireActivity() as SettingsActivity
        settingsActivity.activityBinding.settingsBottomAppBarTitle.text = getString(R.string.menu_help)
        
        fragmentBinding.appsSubmitProcCard.setOnClickListener {
            openURL(settingsActivity,
                    getString(R.string.apps_submit_proc_url),
                    fragmentBinding.helpCoordLayout,
                    settingsActivity.activityBinding.settingsBottomAppBar)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}