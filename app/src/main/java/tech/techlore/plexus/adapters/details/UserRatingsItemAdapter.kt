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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.appdetails.TranslateRatingNoteBottomSheet
import tech.techlore.plexus.models.get.ratings.Rating
import tech.techlore.plexus.diffcallbacks.UserRatingsDetailsDiffCallback
import tech.techlore.plexus.utils.UiUtils.Companion.setInstalledFromStyle
import tech.techlore.plexus.utils.UiUtils.Companion.setStatusStyleWithIcon

class UserRatingsItemAdapter(
    private val fragmentManager: FragmentManager
) : ListAdapter<Rating, UserRatingsItemAdapter.ListViewHolder>(UserRatingsDetailsDiffCallback()) {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val notesCard: MaterialCardView = itemView.findViewById(R.id.ratingsNotesCard)
        val notes: MaterialTextView = itemView.findViewById(R.id.ratingsNotes)
        val translateBtn: MaterialButton = itemView.findViewById(R.id.translateBtn)
        val version: MaterialTextView = itemView.findViewById(R.id.ratingsVersionSubtitle)
        val rom: MaterialTextView = itemView.findViewById(R.id.ratingsRomSubtitle)
        val androidVersion: MaterialTextView = itemView.findViewById(R.id.ratingsAndroidVersion)
        val installedFrom: MaterialTextView = itemView.findViewById(R.id.ratingsInstalledFrom)
        val status: MaterialTextView = itemView.findViewById(R.id.ratingsStatus)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ratings_details_rv, parent, false)
        )
    }
    
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        val userRating = getItem(position)
        val context = holder.itemView.context
        
        // Notes
        if (!userRating.notes.isNullOrEmpty()) {
            holder.notes.text = userRating.notes
            holder.translateBtn.setOnClickListener {
                TranslateRatingNoteBottomSheet(userRating.notes!!).show(fragmentManager, "TranslateRatingNoteBottomSheet")
            }
        }
        else {
            holder.notesCard.isVisible = false
        }
        
        holder.version.text = "${userRating.version} (${userRating.buildNumber})"
        holder.rom.text = "${userRating.romName} (${userRating.romBuild})"
        holder.androidVersion.text = userRating.androidVersion
        
        // Installed from
        holder.installedFrom.setInstalledFromStyle(context, userRating.installedFrom)
        
        // Status
        holder.status.setStatusStyleWithIcon(context,
                                             userRating.ratingType!!,
                                             userRating.ratingScore!!.ratingScore)
        
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
}