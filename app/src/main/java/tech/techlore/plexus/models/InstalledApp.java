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

public class InstalledApp implements Parcelable {

    public String name, packageName, installedVersion, plexusVersion, dgRating, mgRating, dgNotes, mgNotes;
    
    public InstalledApp() {}
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getInstalledVersion() {
        return installedVersion;
    }
    
    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }
    
    public String getPlexusVersion() {
        return plexusVersion;
    }
    
    public void setPlexusVersion(String plexusVersion) {
        this.plexusVersion = plexusVersion;
    }
    
    public String getDgRating() {
        return dgRating;
    }
    
    public void setDgRating(String dgRating) {
        this.dgRating = dgRating;
    }
    
    public String getMgRating() {
        return mgRating;
    }
    
    public void setMgRating(String mgRating) {
        this.mgRating = mgRating;
    }
    
    public String getDgNotes() {
        return dgNotes;
    }
    
    public void setDgNotes(String dgNotes) {
        this.dgNotes = dgNotes;
    }
    
    public String getMgNotes() {
        return mgNotes;
    }
    
    public void setMgNotes(String mgNotes) {
        this.mgNotes = mgNotes;
    }
    
    protected InstalledApp(Parcel in) {
        name = in.readString();
        packageName = in.readString();
        installedVersion = in.readString();
        plexusVersion = in.readString();
        dgRating = in.readString();
        mgRating = in.readString();
        dgNotes = in.readString();
        mgNotes = in.readString();
    }
    
    public static final Creator<InstalledApp> CREATOR = new Creator<InstalledApp>() {
        @Override
        public InstalledApp createFromParcel(Parcel in) {
            return new InstalledApp(in);
        }
        
        @Override
        public InstalledApp[] newArray(int size) {
            return new InstalledApp[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(packageName);
        dest.writeString(installedVersion);
        dest.writeString(plexusVersion);
        dest.writeString(dgRating);
        dest.writeString(mgRating);
        dest.writeString(dgNotes);
        dest.writeString(mgNotes);
    }
}
