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

package tech.techlore.plexus.adapters.main

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.utils.UiUtils.Companion.installedFromTextViewStyle
import tech.techlore.plexus.utils.UiUtils.Companion.mapRatingScoreToStatusTextStyle
import tech.techlore.plexus.utils.UiUtils.Companion.statusTextViewIcon

class MyRatingItemAdapter (private val aListViewItems: ArrayList<MyRating>) : RecyclerView.Adapter<MyRatingItemAdapter.ListViewHolder>() {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val icon: ShapeableImageView = itemView.findViewById(R.id.my_ratings_icon)
        val name: MaterialTextView = itemView.findViewById(R.id.my_ratings_name)
        val version: MaterialTextView = itemView.findViewById(R.id.my_ratings_version)
        val rom: MaterialTextView = itemView.findViewById(R.id.my_ratings_rom)
        val androidVersion: MaterialTextView = itemView.findViewById(R.id.my_ratings_android_version)
        val notes: MaterialTextView = itemView.findViewById(R.id.my_ratings_notes)
        val status: MaterialTextView = itemView.findViewById(R.id.my_ratings_status)
        val installedFrom: MaterialTextView = itemView.findViewById(R.id.my_ratings_installed_from)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyRatingItemAdapter.ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_my_ratings_recycler_view, parent, false)
        )
    }
    
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyRatingItemAdapter.ListViewHolder, position: Int) {
        
        val myRating = aListViewItems[position]
        val context = holder.itemView.context
    
        holder.icon.apply {
            if (myRating.isInstalled) {
                try {
                    setImageDrawable(context.packageManager.getApplicationIcon(myRating.packageName))
                    // Don't use GLIDE to load icons directly to ImageView
                    // as there's a delay in displaying icons when fast scrolling
                }
                catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
            else {
                val requestOptions =
                    RequestOptions()
                        .placeholder(R.drawable.ic_apk) // Placeholder icon
                        .fallback(R.drawable.ic_apk) // Fallback image in case requested image isn't available
                        .centerCrop() // Center-crop the image to fill the ImageView
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache strategy
        
                Glide.with(context)
                    .load(myRating.iconUrl)
                    .onlyRetrieveFromCache(true) // Icon should always be in cache
                    .apply(requestOptions)
                    .into(this)
            }
        }
        
        holder.name.text = myRating.name
        holder.version.text = "${context.getString(R.string.version)}: ${myRating.version} (${myRating.buildNumber})"
        holder.rom.text = "${context.getString(R.string.rom)}: ${myRating.romName} (${myRating.romBuild})"
        holder.androidVersion.text = "${context.getString(R.string.android)}: ${myRating.androidVersion}"
        
        // Notes
        if (!myRating.notes.isNullOrEmpty()) {
            holder.notes.text = myRating.notes
        }
        else {
            holder.notes.isVisible = false
        }
        
        // Status
        statusTextViewIcon(context, myRating.googleLib, holder.status)
        mapRatingScoreToStatusTextStyle(context, myRating.ratingScore, holder.status)
    
        // Installed from
        installedFromTextViewStyle(context, myRating.installedFrom, holder.installedFrom)
        
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
}