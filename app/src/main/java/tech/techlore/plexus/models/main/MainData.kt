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

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.parcelize.Parcelize
import tech.techlore.plexus.deserializer.MainDataDeserializer

@Entity(tableName = "main_table")
@Parcelize
@JsonDeserialize(using = MainDataDeserializer::class)
data class MainData(
    
    @JsonProperty("id")
    var id: String,
    
    @JsonProperty("name")
    var name: String,
    
    @PrimaryKey @JsonProperty("package")
    var packageName: String,
    
    var installedVersion: String,
    
    var installedFrom: String,
    
    var isInPlexusData: Boolean,
    
    var isInstalled: Boolean,
    
    var isFav: Boolean
    
) : Parcelable {
    constructor() : this(id = "",
                         name = "",
                         packageName = "",
                         installedVersion = "",
                         installedFrom = "",
                         isInPlexusData = true,
                         isInstalled = false,
                         isFav = false)
    // Secondary constructor with an empty argument list that initializes the fields with default values.
    // This satisfies the requirement that entities and POJOs must have a usable public constructor.
}