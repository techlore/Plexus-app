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

package tech.techlore.plexus.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import tech.techlore.plexus.models.PlexusData

// This custom class modifies json deserialization by Jackson
// because isFav field is not present in the json data
class PlexusDataDeserializer : JsonDeserializer<PlexusData>() {
    
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PlexusData {
        val jsonNode = p?.codec?.readTree<JsonNode>(p)
    
        val id = jsonNode?.get("id")?.asText()
        val name = jsonNode?.get("name")?.asText()
        val packageName = jsonNode?.get("package")?.asText()
    
        return PlexusData(id!!, name!!, packageName!!, false)
    }
}