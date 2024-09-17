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

import android.annotation.SuppressLint
import android.content.Context
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
    private lateinit var detailsActivity: AppDetailsActivity
    private var dgScore = 0.0f
    private var mgScore = 0.0f
    
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
        dgScore = detailsActivity.app.dgScore
        mgScore = detailsActivity.app.mgScore
        val totalDgRatingsCount = detailsActivity.totalDgRatingsCount
        val totalMgRatingsCount = detailsActivity.totalMgRatingsCount
        
        lifecycleScope.launch(Dispatchers.Default) {
            // Perform calculations in default dispatcher
            // which is optimized for CPU intensive tasks
            
            // Only perform calculations if it was not done already
            // This will prevent calculations
            // everytime user switches from user ratings fragment to this one
            if (!detailsActivity.isTotalScoreCalculated) {
                
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
                
                detailsActivity.dgGoldRatingsPercent = calcPercent(dgGoldRatingsCount, totalDgRatingsCount)
                detailsActivity.dgSilverRatingsPercent = calcPercent(dgSilverRatingsCount, totalDgRatingsCount)
                detailsActivity.dgBronzeRatingsPercent = calcPercent(dgBronzeRatingsCount, totalDgRatingsCount)
                detailsActivity.dgBrokenRatingsPercent = calcPercent(dgBrokenRatingsCount, totalDgRatingsCount)
                detailsActivity.mgGoldRatingsPercent = calcPercent(mgGoldRatingsCount, totalMgRatingsCount)
                detailsActivity.mgSilverRatingsPercent = calcPercent(mgSilverRatingsCount, totalMgRatingsCount)
                detailsActivity.mgBronzeRatingsPercent = calcPercent(mgBronzeRatingsCount, totalMgRatingsCount)
                detailsActivity.mgBrokenRatingsPercent = calcPercent(mgBrokenRatingsCount, totalMgRatingsCount)
                
                detailsActivity.isTotalScoreCalculated = true
            }
            
            // Update UI with all results
            withContext(Dispatchers.Main) {
                fragmentBinding.dgAvgScore.text = "${removeDotZeroFromFloat(dgScore)}/4"
                fragmentBinding.totalDgRatingsCount.text = "${getString(R.string.ratings)}: $totalDgRatingsCount"
                setProgressAndPercent(dgScore,
                                      true,
                                      detailsActivity.dgGoldRatingsPercent,
                                      detailsActivity.dgSilverRatingsPercent,
                                      detailsActivity.dgBronzeRatingsPercent,
                                      detailsActivity.dgBrokenRatingsPercent)
                
                fragmentBinding.mgAvgScore.text = "${removeDotZeroFromFloat(mgScore)}/4"
                fragmentBinding.totalMgRatingsCount.text = "${getString(R.string.ratings)}: $totalMgRatingsCount"
                setProgressAndPercent(mgScore,
                                      false,
                                      detailsActivity.mgGoldRatingsPercent,
                                      detailsActivity.mgSilverRatingsPercent,
                                      detailsActivity.mgBronzeRatingsPercent,
                                      detailsActivity.mgBrokenRatingsPercent)
            }
        }
    }
    
    private fun calcPercent(ratingsCount: Int, totalRatings: Int): Float {
        return if (totalRatings == 0 || ratingsCount == 0) 0.0f else {
            val result = (ratingsCount.toFloat() / totalRatings.toFloat()) * 100.0f
            ((result * 10.0f).toInt().toFloat()) / 10.0f // Limit result to 1 decimal place without rounding off
        }
    }
    
    private fun removeDotZeroFromFloat(avgScore: Float): String {
        return avgScore.toString().removeSuffix(".0")
    }
    
    private fun mapScoreRangeToColor(context: Context, score: Float): Int {
        return when(score) {
            0.0f -> 0
            in 1.0f..1.9f -> context.resources.getColor(R.color.color_broken_status, context.theme)
            in 2.0f..2.9f -> context.resources.getColor(R.color.color_bronze_status, context.theme)
            in 3.0f..3.9f -> context.resources.getColor(R.color.color_silver_status, context.theme)
            else -> context.resources.getColor(R.color.color_gold_status, context.theme)
        }
    }
    
    private fun mapScoreRangeToProgress(score: Float): Int {
        return when(score) {
            0.0f -> 0
            else -> ((score / 4.0f) * 100.0f ).toInt()
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun setProgressAndPercent(score: Float,
                                      isDg: Boolean,
                                      goldRatingsPercent: Float,
                                      silverRatingsPercent: Float,
                                      bronzeRatingsPercent: Float,
                                      brokenRatingsPercent: Float) {
        
        if (isDg) {
            fragmentBinding.dgCircle.apply {
                setIndicatorColor(mapScoreRangeToColor(requireContext(), score))
                setProgressCompat(mapScoreRangeToProgress(score), true)
            }
            fragmentBinding.dgGoldProgress.setProgressCompat(goldRatingsPercent.toInt(), true)
            fragmentBinding.dgGoldPercent.text = "${removeDotZeroFromFloat(goldRatingsPercent)}%"
            fragmentBinding.dgSilverProgress.setProgressCompat(silverRatingsPercent.toInt(), true)
            fragmentBinding.dgSilverPercent.text = "${removeDotZeroFromFloat(silverRatingsPercent)}%"
            fragmentBinding.dgBronzeProgress.setProgressCompat(bronzeRatingsPercent.toInt(), true)
            fragmentBinding.dgBronzePercent.text = "${removeDotZeroFromFloat(bronzeRatingsPercent)}%"
            fragmentBinding.dgBrokenProgress.setProgressCompat(brokenRatingsPercent.toInt(), true)
            fragmentBinding.dgBrokenPercent.text = "${removeDotZeroFromFloat(brokenRatingsPercent)}%"
        }
        else {
            fragmentBinding.mgCircle.apply {
                setIndicatorColor(mapScoreRangeToColor(requireContext(), score))
                setProgressCompat(mapScoreRangeToProgress(score), true)
            }
            // No need to animate progress indicators here,
            // as they won't be shown unless scrolled
            fragmentBinding.mgGoldProgress.progress = goldRatingsPercent.toInt()
            fragmentBinding.mgGoldPercent.text = "${removeDotZeroFromFloat(goldRatingsPercent)}%"
            fragmentBinding.mgSilverProgress.progress = silverRatingsPercent.toInt()
            fragmentBinding.mgSilverPercent.text = "${removeDotZeroFromFloat(silverRatingsPercent)}%"
            fragmentBinding.mgBronzeProgress.progress = bronzeRatingsPercent.toInt()
            fragmentBinding.mgBronzePercent.text = "${removeDotZeroFromFloat(bronzeRatingsPercent)}%"
            fragmentBinding.mgBrokenProgress.progress = brokenRatingsPercent.toInt()
            fragmentBinding.mgBrokenPercent.text = "${removeDotZeroFromFloat(brokenRatingsPercent)}%"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}