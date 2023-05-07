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

package tech.techlore.plexus.adapters.details

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.utils.UiUtils.Companion.mapRatingScoreToStatusTextStyle
import tech.techlore.plexus.utils.UiUtils.Companion.statusTextViewIcon

class UserRatingsItemAdapter (private val aListViewItems: ArrayList<Rating>) : RecyclerView.Adapter<UserRatingsItemAdapter.ListViewHolder>() {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val status: MaterialTextView = itemView.findViewById(R.id.ratings_status)
        val version: MaterialTextView = itemView.findViewById(R.id.ratings_version)
        val note: MaterialTextView = itemView.findViewById(R.id.ratings_notes)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): UserRatingsItemAdapter.ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_ratings_recycler_view, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: UserRatingsItemAdapter.ListViewHolder, position: Int) {
        
        val rating = aListViewItems[position]
        val context = holder.itemView.context
        
        @SuppressLint("SetTextI18n")
        holder.version.text = "${context.getString(R.string.version)}: ${rating.version}"
        
        // Notes
        if (!rating.note.isNullOrEmpty()) {
            holder.note.text = rating.note
        }
        else {
            holder.note.visibility = View.GONE
        }
    
        // Status
        statusTextViewIcon(context, rating.googleLib!!, holder.status)
        mapRatingScoreToStatusTextStyle(context, rating.ratingScore!!.ratingScore, holder.status)
        
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
}