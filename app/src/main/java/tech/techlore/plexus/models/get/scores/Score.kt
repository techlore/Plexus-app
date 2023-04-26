/*
 * Copyright (c) 2022-present Techlore
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

package tech.techlore.plexus.models.get.scores

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class Score(
    
    @JsonProperty("app_package")
    @JsonIgnore
    var dgPkgName: String? = null,
    
    @JsonProperty("denominator")
    @JsonIgnore
    var dgDenominator: Int = 0,
    
    @JsonProperty("google_lib")
    @JsonIgnore
    var dgGoogleLib: String? = null,
    
    @JsonProperty("numerator")
    var score: Float = 0.0f,
    
    @JsonProperty("total_count")
    var totalRatings: Int = 0

)