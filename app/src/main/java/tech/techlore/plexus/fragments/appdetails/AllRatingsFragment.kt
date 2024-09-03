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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.adapters.appdetails.UserRatingsItemAdapter
import tech.techlore.plexus.databinding.FragmentRatingsDetailsBinding
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.utils.UiUtils.Companion.mapInstalledFromChipIdToString
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore
import tech.techlore.plexus.utils.UiUtils.Companion.scrollToTop

class AllRatingsFragment : Fragment() {
    
    private var _binding: FragmentRatingsDetailsBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var detailsActivity: AppDetailsActivity
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = FragmentRatingsDetailsBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        detailsActivity = requireActivity() as AppDetailsActivity
        
        lifecycleScope.launch(Dispatchers.Default) {
            // Perform sorting in default dispatcher
            // which is optimized for CPU intensive tasks
            detailsActivity.sortedRatingsList = detailsActivity.ratingsList
            val googleLib: String
            val dgScore: Int
            val mgScore: Int
            
            // Get different versions, ROMs & androids from ratings list
            // and store them in a separate list
            if (detailsActivity.differentVersionsList.isEmpty()){
                val uniqueVersions = HashSet<String>()
                detailsActivity.sortedRatingsList.forEach { ratings ->
                    uniqueVersions.add("${ratings.version} (${ratings.buildNumber})")
                }
                detailsActivity.differentVersionsList = listOf(getString(R.string.any)) + uniqueVersions.toList()
            }
            
            if (detailsActivity.differentRomsList.isEmpty()){
                val uniqueRoms = HashSet<String>()
                detailsActivity.sortedRatingsList.forEach { ratings ->
                    uniqueRoms.add(ratings.romName)
                }
                detailsActivity.differentRomsList = listOf(getString(R.string.any)) + uniqueRoms.toList()
            }
            
            if (detailsActivity.differentAndroidsList.isEmpty()){
                val uniqueAndroids = HashSet<String>()
                detailsActivity.sortedRatingsList.forEach { ratings ->
                    uniqueAndroids.add(ratings.androidVersion)
                }
                detailsActivity.differentAndroidsList = listOf(getString(R.string.any)) + uniqueAndroids.toList()
            }
            
            // Only perform sorting if it was not done already
            // This will prevent sorting
            // everytime user switches from total score fragment to this one
            if (!detailsActivity.listIsSorted) {
                
                // Version sort
                if (detailsActivity.selectedVersionString != getString(R.string.any)) {
                    detailsActivity.sortedRatingsList =
                        detailsActivity.sortedRatingsList.filter { ratings ->
                            ratings.version == detailsActivity.selectedVersionString.substringBefore(" (")
                        } as ArrayList<Rating>
                }
                
                // ROM sort
                if (detailsActivity.selectedRomString != getString(R.string.any)) {
                    detailsActivity.sortedRatingsList =
                        detailsActivity.sortedRatingsList.filter { ratings ->
                            ratings.romName == detailsActivity.selectedRomString
                        } as ArrayList<Rating>
                }
                
                // Android sort
                if (detailsActivity.selectedAndroidString != getString(R.string.any)) {
                    detailsActivity.sortedRatingsList =
                        detailsActivity.sortedRatingsList.filter { ratings ->
                            ratings.androidVersion == detailsActivity.selectedAndroidString
                        } as ArrayList<Rating>
                }
                
                // Installed from sort
                if (detailsActivity.installedFromChip != R.id.ratingsChipInstalledAny) {
                    detailsActivity.sortedRatingsList =
                        detailsActivity.sortedRatingsList.filter { ratings ->
                            ratings.installedFrom == mapInstalledFromChipIdToString(detailsActivity.installedFromChip)
                        } as ArrayList<Rating>
                }
                
                // Status sort
                if (detailsActivity.statusToggleBtn != R.id.ratingsToggleAnyStatus) {
                    googleLib = if (detailsActivity.statusToggleBtn == R.id.ratingsToggleDgStatus) "native" else "micro_g"
                    detailsActivity.sortedRatingsList =
                        detailsActivity.sortedRatingsList.filter { ratings ->
                            ratings.ratingType == googleLib
                        } as ArrayList<Rating>
                    
                    if (detailsActivity.statusToggleBtn == R.id.ratingsToggleDgStatus
                        && detailsActivity.dgStatusSort != R.id.ratingsSortAny) {
                        dgScore = mapStatusChipIdToRatingScore(detailsActivity.dgStatusSort)
                        detailsActivity.sortedRatingsList =
                            detailsActivity.sortedRatingsList.filter { ratings ->
                                ratings.ratingScore!!.ratingScore == dgScore
                            } as ArrayList<Rating>
                    }
                    else if (detailsActivity.statusToggleBtn == R.id.ratingsToggleMgStatus
                             && detailsActivity.mgStatusSort != R.id.ratingsSortAny) {
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
                    val emptyListView: MaterialTextView = fragmentBinding.root.findViewById(R.id.emptyListViewText)
                    emptyListView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    emptyListView.text = requireContext().getString(R.string.no_ratings_available)
                }
                else {
                    val userRatingsItemAdapter = UserRatingsItemAdapter(detailsActivity.sortedRatingsList)
                    fragmentBinding.userRatingsRv.adapter = userRatingsItemAdapter
                }
            }
        }
        
        // Show FAB on scroll
        detailsActivity.activityBinding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY == 0) {
                detailsActivity.activityBinding.scrollTopFab.hide()
            }
            else detailsActivity.activityBinding.scrollTopFab.show()
        }
        
        // Scroll to top FAB
        detailsActivity.activityBinding.scrollTopFab.setOnClickListener {
            scrollToTop(detailsActivity.activityBinding.nestedScrollView)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        detailsActivity.activityBinding.scrollTopFab.apply {
            if (isVisible) {
                hide()
            }
        }
        _binding = null
    }
    
}