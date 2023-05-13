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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.adapters.details.UserRatingsItemAdapter
import tech.techlore.plexus.databinding.FragmentUserRatingsBinding
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore

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
        
        lifecycleScope.launch(Dispatchers.Default) {
            // Perform sorting in default dispatcher
            // which is optimized for CPU intensive tasks
            val detailsActivity = requireActivity() as AppDetailsActivity
            detailsActivity.sortedRatingsList = detailsActivity.ratingsList
            val googleLib: String
            val dgScore: Int
            val mgScore: Int
    
            // Get different versions from ratings list
            // and store them in a separate list
            if (detailsActivity.differentVersionsList.isEmpty()){
                val uniqueVersions = HashSet<String>()
                detailsActivity.sortedRatingsList.forEach { ratings ->
                    uniqueVersions.add("${ratings.version!!} (${ratings.buildNumber})")
                }
                detailsActivity.differentVersionsList = listOf(getString(R.string.any)) + uniqueVersions.toList()
            }
    
            // Only perform sorting if it was not done already
            // This will prevent sorting
            // everytime user switches from total score fragment to this one
            if (!detailsActivity.listIsSorted) {
                if (!detailsActivity.selectedVersionString.equals(getString(R.string.any))) {
                    detailsActivity.sortedRatingsList =
                        detailsActivity.sortedRatingsList.filter { ratings ->
                            ratings.version == detailsActivity.selectedVersionString
                        } as ArrayList<Rating>
                }
    
                if (detailsActivity.statusRadio != R.id.user_ratings_radio_any_status) {
                    googleLib = if (detailsActivity.statusRadio == R.id.user_ratings_radio_dg_status) "none" else "micro_g"
                    detailsActivity.sortedRatingsList =
                        detailsActivity.sortedRatingsList.filter { ratings ->
                            ratings.googleLib == googleLib
                        } as ArrayList<Rating>
        
                    if (detailsActivity.statusRadio == R.id.user_ratings_radio_dg_status
                        && detailsActivity.dgStatusSort != R.id.user_ratings_sort_any) {
                        dgScore = mapStatusChipIdToRatingScore(detailsActivity.dgStatusSort)
                        detailsActivity.sortedRatingsList =
                            detailsActivity.sortedRatingsList.filter { ratings ->
                                ratings.ratingScore!!.ratingScore == dgScore
                            } as ArrayList<Rating>
                    }
                    else if (detailsActivity.statusRadio == R.id.user_ratings_radio_mg_status
                             && detailsActivity.mgStatusSort != R.id.user_ratings_sort_any) {
                        mgScore = mapStatusChipIdToRatingScore(detailsActivity.mgStatusSort)
                        detailsActivity.sortedRatingsList =
                            detailsActivity.sortedRatingsList.filter { ratings ->
                                ratings.ratingScore!!.ratingScore == mgScore
                            } as ArrayList<Rating>
                    }
                }
                
                detailsActivity.listIsSorted = true
            }
    
            // Update UI with all results
            withContext(Dispatchers.Main) {
                if (detailsActivity.sortedRatingsList.isEmpty()) {
                    fragmentBinding.emptyRatingsListViewStub.inflate()
                    val emptyListView: MaterialTextView = fragmentBinding.root.findViewById(R.id.empty_list_view_text)
                    emptyListView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    emptyListView.text = requireContext().getString(R.string.no_ratings_available)
                }
                else {
                    val userRatingsItemAdapter = UserRatingsItemAdapter(detailsActivity.sortedRatingsList)
                    fragmentBinding.userRatingsRv.adapter = userRatingsItemAdapter
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}