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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SettingsActivity
import tech.techlore.plexus.databinding.FragmentSupportUsBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class SupportUsFragment : Fragment() {
    
    private var _binding: FragmentSupportUsBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
    
        _binding = FragmentSupportUsBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    
        (requireActivity() as SettingsActivity).activityBinding.toolbarBottom.title = getString(R.string.support_us)
        
        // Liberapay
        fragmentBinding.liberapayUrl.setOnClickListener {
            openURL(requireActivity(),
                    getString(R.string.plexus_liberapay_url),
                    fragmentBinding.supportUsCoordLayout,
                    null)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}