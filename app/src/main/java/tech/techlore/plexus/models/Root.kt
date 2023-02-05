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

import com.fasterxml.jackson.annotation.JsonProperty

class Root {
    
    lateinit var data: ArrayList<PlexusData>

    @JsonProperty("page_number")
    var pageNumber = 0

    @JsonProperty("page_size")
    var pageSize = 0

    @JsonProperty("total_entries")
    var totalEntries = 0

    @JsonProperty("total_pages")
    var totalPages = 0
}