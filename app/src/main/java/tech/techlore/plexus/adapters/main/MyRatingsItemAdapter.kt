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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import me.zhanghai.android.fastscroll.PopupTextProvider
import tech.techlore.plexus.R
import tech.techlore.plexus.models.myratings.MyRating

class MyRatingsItemAdapter(private val aListViewItems: ArrayList<MyRating>,
                           private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<MyRatingsItemAdapter.ListViewHolder>(), PopupTextProvider {
    
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        
        val icon: ShapeableImageView = itemView.findViewById(R.id.myRatingsIcon)
        val name: MaterialTextView = itemView.findViewById(R.id.myRatingsName)
        val packageName: MaterialTextView = itemView.findViewById(R.id.myRatingsPackage)
        val totalCount: MaterialTextView = itemView.findViewById(R.id.myRatingsCount)
    
        init {
            itemView.setOnClickListener(this)
        }
    
        override fun onClick(v: View?) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                clickListener.onItemClick(position)
            }
        }
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_my_ratings_recycler_view, parent, false)
        )
    }
    
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
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
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Cache strategy
                
                Glide.with(context)
                    .load(myRating.iconUrl)
                    .onlyRetrieveFromCache(true) // Icon should always be in cache
                    .apply(requestOptions)
                    .into(this)
            }
        }
        
        holder.name.text = myRating.name
        holder.packageName.text = myRating.packageName
        val totalCount = myRating.ratingsDetails.size
        holder.totalCount.text =
            when {
                totalCount <= 500 -> totalCount.toString()
                else -> "500+"
            }
        
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
    
    // Fast scroll popup
    override fun getPopupText(position: Int): String {
        return aListViewItems[position].name.substring(0, 1)
    }
}