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
        
        val detailsActivity = requireActivity() as AppDetailsActivity
        val dgScoreString = "${detailsActivity.app.dgScore.dgScore}/4"
        val mgScoreString = "${detailsActivity.app.mgScore.mgScore}/4"
        val totalDgRatings = detailsActivity.app.dgScore.totalDgRatings
        val totalMgRatings = detailsActivity.app.mgScore.totalMgRatings
        
        val ratingRanges = mapOf("gold" to 4.0f,
                                 "silver" to 3.0f,
                                 "bronze" to 2.0f,
                                 "broken" to 1.0f)
        
        val ratingCounts = mutableMapOf<Pair<String?, String>, Int>()
        for (library in listOf("none", "microG")) {
            for ((range, value) in ratingRanges) {
                val count = detailsActivity.app.ratingsList.count {
                    it.googleLib == library
                    && it.ratingsScore!!.ratingsScore >= value
                    && it.ratingsScore!!.ratingsScore < ratingRanges[nextRange(range)]!!
                }
                ratingCounts[library to range] = count
            }
        }
        
        val dgGoldRatingsCount = ratingCounts["none" to "gold"] ?: 0
        val dgSilverRatingsCount = ratingCounts["none" to "silver"] ?: 0
        val dgBronzeRatingsCount = ratingCounts["none" to "bronze"] ?: 0
        val dgBrokenRatingsCount = ratingCounts["none" to "broken"] ?: 0
        val mgGoldRatingsCount = ratingCounts["microG" to "gold"] ?: 0
        val mgSilverRatingsCount = ratingCounts["microG" to "silver"] ?: 0
        val mgBronzeRatingsCount = ratingCounts["microG" to "bronze"] ?: 0
        val mgBrokenRatingsCount = ratingCounts["microG" to "broken"] ?: 0
        
        val dgGoldRatingsPercent = calcPercent(dgGoldRatingsCount, totalDgRatings)
        val dgSilverRatingsPercent = calcPercent(dgSilverRatingsCount, totalDgRatings)
        val dgBronzeRatingsPercent = calcPercent(dgBronzeRatingsCount, totalDgRatings)
        val dgBrokenRatingsPercent = calcPercent(dgBrokenRatingsCount, totalDgRatings)
        val mgGoldRatingsPercent = calcPercent(mgGoldRatingsCount, totalMgRatings)
        val mgSilverRatingsPercent = calcPercent(mgSilverRatingsCount, totalMgRatings)
        val mgBronzeRatingsPercent = calcPercent(mgBronzeRatingsCount, totalMgRatings)
        val mgBrokenRatingsPercent = calcPercent(mgBrokenRatingsCount, totalMgRatings)
        
        fragmentBinding.dgTotalScore.text = dgScoreString
        fragmentBinding.dgTotalRatings.text = "${getString(R.string.total_ratings)}: $totalDgRatings"
        setDgProgressAndPercent(dgGoldRatingsPercent, dgSilverRatingsPercent, dgBronzeRatingsPercent, dgBrokenRatingsPercent)
        
        fragmentBinding.mgTotalScore.text = mgScoreString
        fragmentBinding.mgTotalRatings.text = "${getString(R.string.total_ratings)}: $totalMgRatings"
        setMgProgressAndPercent(mgGoldRatingsPercent, mgSilverRatingsPercent, mgBronzeRatingsPercent, mgBrokenRatingsPercent)
    }
    
    private fun nextRange(currentRange: String): String {
        return when (currentRange) {
            "gold" -> "gold"
            "silver" -> "gold"
            "bronze" -> "silver"
            "broken" -> "bronze"
            else -> error("Invalid rating range")
        }
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
        fragmentBinding.dgGoldProgress.progress = dgGoldRatingsPercent.toInt()
        fragmentBinding.dgGoldPercent.text = "${dgGoldRatingsPercent}%"
        fragmentBinding.dgSilverProgress.progress = dgSilverRatingsPercent.toInt()
        fragmentBinding.dgSilverPercent.text = "${dgSilverRatingsPercent}%"
        fragmentBinding.dgBronzeProgress.progress = dgBronzeRatingsPercent.toInt()
        fragmentBinding.dgBronzePercent.text = "${dgBronzeRatingsPercent}%"
        fragmentBinding.dgBrokenProgress.progress = dgBrokenRatingsPercent.toInt()
        fragmentBinding.dgBrokenPercent.text = "${dgBrokenRatingsPercent}%"
    }
    
    @SuppressLint("SetTextI18n")
    private fun setMgProgressAndPercent(mgGoldRatingsPercent: Float,
                                        mgSilverRatingsPercent: Float,
                                        mgBronzeRatingsPercent: Float,
                                        mgBrokenRatingsPercent: Float) {
        fragmentBinding.mgGoldProgress.progress = mgGoldRatingsPercent.toInt()
        fragmentBinding.mgGoldPercent.text = "${mgGoldRatingsPercent}%"
        fragmentBinding.mgSilverProgress.progress = mgSilverRatingsPercent.toInt()
        fragmentBinding.mgSilverPercent.text = "${mgSilverRatingsPercent}%"
        fragmentBinding.mgBronzeProgress.progress = mgBronzeRatingsPercent.toInt()
        fragmentBinding.mgBronzePercent.text = "${mgBronzeRatingsPercent}%"
        fragmentBinding.mgBrokenProgress.progress = mgBrokenRatingsPercent.toInt()
        fragmentBinding.mgBrokenPercent.text = "${mgBrokenRatingsPercent}%"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        //job.cancel()
        _binding = null
    }
    
}