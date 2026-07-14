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
import tech.techlore.plexus.utils.UiUtils.Companion.scrollToTop
import tech.techlore.plexus.utils.UiUtils.Companion.showSnackbar

class MyRatingsDetailsFragment :
    BaseRatingsDetailsFragment<MyRatingDetails>(),
    DeleteBtnClickListener,
    RatingDetailDeleteListener {
    
    private lateinit var myRatingsItemAdapter: MyRatingsDetailsItemAdapter
    private val myRatingsDetailsActivity by lazy { requireActivity() as MyRatingsDetailsActivity }
    
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
    
    override fun onDeleteClicked(ratingId: String, encTokenBase64: String?) {
        DeleteRatingBottomSheet(
            ratingId = ratingId,
            encTokenBase64 = encTokenBase64,
            packageName = packageNameString,
            ratingDetailDeleteListener = this
        ).show(parentFragmentManager, "DeleteRatingBottomSheet")
    }
    
    override fun onRatingDetailDeleted(deletedRatingId: String) {
        myRatingsDetailsActivity.newListSize -= 1
        sortedListSize -= 1
        
        if (myRatingsDetailsActivity.newListSize == 0) {
            myRatingsDetailsActivity.finishAfterTransition()
        }
        else {
            lifecycleScope.launch {
                // We could do removeAt(clickedPosition),
                // but when I tried, sometimes it had issues
                // like item not getting removed from UI.
                // Some other times it worked fine.
                withContext(Dispatchers.Default) {
                    ArrayList<MyRatingDetails>(
                        myRatingsDetailsActivity.sortedMyRatingsDetailsList
                            .filterNot { it.id == deletedRatingId }
                    )
                }.let {
                    submitList(it)
                    myRatingsDetailsActivity.sortedMyRatingsDetailsList = it
                }
                
                withContext(Dispatchers.Default) {
                    myRatingsDetailsActivity.myRatingDetailsList!!.removeIf {
                        it.id == deletedRatingId
                    }
                }
                
                myRatingsDetailsActivity.apply {
                    // If one item is visible, show floating toolbar forcefully
                    // This will prevent the following situation:
                    // User scrolls > floating toolbar is hidden >
                    // user deletes a few items > only one item remains in list >
                    // now we can't scroll as scrollview is not long enough >
                    // floating toolbar is still hidden
                    if (sortedListSize == 1) {
                        activityBinding.detailsNestedScrollView.scrollToTop()
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
    }
}
