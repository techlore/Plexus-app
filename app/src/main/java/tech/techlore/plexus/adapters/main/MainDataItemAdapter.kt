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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import me.stellarsand.android.fastscroll.PopupTextProvider
import tech.techlore.plexus.R
import tech.techlore.plexus.diffcallbacks.MainDataMinimalDiffCallback
import tech.techlore.plexus.interfaces.FavToggleListener
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.utils.UiUtils.Companion.hScroll
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.setStatusStyleWithoutIcon

class MainDataItemAdapter(private val clickListener: OnItemClickListener,
                          private val favToggleListener: FavToggleListener,
                          private val isGridView: Boolean = false,
                          private val isFavFrag: Boolean = false) :
    ListAdapter<MainDataMinimal, MainDataItemAdapter.ListViewHolder>(MainDataMinimalDiffCallback()),
    PopupTextProvider {
    
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    
    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: MaterialTextView = itemView.findViewById(R.id.name)
        val packageName: MaterialTextView = itemView.findViewById(R.id.packageName)
        val dgStatus: MaterialTextView = itemView.findViewById(R.id.dgStatus)
        val mgStatus: MaterialTextView = itemView.findViewById(R.id.mgStatus)
        val fav: MaterialCheckBox = itemView.findViewById(R.id.fav)
        
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
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(
                    if (!isGridView) R.layout.item_main_rv_list else R.layout.item_main_rv_grid,
                    parent,
                    false
                )
        )
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        if (isFavFrag) holder.fav.setOnCheckedChangeListener(null)
        
        val mainDataMinimal = getItem(position)
        val context = holder.itemView.context
        
        holder.icon.displayAppIcon(
            context = context,
            isInstalled = mainDataMinimal.isInstalled,
            packageName = mainDataMinimal.packageName,
            iconUrl = mainDataMinimal.iconUrl
        )
        
        holder.name.apply {
            text = mainDataMinimal.name
            hScroll()
        }
        
        holder.packageName.apply {
            text = mainDataMinimal.packageName
            hScroll()
        }
        
        holder.dgStatus.setStatusStyleWithoutIcon(context, mainDataMinimal.dgStatus)
        
        holder.mgStatus.setStatusStyleWithoutIcon(context, mainDataMinimal.mgStatus)
        
        holder.fav.apply {
            isChecked = mainDataMinimal.isFav
            setOnCheckedChangeListener{ _, isChecked ->
                favToggleListener.onFavToggled(mainDataMinimal, isChecked)
            }
        }
        
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
    
    // Fast scroll popup
    override fun getPopupText(view: View, position: Int): CharSequence {
        return getItem(position).name.first().let {
            if (it.isLowerCase()) it.uppercase()
            else it
        }.toString()
    }
}