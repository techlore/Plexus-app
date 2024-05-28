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

package tech.techlore.plexus.fragments.myratingsdetails

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
import tech.techlore.plexus.activities.MyRatingsDetailsActivity
import tech.techlore.plexus.adapters.myratingsdetails.MyRatingsDetailsItemAdapter
import tech.techlore.plexus.databinding.FragmentRatingsDetailsBinding
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.utils.UiUtils
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore

class MyRatingsDetailsFragment : Fragment() {
    
    private var _binding: FragmentRatingsDetailsBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = FragmentRatingsDetailsBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val detailsActivity = requireActivity() as MyRatingsDetailsActivity
        
        lifecycleScope.launch(Dispatchers.Default) {
            // Perform sorting in default dispatcher
            // which is optimized for CPU intensive tasks
            var sortedMyRatingsList = detailsActivity.myRating.ratingsDetails
            val googleLib: String
            val dgScore: Int
            val mgScore: Int
            
            // Get different versions, ROMs & androids from ratings list
            // and store them in a separate list
            if (detailsActivity.differentVersionsList.isEmpty()){
                val uniqueVersions = HashSet<String>()
                sortedMyRatingsList.forEach { ratings ->
                    uniqueVersions.add("${ratings.version} (${ratings.buildNumber})")
                }
                detailsActivity.differentVersionsList = listOf(getString(R.string.any)) + uniqueVersions.toList()
            }
            
            if (detailsActivity.differentRomsList.isEmpty()){
                val uniqueRoms = HashSet<String>()
                sortedMyRatingsList.forEach { ratings ->
                    uniqueRoms.add(ratings.romName)
                }
                detailsActivity.differentRomsList = listOf(getString(R.string.any)) + uniqueRoms.toList()
            }
            
            if (detailsActivity.differentAndroidsList.isEmpty()){
                val uniqueAndroids = HashSet<String>()
                sortedMyRatingsList.forEach { ratings ->
                    uniqueAndroids.add(ratings.androidVersion)
                }
                detailsActivity.differentAndroidsList = listOf(getString(R.string.any)) + uniqueAndroids.toList()
            }
            
            // Version sort
            if (detailsActivity.selectedVersionString != getString(R.string.any)) {
                sortedMyRatingsList =
                    sortedMyRatingsList.filter { ratings ->
                        ratings.version == detailsActivity.selectedVersionString.substringBefore(" (")
                    } as ArrayList<MyRatingDetails>
            }
            
            // ROM sort
            if (detailsActivity.selectedRomString != getString(R.string.any)) {
                sortedMyRatingsList =
                    sortedMyRatingsList.filter { ratings ->
                        ratings.romName == detailsActivity.selectedRomString
                    } as ArrayList<MyRatingDetails>
            }
            
            // Android sort
            if (detailsActivity.selectedAndroidString != getString(R.string.any)) {
                sortedMyRatingsList =
                    sortedMyRatingsList.filter { ratings ->
                        ratings.androidVersion == detailsActivity.selectedAndroidString
                    } as ArrayList<MyRatingDetails>
            }
            
            // Installed from sort
            if (detailsActivity.installedFromChip != R.id.ratingsChipInstalledAny) {
                sortedMyRatingsList =
                    sortedMyRatingsList.filter { ratings ->
                        ratings.installedFrom == UiUtils.mapInstalledFromChipIdToString(
                            detailsActivity.installedFromChip)
                    } as ArrayList<MyRatingDetails>
            }
            
            // Status sort
            if (detailsActivity.statusRadio != R.id.ratingsRadioAnyStatus) {
                googleLib = if (detailsActivity.statusRadio == R.id.ratingsRadioDgStatus) "native" else "micro_g"
                sortedMyRatingsList =
                    sortedMyRatingsList.filter { ratings ->
                        ratings.googleLib == googleLib
                    } as ArrayList<MyRatingDetails>
                
                if (detailsActivity.statusRadio == R.id.ratingsRadioDgStatus
                    && detailsActivity.dgStatusSort != R.id.ratingsSortAny) {
                    dgScore = mapStatusChipIdToRatingScore(detailsActivity.dgStatusSort)
                    sortedMyRatingsList =
                        sortedMyRatingsList.filter { ratings ->
                            ratings.myRatingScore == dgScore
                        } as ArrayList<MyRatingDetails>
                }
                else if (detailsActivity.statusRadio == R.id.ratingsRadioMgStatus
                         && detailsActivity.mgStatusSort != R.id.ratingsSortAny) {
                    mgScore = mapStatusChipIdToRatingScore(detailsActivity.mgStatusSort)
                    sortedMyRatingsList =
                        sortedMyRatingsList.filter { ratings ->
                            ratings.myRatingScore == mgScore
                        } as ArrayList<MyRatingDetails>
                }
            }
            
            // Update UI with all results
            withContext(Dispatchers.Main) {
                if (sortedMyRatingsList.isEmpty()) {
                    fragmentBinding.emptyRatingsListViewStub.inflate()
                    val emptyListView: MaterialTextView = fragmentBinding.root.findViewById(R.id.emptyListViewText)
                    emptyListView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    emptyListView.text = requireContext().getString(R.string.no_ratings_available)
                }
                else {
                    val myRatingsItemAdapter = MyRatingsDetailsItemAdapter(sortedMyRatingsList)
                    fragmentBinding.userRatingsRv.adapter = myRatingsItemAdapter
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}