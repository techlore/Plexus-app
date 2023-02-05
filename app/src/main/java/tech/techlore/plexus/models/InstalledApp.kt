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

class InstalledApp() : Parcelable {
    lateinit var name: String
    lateinit var packageName: String
    lateinit var installedVersion: String
    lateinit var plexusVersion: String
    lateinit var dgRating: String
    lateinit var mgRating: String
    lateinit var dgNotes: String
    lateinit var mgNotes: String

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()!!
        packageName = parcel.readString()!!
        installedVersion = parcel.readString()!!
        plexusVersion = parcel.readString()!!
        dgRating = parcel.readString()!!
        mgRating = parcel.readString()!!
        dgNotes = parcel.readString()!!
        mgNotes = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(packageName)
        parcel.writeString(installedVersion)
        parcel.writeString(plexusVersion)
        parcel.writeString(dgRating)
        parcel.writeString(mgRating)
        parcel.writeString(dgNotes)
        parcel.writeString(mgNotes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InstalledApp> {
        override fun createFromParcel(parcel: Parcel): InstalledApp {
            return InstalledApp(parcel)
        }

        override fun newArray(size: Int): Array<InstalledApp?> {
            return arrayOfNulls(size)
        }
    }

}
