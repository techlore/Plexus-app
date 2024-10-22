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

package tech.techlore.plexus.models.post.rating

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostRating(
    
    @SerialName("app_version")
    var version: String,
    
    @SerialName("app_build_number")
    var buildNumber: Int,
    
    @SerialName("rom_name")
    var romName: String,
    
    @SerialName("rom_build")
    var romBuild: String,
    
    @SerialName("android_version")
    var androidVersion: String,
    
    @SerialName("installation_source")
    var installedFrom: String,
    
    @SerialName("rating_type")
    var ratingType: String,
    
    @SerialName("score")
    var score: Int,
    
    @SerialName("notes")
    var notes: String,
)