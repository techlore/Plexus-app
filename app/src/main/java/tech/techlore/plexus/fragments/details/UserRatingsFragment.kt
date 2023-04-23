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

package tech.techlore.plexus.fragments.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.adapters.details.UserRatingsItemAdapter
import tech.techlore.plexus.databinding.FragmentUserRatingsBinding
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipToRatingScore

class UserRatingsFragment : Fragment() {
    
    private var _binding: FragmentUserRatingsBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = FragmentUserRatingsBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val detailsActivity = requireActivity() as AppDetailsActivity
        var sortedRatingsList = detailsActivity.ratingsList
        val googleLib: String
        val dgScore: Int
        val mgScore: Int
        
        if (!detailsActivity.selectedVersionString.equals(getString(R.string.any))) {
            sortedRatingsList =
                sortedRatingsList.filter { ratings ->
                    ratings.version == detailsActivity.selectedVersionString
                } as ArrayList<Rating>
        }
        
        if (detailsActivity.statusRadio != R.id.user_ratings_radio_any_status) {
            googleLib = if (detailsActivity.statusRadio == R.id.user_ratings_radio_dg_status) "none" else "micro_g"
            sortedRatingsList =
                sortedRatingsList.filter { ratings ->
                    ratings.googleLib == googleLib
                } as ArrayList<Rating>
            
            if (detailsActivity.dgStatusSort != 0) {
                dgScore = mapStatusChipToRatingScore(detailsActivity.dgStatusSort)
                sortedRatingsList =
                    sortedRatingsList.filter { ratings ->
                        ratings.ratingScore!!.ratingScore == dgScore
                    } as ArrayList<Rating>
            }
            else if (detailsActivity.mgStatusSort != 0) {
                mgScore = mapStatusChipToRatingScore(detailsActivity.mgStatusSort)
                sortedRatingsList =
                    sortedRatingsList.filter { ratings ->
                        ratings.ratingScore!!.ratingScore == mgScore
                    } as ArrayList<Rating>
            }
        }
        
        if (sortedRatingsList.isEmpty()) {
            fragmentBinding.emptyRatingsListViewStub.inflate()
            val emptyListView: MaterialTextView = fragmentBinding.root.findViewById(R.id.empty_list_view_text)
            emptyListView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            emptyListView.text = requireContext().getString(R.string.no_ratings_found)
        }
        else {
            val userRatingsItemAdapter = UserRatingsItemAdapter(sortedRatingsList)
            fragmentBinding.userRatingsRv.adapter = userRatingsItemAdapter
        }
        
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}