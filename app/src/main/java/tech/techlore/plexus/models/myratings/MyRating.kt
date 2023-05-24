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

package tech.techlore.plexus.models.myratings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_ratings_table")
data class MyRating(
    
    @PrimaryKey
    var id: String,
    
    var name: String,
    
    var packageName: String,
    
    var iconUrl: String? = null,
    
    var version: String,
    
    var buildNumber: Int,
    
    var romName: String,
    
    var romBuild: String,

    var androidVersion: String,
    
    var isInstalled: Boolean = true,
    
    var installedFrom: String,
    
    var googleLib: String,
    
    var ratingScore: Int = 0,
    
    var notes: String? = null

)