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

package tech.techlore.plexus.models.main

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

@Entity(tableName = "main_table")
data class MainData(
    
    @JsonProperty("id")
    var id: String = "",
    
    @JsonProperty("name")
    var name: String = "",
    
    @PrimaryKey @JsonProperty("package")
    var packageName: String = "",
    
    @JsonProperty("score")
    var dgScore: Int = 0,
    
    @JsonProperty("micro_g_score")
    var mgScore: Int = 0,
    
    var installedVersion: String = "",
    
    var installedBuild: Int = 0,
    
    var installedFrom: String = "",
    
    var isInPlexusData: Boolean = true,
    
    var isInstalled: Boolean = false,
    
    var isFav: Boolean = false

) /*{
    
    val dgStatus: String
        get() {
            return when (dgScore) {
                1 -> "broken"
                2 -> "bronze"
                3 -> "silver"
                4 -> "gold"
                else -> "notTested"
            }
        }
    
    val mgStatus: String
        get() {
            return when (mgScore) {
                1 -> "broken"
                2 -> "bronze"
                3 -> "silver"
                4 -> "gold"
                else -> "notTested"
            }
        }
}*/