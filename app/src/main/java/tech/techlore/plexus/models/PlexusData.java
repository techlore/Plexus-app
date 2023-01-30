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

package tech.techlore.plexus.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlexusData implements Parcelable {
    
    public PlexusData() {}
    
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("package")
    public String packageName;
    /*@JsonProperty("Version")
    public String version;
    @JsonProperty("DG_Rating")
    public String dgStatus;
    @JsonProperty("MG_Rating")
    public String mgStatus;
    @JsonProperty("DG_Notes")
    public String dgNotes;
    @JsonProperty("MG_Notes")
    public String mgNotes;*/
    
    protected PlexusData(Parcel in) {
        id = in.readString();
        name = in.readString();
        packageName = in.readString();
    }
    
    public static final Creator<PlexusData> CREATOR = new Creator<PlexusData>() {
        @Override
        public PlexusData createFromParcel(Parcel in) {
            return new PlexusData(in);
        }
        
        @Override
        public PlexusData[] newArray(int size) {
            return new PlexusData[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
    
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(packageName);
    }

}
