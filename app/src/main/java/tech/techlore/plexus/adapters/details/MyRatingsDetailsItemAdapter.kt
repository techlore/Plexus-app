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

package tech.techlore.plexus.adapters.details

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.diffcallbacks.MyRatingsDetailsDiffCallback
import tech.techlore.plexus.models.myratings.MyRatingDetails
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromStyle
import tech.techlore.plexus.utils.UiUtils.Companion.setStatusStyleWithIcon

class MyRatingsDetailsItemAdapter()
    : ListAdapter<MyRatingDetails, MyRatingsDetailsItemAdapter.ListViewHolder>(MyRatingsDetailsDiffCallback()) {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val notesCard: MaterialCardView = itemView.findViewById(R.id.ratingsNotesCard)
        val notes: MaterialTextView = itemView.findViewById(R.id.ratingsNotes)
        val translateBtn: MaterialButton = itemView.findViewById(R.id.translateBtn)
        val version: MaterialTextView = itemView.findViewById(R.id.ratingsVersionSubtitle)
        val rom: MaterialTextView = itemView.findViewById(R.id.ratingsVersionSubtitle)
        val androidVersion: MaterialTextView = itemView.findViewById(R.id.ratingsAndroidVersion)
        val installedFrom: MaterialTextView = itemView.findViewById(R.id.ratingsInstalledFrom)
        val status: MaterialTextView = itemView.findViewById(R.id.ratingsStatus)
        val dateTime: MaterialTextView = itemView.findViewById(R.id.ratingsDateTime)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ratings_details_rv, parent, false)
        )
    }
    
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        val myRatingsDetails = getItem(position)
        val context = holder.itemView.context
        
        // Notes
        if (!myRatingsDetails.notes.isNullOrEmpty()) {
            holder.notes.text = myRatingsDetails.notes
            holder.translateBtn.visibility = View.INVISIBLE
        }
        else {
            holder.notesCard.isVisible = false
        }
        
        holder.version.text = "${myRatingsDetails.version} (${myRatingsDetails.buildNumber})"
        holder.rom.text = "${myRatingsDetails.romName} (${myRatingsDetails.romBuild})"
        holder.androidVersion.text = myRatingsDetails.androidVersion
        
        holder.installedFrom.setInstalledFromStyle(context, myRatingsDetails.installedFrom)
        
        holder.status.setStatusStyleWithIcon(
            context,
            myRatingsDetails.googleLib,
            myRatingsDetails.myRatingScore
        )
        
        // TODO: Disable date time text for now,
        //  if added, database migration is needed
        //  will look into it in the future when edit ratings is added
        holder.dateTime.isVisible = false
        
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
}