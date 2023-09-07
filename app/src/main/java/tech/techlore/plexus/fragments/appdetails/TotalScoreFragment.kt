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

package tech.techlore.plexus.fragments.appdetails

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
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToColor

class TotalScoreFragment : Fragment() {
    
    private var _binding: FragmentTotalScoreBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var detailsActivity: AppDetailsActivity
    
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
    
        detailsActivity = requireActivity() as AppDetailsActivity
        val totalDgRatings = detailsActivity.app.totalDgRatings
        val totalMgRatings = detailsActivity.app.totalMgRatings
        
        lifecycleScope.launch(Dispatchers.Default) {
            // Perform calculations in default dispatcher
            // which is optimized for CPU intensive tasks
            
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
                            val key = rating.ratingType to range.status
                            ratingCounts[key] = (ratingCounts[key] ?: 0) + 1
                        }
                    }
                }
    
                val dgGoldRatingsCount = ratingCounts["native" to "gold"] ?: 0
                val dgSilverRatingsCount = ratingCounts["native" to "silver"] ?: 0
                val dgBronzeRatingsCount = ratingCounts["native" to "bronze"] ?: 0
                val dgBrokenRatingsCount = ratingCounts["native" to "broken"] ?: 0
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
        fragmentBinding.dgCircle.apply {
            setIndicatorColor(mapScoreRangeToColor(requireContext(), detailsActivity.app.dgScore))
            setProgressCompat(100, true)
        }
        
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
        // No need to animate progress indicators here
        // as they won't be shown unless scrolled
        fragmentBinding.mgCircle.apply {
            setIndicatorColor(mapScoreRangeToColor(requireContext(), detailsActivity.app.mgScore))
            fragmentBinding.mgCircle.progress = 100
        }
        fragmentBinding.mgGoldProgress.progress = mgGoldRatingsPercent.toInt()
        fragmentBinding.mgGoldPercent.text = "${removeDotZeroFromFloat(mgGoldRatingsPercent)}%"
        fragmentBinding.mgSilverProgress.progress = mgSilverRatingsPercent.toInt()
        fragmentBinding.mgSilverPercent.text = "${removeDotZeroFromFloat(mgSilverRatingsPercent)}%"
        fragmentBinding.mgBronzeProgress.progress = mgBronzeRatingsPercent.toInt()
        fragmentBinding.mgBronzePercent.text = "${removeDotZeroFromFloat(mgBronzeRatingsPercent)}%"
        fragmentBinding.mgBrokenProgress.progress = mgBrokenRatingsPercent.toInt()
        fragmentBinding.mgBrokenPercent.text = "${removeDotZeroFromFloat(mgBrokenRatingsPercent)}%"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}