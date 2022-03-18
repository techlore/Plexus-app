package tech.techlore.plexus.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class App implements Serializable {

    @JsonProperty("Application")
    public String name;
    @JsonProperty("Month")
    public String packageName;
    @JsonProperty("Year")
    public String version;
    @JsonProperty("Rating (1-4)")
    public String dgRating;
    @JsonProperty("Notes")
    public String dgNotes;
    @JsonProperty("MicroG Rating (1-4)")
    public String mgRating;
    @JsonProperty("MicroG Notes")
    public String mgNotes;

}
