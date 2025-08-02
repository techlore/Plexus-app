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

package tech.techlore.plexus.models.get.ratings

import androidx.room.Embedded
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Rating(
    
    @SerialName("id")
    var id: String,
    
    @SerialName("app_package")
    @Transient
    var packageName: String = "",
    
    @SerialName("app_version")
    var version: String,
    
    @SerialName("app_build_number")
    var buildNumber: Long,
    
    @SerialName("rom_name")
    var romName: String,
    
    @SerialName("rom_build")
    var romBuild: String,
    
    @SerialName("android_version")
    var androidVersion: String,
    
    @SerialName("installation_source")
    var installedFrom: String,
    
    @SerialName("rating_type")
    var ratingType: String? = null,
    
    @SerialName("score")
    @Embedded
    var ratingScore: RatingScore? = null,
    
    @SerialName("notes")
    var notes: String? = null,
    
    @SerialName("rated_at")
    var ratingDateTime: String = ""

)