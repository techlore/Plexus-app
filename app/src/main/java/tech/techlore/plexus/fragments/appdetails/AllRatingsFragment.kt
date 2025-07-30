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
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.adapters.appdetails.UserRatingsItemAdapter
import tech.techlore.plexus.databinding.FragmentRatingsDetailsBinding
import tech.techlore.plexus.models.get.ratings.Rating

class AllRatingsFragment : Fragment() {
    
    private var _binding: FragmentRatingsDetailsBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var detailsActivity: AppDetailsActivity
    private val displayedRatings = ArrayList<Rating>()
    private lateinit var userRatingsItemAdapter: UserRatingsItemAdapter
    private var fullListSize = 0
    private val pageSize = 5
    private var currentPage = 0
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentRatingsDetailsBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        detailsActivity = requireActivity() as AppDetailsActivity
        
        if (!detailsActivity.isListSorted) detailsActivity.sortRatings()
        
        fullListSize = detailsActivity.sortedRatingsList.size
        
        if (detailsActivity.sortedRatingsList.isEmpty()) {
            fragmentBinding.emptyRatingsListViewStub.inflate()
            val emptyListView: MaterialTextView = fragmentBinding.root.findViewById(R.id.emptyListViewText)
            emptyListView.text = requireContext().getString(R.string.no_ratings_available)
        }
        else {
            userRatingsItemAdapter = UserRatingsItemAdapter(parentFragmentManager)
            fragmentBinding.userRatingsRv.adapter = userRatingsItemAdapter
            loadNextPage()
        }
        
        detailsActivity.activityBinding.nestedScrollView
            .setOnScrollChangeListener { view: NestedScrollView, _, newScrollY, _, oldScrollY ->
                // Show FAB on scroll
                detailsActivity.apply {
                    if (newScrollY == 0) {
                        activityBinding.scrollTopFab.hide()
                        if (isScrolledByFab) {
                            activityBinding.detailsAppBar.setExpanded(true,true)
                            isScrolledByFab = false
                        }
                    }
                    else activityBinding.scrollTopFab.show()
                }
                
                if (newScrollY > oldScrollY) {
                    val child = view.getChildAt(view.childCount - 1)
                    val buffer = 300 // pixels before bottom
                    val thresholdReached = child.bottom - (view.height + view.scrollY)
                    if (thresholdReached <= buffer
                        && displayedRatings.size < fullListSize) {
                        loadNextPage()
                    }
                }
            }
    }
    
    private fun loadNextPage() {
        val start = currentPage * pageSize
        val end = minOf(start + pageSize, fullListSize)
        if (start < end) {
            ArrayList(userRatingsItemAdapter.currentList).apply {
                addAll(detailsActivity.sortedRatingsList.subList(start, end))
                userRatingsItemAdapter.submitList(this)
            }
            currentPage++
        }
    }
    
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}