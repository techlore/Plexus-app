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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class PlexusData implements Serializable {

    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("package")
    public String packageName;
//    @JsonProperty("Version")
//    public String version;
//    @JsonProperty("DG_Rating")
//    public String dgStatus;
//    @JsonProperty("MG_Rating")
//    public String mgStatus;
//    @JsonProperty("DG_Notes")
//    public String dgNotes;
//    @JsonProperty("MG_Notes")
//    public String mgNotes;

}
