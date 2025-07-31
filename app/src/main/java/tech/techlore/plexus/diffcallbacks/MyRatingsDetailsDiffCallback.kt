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

package tech.techlore.plexus.diffcallbacks

import androidx.recyclerview.widget.DiffUtil
import tech.techlore.plexus.models.myratings.MyRatingDetails

class MyRatingsDetailsDiffCallback : DiffUtil.ItemCallback<MyRatingDetails>() {
    
    override fun areItemsTheSame(oldItem: MyRatingDetails, newItem: MyRatingDetails): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: MyRatingDetails, newItem: MyRatingDetails): Boolean {
        return oldItem == newItem
    }
}