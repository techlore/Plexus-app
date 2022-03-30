package tech.techlore.plexus.models;

import java.io.Serializable;

public class InstalledApp implements Serializable {

    public String name, packageName, installedVersion, plexusVersion, dgRating, mgRating, dgNotes, mgNotes;

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

}
