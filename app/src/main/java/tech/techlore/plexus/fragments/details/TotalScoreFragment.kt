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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.AppDetailsActivity
import tech.techlore.plexus.databinding.FragmentTotalScoreBinding
import tech.techlore.plexus.models.ratingrange.RatingRange

class TotalScoreFragment : Fragment() {
    
    private var _binding: FragmentTotalScoreBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = FragmentTotalScoreBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        lifecycleScope.launch(Dispatchers.Default) {
            // Perform calculations in default dispatcher
            // which is optimized for CPU intensive tasks
            val detailsActivity = requireActivity() as AppDetailsActivity
            val totalDgRatings = detailsActivity.app.totalDgRatings
            val totalMgRatings = detailsActivity.app.totalMgRatings
            
            // Only perform calculations if it was not done already
            // This will prevent calculations
            // everytime user switches from user ratings fragment to this one
            if (!detailsActivity.totalScoreCalculated) {
    
                val ratingRanges = listOf(RatingRange("gold", 4.0f, 4.0f),
                                          RatingRange("silver", 3.0f, 3.9f),
                                          RatingRange("bronze", 2.0f, 2.9f),
                                          RatingRange("broken", 1.0f, 1.9f))
    
                val ratingCounts = mutableMapOf<Pair<String?, String>, Int>()
                for (rating in detailsActivity.ratingsList) {
                    for (range in ratingRanges) {
                        if (rating.ratingScore!!.ratingScore >= range.minValue
                            && rating.ratingScore!!.ratingScore <= range.maxValue) {
                            val key = rating.googleLib to range.status
                            ratingCounts[key] = (ratingCounts[key] ?: 0) + 1
                        }
                    }
                }
    
                val dgGoldRatingsCount = ratingCounts["none" to "gold"] ?: 0
                val dgSilverRatingsCount = ratingCounts["none" to "silver"] ?: 0
                val dgBronzeRatingsCount = ratingCounts["none" to "bronze"] ?: 0
                val dgBrokenRatingsCount = ratingCounts["none" to "broken"] ?: 0
                val mgGoldRatingsCount = ratingCounts["micro_g" to "gold"] ?: 0
                val mgSilverRatingsCount = ratingCounts["micro_g" to "silver"] ?: 0
                val mgBronzeRatingsCount = ratingCounts["micro_g" to "bronze"] ?: 0
                val mgBrokenRatingsCount = ratingCounts["micro_g" to "broken"] ?: 0
    
                detailsActivity.dgGoldRatingsPercent = calcPercent(dgGoldRatingsCount, totalDgRatings)
                detailsActivity.dgSilverRatingsPercent = calcPercent(dgSilverRatingsCount, totalDgRatings)
                detailsActivity.dgBronzeRatingsPercent = calcPercent(dgBronzeRatingsCount, totalDgRatings)
                detailsActivity.dgBrokenRatingsPercent = calcPercent(dgBrokenRatingsCount, totalDgRatings)
                detailsActivity.mgGoldRatingsPercent = calcPercent(mgGoldRatingsCount, totalMgRatings)
                detailsActivity.mgSilverRatingsPercent = calcPercent(mgSilverRatingsCount, totalMgRatings)
                detailsActivity.mgBronzeRatingsPercent = calcPercent(mgBronzeRatingsCount, totalMgRatings)
                detailsActivity.mgBrokenRatingsPercent = calcPercent(mgBrokenRatingsCount, totalMgRatings)
    
                detailsActivity.totalScoreCalculated = true
            }
            
            // Update UI with all results
            withContext(Dispatchers.Main) {
                fragmentBinding.dgAvgScore.text = "${removeDotZeroFromFloat(detailsActivity.app.dgScore)}/4"
                fragmentBinding.dgTotalRatings.text = "${getString(R.string.total_ratings)}: $totalDgRatings"
                setDgProgressAndPercent(detailsActivity.dgGoldRatingsPercent,
                                        detailsActivity.dgSilverRatingsPercent,
                                        detailsActivity.dgBronzeRatingsPercent,
                                        detailsActivity.dgBrokenRatingsPercent)
                
                fragmentBinding.mgAvgScore.text = "${removeDotZeroFromFloat(detailsActivity.app.mgScore)}/4"
                fragmentBinding.mgTotalRatings.text = "${getString(R.string.total_ratings)}: $totalMgRatings"
                setMgProgressAndPercent(detailsActivity.mgGoldRatingsPercent,
                                        detailsActivity.mgSilverRatingsPercent,
                                        detailsActivity.mgBronzeRatingsPercent,
                                        detailsActivity.mgBrokenRatingsPercent)
            }
        }
    }
    
    private fun removeDotZeroFromFloat(avgScore: Float): String {
        return avgScore.toString().removeSuffix(".0")
    }
    
    private fun calcPercent(ratingsCount: Int, totalRatings: Int): Float {
        return if (totalRatings == 0) 0.0f else {
            val result = (ratingsCount.toFloat() / totalRatings.toFloat()) * 100.0f
            ((result * 10.0f).toInt().toFloat()) / 10.0f // Limit result to 1 decimal place without rounding off
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun setDgProgressAndPercent(dgGoldRatingsPercent: Float,
                                        dgSilverRatingsPercent: Float,
                                        dgBronzeRatingsPercent: Float,
                                        dgBrokenRatingsPercent: Float) {
        fragmentBinding.dgGoldProgress.setProgressCompat(dgGoldRatingsPercent.toInt(), true)
        fragmentBinding.dgGoldPercent.text = "${removeDotZeroFromFloat(dgGoldRatingsPercent)}%"
        fragmentBinding.dgSilverProgress.setProgressCompat(dgSilverRatingsPercent.toInt(), true)
        fragmentBinding.dgSilverPercent.text = "${removeDotZeroFromFloat(dgSilverRatingsPercent)}%"
        fragmentBinding.dgBronzeProgress.setProgressCompat(dgBronzeRatingsPercent.toInt(), true)
        fragmentBinding.dgBronzePercent.text = "${removeDotZeroFromFloat(dgBronzeRatingsPercent)}%"
        fragmentBinding.dgBrokenProgress.setProgressCompat(dgBrokenRatingsPercent.toInt(), true)
        fragmentBinding.dgBrokenPercent.text = "${removeDotZeroFromFloat(dgBrokenRatingsPercent)}%"
    }
    
    @SuppressLint("SetTextI18n")
    private fun setMgProgressAndPercent(mgGoldRatingsPercent: Float,
                                        mgSilverRatingsPercent: Float,
                                        mgBronzeRatingsPercent: Float,
                                        mgBrokenRatingsPercent: Float) {
        fragmentBinding.mgGoldProgress.setProgressCompat(mgGoldRatingsPercent.toInt(), true)
        fragmentBinding.mgGoldPercent.text = "${removeDotZeroFromFloat(mgGoldRatingsPercent)}%"
        fragmentBinding.mgSilverProgress.setProgressCompat(mgSilverRatingsPercent.toInt(), true)
        fragmentBinding.mgSilverPercent.text = "${removeDotZeroFromFloat(mgSilverRatingsPercent)}%"
        fragmentBinding.mgBronzeProgress.setProgressCompat(mgBronzeRatingsPercent.toInt(), true)
        fragmentBinding.mgBronzePercent.text = "${removeDotZeroFromFloat(mgBronzeRatingsPercent)}%"
        fragmentBinding.mgBrokenProgress.setProgressCompat(mgBrokenRatingsPercent.toInt(), true)
        fragmentBinding.mgBrokenPercent.text = "${removeDotZeroFromFloat(mgBrokenRatingsPercent)}%"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}