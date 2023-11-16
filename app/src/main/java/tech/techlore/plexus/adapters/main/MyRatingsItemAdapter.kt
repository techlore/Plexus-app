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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import me.stellarsand.android.fastscroll.PopupTextProvider
import tech.techlore.plexus.R
import tech.techlore.plexus.models.myratings.MyRating
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon

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
        
        displayAppIcon(context = context,
                       imageView = holder.icon,
                       isInstalled = myRating.isInstalled,
                       packageName = myRating.packageName,
                       iconUrl = myRating.iconUrl)
        
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
    override fun getPopupText(view: View, position: Int): CharSequence {
        return aListViewItems[position].name.substring(0, 1)
    }
}