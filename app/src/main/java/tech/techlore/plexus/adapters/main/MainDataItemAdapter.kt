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

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.PopupTextProvider
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.utils.MainDataMinimalDiffUtil
import tech.techlore.plexus.models.minimal.MainDataMinimal
import tech.techlore.plexus.utils.UiUtils.Companion.hScrollText
import tech.techlore.plexus.utils.UiUtils.Companion.mapStatusStringToBgColor
import kotlin.collections.ArrayList

class MainDataItemAdapter(private val aListViewItems: ArrayList<MainDataMinimal>,
                          private val clickListener: OnItemClickListener,
                          private val coroutineScope: CoroutineScope) :
    RecyclerView.Adapter<MainDataItemAdapter.ListViewHolder>(), PopupTextProvider {
    
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    
    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: MaterialTextView = itemView.findViewById(R.id.name)
        val packageName: MaterialTextView = itemView.findViewById(R.id.package_name)
        val dgStatus: MaterialTextView = itemView.findViewById(R.id.dg_status)
        val mgStatus: MaterialTextView = itemView.findViewById(R.id.mg_status)
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
            LayoutInflater.from(parent.context).inflate(R.layout.item_main_recycler_view, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        val plexusData = aListViewItems[position]
        val context = holder.itemView.context
        
        if (plexusData.isInstalled) {
            try {
                holder.icon.setImageDrawable(context.packageManager.getApplicationIcon(plexusData.packageName))
                // Don't use GLIDE to load icons directly to ImageView
                // as there's a delay in displaying icons when fast scrolling
            }
            catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        else {
        Glide.with(context)
            .load("")
            .placeholder(R.drawable.ic_apk)
            .into(holder.icon)
        }
        
        holder.name.text = plexusData.name
        holder.packageName.text = plexusData.packageName
        holder.dgStatus.text = plexusData.dgStatus
        holder.dgStatus.backgroundTintList =
            mapStatusStringToBgColor(context, plexusData.dgStatus)?.let { ColorStateList.valueOf(it) }
        holder.mgStatus.text = plexusData.mgStatus
        holder.mgStatus.backgroundTintList =
            mapStatusStringToBgColor(context, plexusData.mgStatus)?.let { ColorStateList.valueOf(it) }
        holder.fav.isChecked = plexusData.isFav
        
        /// Horizontally scrolling text
        hScrollText(holder.name)
        hScrollText(holder.packageName)
        
        holder.fav.setOnCheckedChangeListener{ _, isChecked ->
            plexusData.isFav = isChecked
            coroutineScope.launch {
                (context.applicationContext as ApplicationManager)
                    .miniRepository
                    .updateFav(plexusData)
            }
        }
        
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
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