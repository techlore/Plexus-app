package tech.techlore.plexus.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class PlexusData implements Serializable {

    @JsonProperty("Application")
    public String name;
    @JsonProperty("Package")
    public String packageName;
    @JsonProperty("Version")
    public String version;
    @JsonProperty("DG_Rating")
    public String dgRating;
    @JsonProperty("MG_Rating")
    public String mgRating;
    @JsonProperty("DG_Notes")
    public String dgNotes;
    @JsonProperty("MG_Notes")
    public String mgNotes;
    @JsonProperty("Category")
    public String category;

}
