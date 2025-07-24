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

package tech.techlore.plexus.adapters.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.MainActivity
import tech.techlore.plexus.models.settings.SupportMethod
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class SupportMethodItemAdapter (private val aListViewItems: ArrayList<SupportMethod>,
                                private val mainActivity: MainActivity) : RecyclerView.Adapter<SupportMethodItemAdapter.ListViewHolder>() {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val supportMethodTitle: MaterialTextView = itemView.findViewById(R.id.supportMethodTitle)
        val supportMethodQr: ShapeableImageView = itemView.findViewById(R.id.supportMethodQr)
        val liberapayUrl: MaterialTextView = itemView.findViewById(R.id.liberapayUrl)
        val moneroAddress: MaterialTextView = itemView.findViewById(R.id.moneroAddress)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListViewHolder {
        return ListViewHolder(LayoutInflater.from(parent.context)
                                  .inflate(R.layout.item_support_methods_rv, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        val supportMethod = aListViewItems[position]
        
        holder.supportMethodTitle.apply {
            text = supportMethod.title
            setCompoundDrawablesWithIntrinsicBounds(supportMethod.titleIcon, 0, 0, 0)
        }
        
        holder.supportMethodQr.setImageResource(supportMethod.qr)
        
        holder.liberapayUrl.apply {
            isVisible = position == 0
            setOnClickListener{
                mainActivity.openURL(text.toString())
            }
        }
        
        holder.moneroAddress.isVisible = position == 1
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
}