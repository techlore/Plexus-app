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

package tech.techlore.plexus.fragments.appdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import tech.techlore.plexus.databinding.FragmentProgressViewBinding

class FragmentProgressView : Fragment() {
    
    private var _binding: FragmentProgressViewBinding ? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentProgressViewBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding.progressIndicatorAnim.apply {
            pauseAnimation()
            isVisible = false
        }
        fragmentBinding.retrievingRatingsText.isVisible = false
        _binding = null
    }
}