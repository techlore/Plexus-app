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

package tech.techlore.plexus.activities

import android.content.Intent
import androidx.core.view.isVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.utils.UiUtils.Companion.mapInstalledFromChipIdToString
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusChipIdToRatingScore

class MyRatingsDetailsActivity : BaseDetailsActivity() {
    
    var sortedMyRatingsDetailsList = arrayListOf<MyRatingDetails>()
    var fullListSize = -1
    var newListSize = -1
    
    override fun initAdditionalValuesInOnCreate() {
        hideRomSortDropdown = true
    }
    
    override suspend fun setupUiComponents() {
        activityBinding.totalRatingsCount.isVisible = false
        activityBinding.detailsChipGroup.isVisible = false
        activityBinding.loadingIndicator.isVisible = false
        activityBinding.retrievingRatingsText.isVisible = false
        
        navController.navInflater.inflate(R.navigation.details_fragments_nav_graph).apply {
            setStartDestination(R.id.myRatingsDetailsFragment)
            navController.setGraph(this, intent.extras)
        }
        
        activityBinding.detailsNavHost.isVisible = true
        activityBinding.rateBtn.isVisible = false
        fullListSize = myRatingDetailsList?.size ?: -1
        newListSize = fullListSize
    }
    
    override suspend fun retrieveAndDisplayData() {
        myRatingDetailsList?.let { ratingsDetails ->
            withContext(Dispatchers.Default) {
                // Get different app versions & android versions from ratings list
                // and store them in a separate list to show in sort ratings bottom sheet
                differentAppVerList =
                    arrayOf(getString(R.string.any)) +
                    ratingsDetails.map { "${it.version} (${it.buildNumber})" }.distinct()
                
                differentAndroidVerList =
                    arrayOf(getString(R.string.any)) +
                    ratingsDetails.map { it.androidVersion }.distinct()
                
                sortMyRatingsDetails()
            }
            
            activityBinding.detailsSortBtn.isEnabled = true
        }
    }
    
    fun sortMyRatingsDetails() {
        myRatingDetailsList?.let { ratingDetails ->
            sortedMyRatingsDetailsList =
                ArrayList(
                    ratingDetails
                        .filter {
                            val appVerMatches =
                                selectedVersionString == getString(R.string.any)
                                || it.version == selectedVersionString.substringBefore(" (")
                            
                            val androidMatches =
                                selectedAndroidString == getString(R.string.any)
                                || it.androidVersion == selectedAndroidString
                            
                            val installedFromMatches =
                                installedFromChipId == R.id.ratingsChipInstalledAny
                                || it.installedFrom == mapInstalledFromChipIdToString(installedFromChipId)
                            
                            val statusToggleMatches =
                                statusToggleBtnId == R.id.ratingsToggleAnyStatus
                                || it.googleLib == (if (statusToggleBtnId == R.id.ratingsToggleDgStatus) "native" else "micro_g")
                            
                            val statusChipMatches =
                                when {
                                    statusToggleBtnId == R.id.ratingsToggleDgStatus
                                    && dgStatusSortChipId != R.id.ratingsSortAny -> {
                                        it.myRatingScore == mapStatusChipIdToRatingScore(dgStatusSortChipId)
                                    }
                                    
                                    statusToggleBtnId == R.id.ratingsToggleMgStatus
                                    && mgStatusSortChipId != R.id.ratingsSortAny -> {
                                        it.myRatingScore == mapStatusChipIdToRatingScore(
                                            mgStatusSortChipId)
                                    }
                                    
                                    else -> true
                                }
                            
                            appVerMatches && androidMatches && installedFromMatches
                            && statusToggleMatches && statusChipMatches
                            
                        }
                        .sortedByDescending { it.myRatingDateTime }
                )
            
            isListSorted = true
        }
    }
    
    override fun finishAfterTransition() {
        val resultIntent =
            Intent().apply {
                putExtra("isMyRatingCountChanged", newListSize != fullListSize)
                putExtra("myRatingsNewCount", newListSize)
            }
        setResult(RESULT_OK, resultIntent)
        super.finishAfterTransition()
    }
}