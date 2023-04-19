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

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.models.ratings.Ratings

class UserRatingsItemAdapter (private val aListViewItems: ArrayList<Ratings>) : RecyclerView.Adapter<UserRatingsItemAdapter.ListViewHolder>() {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val dgMgText: MaterialTextView = itemView.findViewById(R.id.ratings_dg_mg_text)
        val status: MaterialTextView = itemView.findViewById(R.id.ratings_status)
        val version: MaterialTextView = itemView.findViewById(R.id.ratings_version)
        val note: MaterialTextView = itemView.findViewById(R.id.ratings_note)
        
    }
    
    init {
        setHasStableIds(true)
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
        
        holder.dgMgText.text =
            if (rating.googleLib.equals("none")) {
                context.getString(R.string.de_Googled)
            }
            else {
                context.getString(R.string.microG)
            }
        
        val (statusString, backgroundTint) =
            when(rating.ratingsScore!!.ratingsScore) {
                1 -> Pair(context.getString(R.string.broken_title),
                          context.resources.getColor(R.color.color_broken_status, context.theme))
                2 -> Pair(context.getString(R.string.bronze_title),
                          context.resources.getColor(R.color.color_bronze_status, context.theme))
                3 -> Pair(context.getString(R.string.silver_title),
                          context.resources.getColor(R.color.color_silver_status, context.theme))
                else -> Pair(context.getString(R.string.gold_title),
                             context.resources.getColor(R.color.color_gold_status, context.theme))
            }
        holder.status.text = statusString
        holder.status.backgroundTintList = ColorStateList.valueOf(backgroundTint)
        
        val versionString = "${context.getString(R.string.version)}: ${rating.version}"
        holder.version.text = versionString
        holder.note.text = rating.note
        
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
}