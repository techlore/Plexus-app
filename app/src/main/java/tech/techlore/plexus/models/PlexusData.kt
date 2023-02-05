/*
 * Copyright (c) 2022 Techlore
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

package tech.techlore.plexus.models

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.fasterxml.jackson.annotation.JsonProperty

class PlexusData() : Parcelable {

    @JsonProperty("id")
    lateinit var id: String

    @JsonProperty("name")
    lateinit var name: String

    @JsonProperty("package")
    lateinit var packageName: String

    /*@JsonProperty("Version")
    lateinit var version: String

    @JsonProperty("DG_Rating")
    lateinit var dgStatus: String

    @JsonProperty("MG_Rating")
    lateinit var mgStatus: String

    @JsonProperty("DG_Notes")
    lateinit var dgNotes: String

    @JsonProperty("MG_Notes")
    lateinit var mgNotes: String*/

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        name = parcel.readString()!!
        packageName = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(packageName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<PlexusData> {
        override fun createFromParcel(parcel: Parcel): PlexusData {
            return PlexusData(parcel)
        }

        override fun newArray(size: Int): Array<PlexusData?> {
            return arrayOfNulls(size)
        }
    }

}