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
import tech.techlore.plexus.activities.MyRatingsDetailsActivity
import tech.techlore.plexus.adapters.details.MyRatingsDetailsItemAdapter
import tech.techlore.plexus.models.myratings.MyRatingDetails

class MyRatingsDetailsFragment : BaseRatingsDetailsFragment<MyRatingDetails>() {
    
    private lateinit var myRatingsItemAdapter: MyRatingsDetailsItemAdapter
    
    override fun createAdapter(): ListAdapter<*, *> {
        myRatingsItemAdapter = MyRatingsDetailsItemAdapter()
        return myRatingsItemAdapter
    }
    
    override fun sortRatings() {
        (requireActivity() as MyRatingsDetailsActivity).sortMyRatingsDetails()
    }
    
    override fun getSortedList(): ArrayList<MyRatingDetails> {
        return (requireActivity() as MyRatingsDetailsActivity).sortedMyRatingsDetailsList
    }
    
    override fun getCurrentList(): List<MyRatingDetails> {
        return myRatingsItemAdapter.currentList
    }
    
    override fun submitList(list: ArrayList<MyRatingDetails>) {
        myRatingsItemAdapter.submitList(list)
    }
}
