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

package tech.techlore.plexus.fragments.details

import androidx.recyclerview.widget.ListAdapter
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.adapters.details.UserRatingsItemAdapter
import tech.techlore.plexus.models.get.ratings.Rating

class UserRatingsFragment : BaseRatingsDetailsFragment<Rating>() {
    
    private lateinit var userRatingsItemAdapter: UserRatingsItemAdapter
    
    override fun createAdapter(): ListAdapter<*, *> {
        userRatingsItemAdapter = UserRatingsItemAdapter(parentFragmentManager)
        return userRatingsItemAdapter
    }
    
    override fun sortRatings() {
        (requireActivity() as AppDetailsActivity).sortRatings()
    }
    
    override fun getSortedList(): ArrayList<Rating> {
        return (requireActivity() as AppDetailsActivity).sortedRatingsList
    }
    
    override fun getCurrentList(): List<Rating> {
        return userRatingsItemAdapter.currentList
    }
    
    override fun submitList(list: ArrayList<Rating>) {
        userRatingsItemAdapter.submitList(list)
    }
}