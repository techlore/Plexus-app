/*
 * Copyright (c) 2022 Techlore
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

package tech.techlore.plexus.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.PopupTextProvider
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.utils.MainDataMinimalDiffUtil
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.utils.UiUtils.Companion.hScrollText
import java.util.Locale
import kotlin.collections.ArrayList

class PlexusDataItemAdapter(private val aListViewItems: ArrayList<MainDataMinimal>,
                            private val clickListener: OnItemClickListener,
                            private val longClickListener: OnItemLongCLickListener,
                            private val coroutineScope: CoroutineScope) :
    RecyclerView.Adapter<PlexusDataItemAdapter.ListViewHolder>(), Filterable, PopupTextProvider {
    
    private val aListViewItemsFull: List<MainDataMinimal>
    
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    
    interface OnItemLongCLickListener {
        fun onItemLongCLick(position: Int)
    }
    
    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: TextView = itemView.findViewById(R.id.name)
        val packageName: TextView = itemView.findViewById(R.id.package_name)
        //val version: TextView = itemView.findViewById(R.id.version)
        val fav: MaterialCheckBox = itemView.findViewById(R.id.fav)
        
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
        
        override fun onClick(v: View?) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                clickListener.onItemClick(position)
            }
        }
        
        override fun onLongClick(v: View?): Boolean {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                longClickListener.onItemLongCLick(position)
            }
            return true
        }
    }
    
    init {
        aListViewItemsFull = ArrayList(aListViewItems)
        setHasStableIds(true)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        val plexusData = aListViewItems[position]
        val context = holder.itemView.context
        /*if (plexusData.isInstalled) {
            try {
                holder.icon.setImageDrawable(context.packageManager.getApplicationIcon(plexusData.packageName))
                // Don't use GLIDE to load icons directly to ImageView
                // as there's a delay in displaying icons when fast scrolling
            }
            catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        else {*/
        Glide.with(context)
            .load("")
            .placeholder(R.drawable.ic_apk)
            .into(holder.icon)
        //}
        
        holder.name.text = plexusData.name
        holder.packageName.text = plexusData.packageName
        //holder.version.text = plexusData.version
        holder.fav.isChecked = plexusData.isFav
        
        /// Horizontally scrolling text
        hScrollText(holder.name)
        hScrollText(holder.packageName)
        //hScrollText(holder.version)
        
        holder.fav.setOnCheckedChangeListener{ _, isChecked ->
            plexusData.isFav = isChecked
            coroutineScope.launch {
                (context.applicationContext as ApplicationManager)
                    .miniRepository
                    .update(plexusData)
            }
        }
        
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
    
    // Req. for search
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val filteredList: ArrayList<MainDataMinimal> = ArrayList()
                if (charSequence.isNotEmpty()) {
                    val searchString =
                        charSequence.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                    for (plexusData in aListViewItemsFull) {
                        if (plexusData.name.lowercase(Locale.getDefault()).contains(searchString)
                            || plexusData.packageName.lowercase(Locale.getDefault())
                                .contains(searchString)) {
                            filteredList.add(plexusData)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }
            
            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                aListViewItems.clear()
                aListViewItems.addAll((filterResults.values as ArrayList<MainDataMinimal>))
                notifyDataSetChanged()
            }
        }
    }
    
    // Fast scroll popup
    override fun getPopupText(position: Int): String {
        return aListViewItems[position].name.substring(0, 1)
    }
    
    fun updateList(newList: ArrayList<MainDataMinimal>){
        val diffCallback = MainDataMinimalDiffUtil(aListViewItems, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        aListViewItems.clear()
        aListViewItems.addAll(newList)
    }
}