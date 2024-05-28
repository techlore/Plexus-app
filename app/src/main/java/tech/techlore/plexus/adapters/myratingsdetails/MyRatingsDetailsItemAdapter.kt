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

package tech.techlore.plexus.adapters.myratingsdetails

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromTextViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.setStatusTextViewStyle

class MyRatingsDetailsItemAdapter(private val aListViewItems: List<MyRatingDetails>) : RecyclerView.Adapter<MyRatingsDetailsItemAdapter.ListViewHolder>() {
    
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
        
        val myRatingsDetails = aListViewItems[position]
        val context = holder.itemView.context
        
        holder.version.text = "${context.getString(R.string.version)}: ${myRatingsDetails.version} (${myRatingsDetails.buildNumber})"
        holder.rom.text = "${context.getString(R.string.rom)}: ${myRatingsDetails.romName} (${myRatingsDetails.romBuild})"
        
        // Notes
        if (!myRatingsDetails.notes.isNullOrEmpty()) {
            holder.notes.text = myRatingsDetails.notes
        }
        else {
            holder.notes.isVisible = false
        }
        
        holder.androidVersion.text = myRatingsDetails.androidVersion
        
        // Installed from
        setInstalledFromTextViewStyle(context, myRatingsDetails.installedFrom, holder.installedFrom)
        
        // Status
        setStatusTextViewStyle(context,
                               myRatingsDetails.googleLib,
                               myRatingsDetails.myRatingScore,
                               holder.status)
        
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
}