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

package tech.techlore.plexus.models.main

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "main_table")
data class MainData(
    
    var name: String = "",
    
    @PrimaryKey
    var packageName: String = "",
    
    var iconUrl: String? = null,
    
    var dgScore: Float = 0.0f,
    
    var totalDgRatings: Int = 0,
    
    var mgScore: Float = 0.0f,
    
    var totalMgRatings: Int = 0,
    
    var installedVersion: String = "",
    
    var installedBuild: Int = 0,
    
    var installedFrom: String = "",
    
    var isInPlexusData: Boolean = true,
    
    var isInstalled: Boolean = false,
    
    var isFav: Boolean = false

)