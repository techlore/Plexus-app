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
import tech.techlore.plexus.diffcallbacks.MainDataMiniDiffCallback
import tech.techlore.plexus.interfaces.main.FavToggleListener
import tech.techlore.plexus.models.mini.MainDataMini
import tech.techlore.plexus.utils.UiUtils.Companion.hScroll
import tech.techlore.plexus.utils.UiUtils.Companion.displayAppIcon
import tech.techlore.plexus.utils.UiUtils.Companion.mapScoreRangeToStatusString
import tech.techlore.plexus.utils.UiUtils.Companion.setStatusStyleWithoutIcon

class MainDataItemAdapter(private val clickListener: OnItemClickListener,
                          private val favToggleListener: FavToggleListener,
                          private val isFavFrag: Boolean = false) :
    ListAdapter<MainDataMini, MainDataItemAdapter.ListViewHolder>(MainDataMiniDiffCallback()),
    PopupTextProvider {
    
    var isGridViewLayout = false
    
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
                    if (!isGridViewLayout) R.layout.item_main_rv_list else R.layout.item_main_rv_grid,
                    parent,
                    false
                )
        )
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        if (isFavFrag) holder.fav.setOnCheckedChangeListener(null)
        
        val mainDataMini = getItem(position)
        val context = holder.itemView.context
        
        holder.icon.displayAppIcon(
            context = context,
            isInstalled = mainDataMini.isInstalled,
            packageName = mainDataMini.packageName,
            iconUrl = mainDataMini.iconUrl
        )
        
        holder.name.apply {
            text = mainDataMini.name
            hScroll()
        }
        
        holder.packageName.apply {
            text = mainDataMini.packageName
            hScroll()
        }
        
        holder.dgStatus.apply {
            text = mapScoreRangeToStatusString(context, mainDataMini.dgScore)//context.getString(mainDataMini.dgStatusStringResId)
            setStatusStyleWithoutIcon(context, mainDataMini.dgScore)
        }
        
        holder.mgStatus.apply {
            text = mapScoreRangeToStatusString(context, mainDataMini.mgScore)
            setStatusStyleWithoutIcon(context, mainDataMini.mgScore)
        }
        
        holder.fav.apply {
            isChecked = mainDataMini.isFav
            setOnCheckedChangeListener{ _, isChecked ->
                favToggleListener.onFavToggled(mainDataMini, isChecked)
            }
        }
        
    }
    
    // Fast scroll popup
    override fun getPopupText(view: View, position: Int): CharSequence {
        // Prevent app crashing due to IndexOutOfBoundsException in some cases like:
        // search for "rev" > once results appear, append "o" to the search query >
        // new query is "revo" > app crashes
        //
        // https://github.com/techlore/Plexus-app/issues/78
        // It is not always reproducible in release build on my end,
        // although I was able to reproduce it in debug build consistently.
        //
        // UPDATE: It is no more reproducible as now
        // search is performed in onQueryTextSubmit() instead of onQueryTextChange()
        if (position !in 0..<itemCount) return ""
        
        return getItem(position).name.first().uppercaseChar().toString()
    }
}