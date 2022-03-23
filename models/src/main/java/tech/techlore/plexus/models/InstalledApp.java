package tech.techlore.plexus.models;

public class InstalledApp {

    public String name, packageName, version, dgRating, mgRating, dgNotes, mgNotes;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
