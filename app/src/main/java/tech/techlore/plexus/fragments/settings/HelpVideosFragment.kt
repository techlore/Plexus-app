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
import tech.techlore.plexus.databinding.FragmentHelpVideosBinding
import tech.techlore.plexus.fragments.bottomsheets.VideoPlayerBottomSheet

class HelpVideosFragment : Fragment() {
    
    private var _binding: FragmentHelpVideosBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentHelpVideosBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        (requireActivity() as SettingsActivity).activityBinding.toolbarBottom.title = getString(R.string.menu_help)
        
        val viewToVideoIdMap =
            mapOf(fragmentBinding.introductionThumbnail to R.raw.intro_video,
                  fragmentBinding.introductionText to R.raw.intro_video,
                  fragmentBinding.navigationThumbnail to R.raw.navigation_video,
                  fragmentBinding.navigationText to R.raw.navigation_video,
                  fragmentBinding.submissionsThumbnail to R.raw.submissions_video,
                  fragmentBinding.submissionsText to R.raw.submissions_video,
                  fragmentBinding.aboutThumbnail to R.raw.about_video,
                  fragmentBinding.aboutText to R.raw.about_video
            )
        
        viewToVideoIdMap.forEach { (view, videoId) ->
            view.setOnClickListener {
                VideoPlayerBottomSheet(videoId).show(parentFragmentManager, "VideoPlayerBottomSheet")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}