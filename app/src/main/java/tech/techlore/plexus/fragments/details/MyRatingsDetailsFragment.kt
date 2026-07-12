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

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MyRatingsDetailsActivity
import tech.techlore.plexus.adapters.details.MyRatingsDetailsItemAdapter
import tech.techlore.plexus.bottomsheets.myratingsdetails.DeleteRatingBottomSheet
import tech.techlore.plexus.interfaces.details.DeleteBtnClickListener
import tech.techlore.plexus.interfaces.details.RatingDetailDeleteListener
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.objects.DataState
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class MyRatingsDetailsFragment :
    BaseRatingsDetailsFragment<MyRatingDetails>(),
    DeleteBtnClickListener,
    RatingDetailDeleteListener {
    
    private lateinit var myRatingsItemAdapter: MyRatingsDetailsItemAdapter
    private val myRatingsDetailsActivity by lazy { requireActivity() as MyRatingsDetailsActivity }
    private var delClickedPos = 0
    
    override fun createAdapter(): ListAdapter<*, *> {
        myRatingsItemAdapter = MyRatingsDetailsItemAdapter(this)
        return myRatingsItemAdapter
    }
    
    override fun sortRatings() {
        myRatingsDetailsActivity.sortMyRatingsDetails()
    }
    
    override fun getSortedList(): ArrayList<MyRatingDetails> {
        return myRatingsDetailsActivity.sortedMyRatingsDetailsList
    }
    
    override fun getCurrentList(): List<MyRatingDetails> {
        return myRatingsItemAdapter.currentList
    }
    
    override fun submitList(list: ArrayList<MyRatingDetails>) {
        myRatingsItemAdapter.submitList(list)
    }
    
    override fun onDeleteClicked(position: Int, ratingId: String, encTokenBase64: String?) {
        delClickedPos = position
        DeleteRatingBottomSheet(
            ratingId = ratingId,
            encTokenBase64 = encTokenBase64,
            packageName = packageNameString,
            ratingDetailDeleteListener = this
        ).show(parentFragmentManager, "DeleteRatingBottomSheet")
    }
    
    override fun onRatingDetailDeleted(deletedRatingId: String) {
        lifecycleScope.launch {
            myRatingsItemAdapter.submitList(
                myRatingsDetailsActivity.sortedMyRatingsDetailsList.apply {
                    removeAt(delClickedPos)
                }
            )
            withContext(Dispatchers.Default) {
                myRatingsDetailsActivity.myRatingDetailsList!!.removeIf {
                    it.id == deletedRatingId
                }
            }
            fullListSize -= 1
            DataState.apply {
                isMyRatingsCountChanged = true
                myRatingsNewCount = fullListSize
            }
        }
        
        showSnackbar(
            coordinatorLayout = myRatingsDetailsActivity.activityBinding.detailsCoordLayout,
            message = getString(R.string.deleted_rating_successfully),
            anchorView =
                (myRatingsDetailsActivity.activityBinding.scrollTopFab).let {
                    if (it.isVisible) it
                    else myRatingsDetailsActivity.activityBinding.detailsFloatingToolbar
                }
        )
    }
}
