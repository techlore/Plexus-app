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

package tech.techlore.plexus.utils

import androidx.recyclerview.widget.DiffUtil
import tech.techlore.plexus.models.minimal.MainDataMinimal

class MainDataMinimalDiffUtil(
    private val oldList: List<MainDataMinimal>,
    private val newList: List<MainDataMinimal>
) : DiffUtil.Callback() {
    
    override fun getOldListSize(): Int {
        return oldList.size
    }
    
    override fun getNewListSize(): Int {
        return newList.size
    }
    
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newList[newItemPosition].packageName === oldList[oldItemPosition].packageName
    }
    
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newList[newItemPosition] == oldList[oldItemPosition]
    }
}