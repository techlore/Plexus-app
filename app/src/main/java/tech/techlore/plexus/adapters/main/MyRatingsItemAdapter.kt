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

package tech.techlore.plexus.adapters.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import me.stellarsand.android.fastscroll.PopupTextProvider
import tech.techlore.plexus.R
import tech.techlore.plexus.models.mini.MyRatingMini
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon

class MyRatingsItemAdapter(
    private val clickListener: OnItemClickListener
) : PagingDataAdapter<MyRatingMini, MyRatingsItemAdapter.ListViewHolder>(DIFF_CALLBACK),
    PopupTextProvider {
    
    var isGridViewLayout = false
    
    private companion object {
        private const val VIEW_TYPE_LIST = 0
        private const val VIEW_TYPE_GRID = 1
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<MyRatingMini>() {
                override fun areItemsTheSame(oldItem: MyRatingMini, newItem: MyRatingMini): Boolean {
                    return oldItem.packageName == newItem.packageName
                }
                override fun areContentsTheSame(oldItem: MyRatingMini, newItem: MyRatingMini): Boolean {
                    return oldItem == newItem
                }
            }
    }
    
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
            LayoutInflater
                .from(parent.context)
                .inflate(
                    if (viewType != VIEW_TYPE_GRID) R.layout.item_my_ratings_rv_list else R.layout.item_my_ratings_rv_grid,
                    parent,
                    false)
        )
    }
    
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        val myRating = getItem(position)!!
        val context = holder.itemView.context
        
        holder.icon.displayAppIcon(
            context = context,
            isInstalled = myRating.isInstalled,
            packageName = myRating.packageName,
            iconUrl = myRating.iconUrl
        )
        
        holder.name.text = myRating.name
        holder.packageName.text = myRating.packageName
        holder.totalCount.text = myRating.totalRatings.toString()
        
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (!isGridViewLayout) VIEW_TYPE_LIST else VIEW_TYPE_GRID
    }
    
    // Fast scroll popup
    override fun getPopupText(view: View, position: Int): CharSequence {
        return getItem(position)!!.name.first().uppercaseChar().toString()
    }
}