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

package tech.techlore.plexus.models.myratings

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import tech.techlore.plexus.objects.MyRatingsDetailsConverter

@Serializable
@Entity(tableName = "my_ratings_table")
@TypeConverters(MyRatingsDetailsConverter::class)
data class MyRating(
    
    var name: String,
    
    @PrimaryKey
    var packageName: String,
    
    var iconUrl: String? = null,

    var isInstalled: Boolean = true,
    
    var ratingsDetails: List<MyRatingDetails>

)
