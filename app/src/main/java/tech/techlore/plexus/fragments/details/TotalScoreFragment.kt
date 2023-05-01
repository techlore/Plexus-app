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
import java.text.DecimalFormat

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
            
            val ratingRanges = mapOf("limit_reached" to 4.1f, // Dummy range, check nextRange()
                                     "gold" to 4.0f,
                                     "silver" to 3.0f,
                                     "bronze" to 2.0f,
                                     "broken" to 1.0f)
            
            val ratingCounts = mutableMapOf<Pair<String?, String>, Int>()
            for (library in listOf("none", "micro_g")) {
                for ((range, value) in ratingRanges) {
                    val count = detailsActivity.app.ratingsList.count {
                        it.googleLib == library
                        && it.ratingScore !!.ratingScore >= value
                        && it.ratingScore !!.ratingScore < ratingRanges[nextRange(range)] !!
                    }
                    ratingCounts[library to range] = count
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
            
            val dgGoldRatingsPercent = calcPercent(dgGoldRatingsCount, totalDgRatings)
            val dgSilverRatingsPercent = calcPercent(dgSilverRatingsCount, totalDgRatings)
            val dgBronzeRatingsPercent = calcPercent(dgBronzeRatingsCount, totalDgRatings)
            val dgBrokenRatingsPercent = calcPercent(dgBrokenRatingsCount, totalDgRatings)
            val mgGoldRatingsPercent = calcPercent(mgGoldRatingsCount, totalMgRatings)
            val mgSilverRatingsPercent = calcPercent(mgSilverRatingsCount, totalMgRatings)
            val mgBronzeRatingsPercent = calcPercent(mgBronzeRatingsCount, totalMgRatings)
            val mgBrokenRatingsPercent = calcPercent(mgBrokenRatingsCount, totalMgRatings)
            
            // Update UI with all results
            withContext(Dispatchers.Main) {
                fragmentBinding.dgAvgScore.text = "${removeDotZeroFromFloat(detailsActivity.app.dgScore)}/4"
                fragmentBinding.dgTotalRatings.text = "${getString(R.string.total_ratings)}: $totalDgRatings"
                setDgProgressAndPercent(dgGoldRatingsPercent,
                                        dgSilverRatingsPercent,
                                        dgBronzeRatingsPercent,
                                        dgBrokenRatingsPercent)
                
                fragmentBinding.mgAvgScore.text = "${removeDotZeroFromFloat(detailsActivity.app.mgScore)}/4"
                fragmentBinding.mgTotalRatings.text = "${getString(R.string.total_ratings)}: $totalMgRatings"
                setMgProgressAndPercent(mgGoldRatingsPercent,
                                        mgSilverRatingsPercent,
                                        mgBronzeRatingsPercent,
                                        mgBrokenRatingsPercent)
            }
        }
    }
    
    private fun nextRange(currentRange: String): String {
        return when (currentRange) {
            "gold" -> "limit_reached" // "limit_reached" is used to know that real range has reached "gold"
            "silver" -> "gold"
            "bronze" -> "silver"
            "broken" -> "bronze"
            else -> error("Invalid rating range")
        }
    }
    
    private fun removeDotZeroFromFloat(avgScore: Float): String {
        return avgScore.toString().removeSuffix(".0")
    }
    
    private fun calcPercent(ratingsCount: Int, totalRatings: Int): Float {
        return if (totalRatings == 0) 0.0f else {
            DecimalFormat("#.#") // Limit result to 1 decimal place
                .format((ratingsCount.toFloat() / totalRatings.toFloat()) * 100)
                .toFloat()
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