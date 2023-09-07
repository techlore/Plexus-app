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

package tech.techlore.plexus.adapters.appdetails

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromTextViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.setStatusTextViewStyle

class UserRatingsItemAdapter(private val aListViewItems: ArrayList<Rating>) : RecyclerView.Adapter<UserRatingsItemAdapter.ListViewHolder>() {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val version: MaterialTextView = itemView.findViewById(R.id.ratingsVersion)
        val rom: MaterialTextView = itemView.findViewById(R.id.ratingsRom)
        val notes: MaterialTextView = itemView.findViewById(R.id.ratingsNotes)
        val androidVersion: MaterialTextView = itemView.findViewById(R.id.ratingsAndroidVersion)
        val installedFrom: MaterialTextView = itemView.findViewById(R.id.ratingsInstalledFrom)
        val status: MaterialTextView = itemView.findViewById(R.id.ratingsStatus)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ratings_details_recycler_view, parent, false)
        )
    }
    
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
    
        val userRating = aListViewItems[position]
        val context = holder.itemView.context
        
        holder.version.text = "${context.getString(R.string.version)}: ${userRating.version} (${userRating.buildNumber})"
        holder.rom.text = "${context.getString(R.string.rom)}: ${userRating.romName} (${userRating.romBuild})"
        
        // Notes
        if (!userRating.notes.isNullOrEmpty()) {
            holder.notes.text = userRating.notes
        }
        else {
            holder.notes.isVisible = false
        }
        
        holder.androidVersion.text = userRating.androidVersion
        
        // Installed from
        setInstalledFromTextViewStyle(context, userRating.installedFrom, holder.installedFrom)
        
        // Status
        setStatusTextViewStyle(context,
                               userRating.ratingType!!,
                               userRating.ratingScore!!.ratingScore,
                               holder.status)
        
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
}