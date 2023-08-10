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

package tech.techlore.plexus.converters.myratings

import androidx.room.TypeConverter
import tech.techlore.plexus.models.myratings.MyRatingDetails

object MyRatingsDetailsConverter {
    
    @TypeConverter
    fun fromMyRatingsDetails(ratingsDetails: List<MyRatingDetails>): String {
        return ratingsDetails.joinToString(separator = ";") { rating ->
            """${rating.id},
               ${rating.version},
               ${rating.buildNumber},
               ${rating.romName},
               ${rating.romBuild},
               ${rating.androidVersion},
               ${rating.isInstalled},
               ${rating.installedFrom},
               ${rating.googleLib},
               ${rating.myRatingScore},
               ${rating.notes}""".trimIndent()
        }
    }
    
    @TypeConverter
    fun toMyRatingsDetails(ratingsDetailsString: String): List<MyRatingDetails> {
        return ratingsDetailsString.split(";").map { ratingString ->
            val parts = ratingString.split(",")
            MyRatingDetails(id = parts[0],
                            version = parts[1],
                            buildNumber = parts[2].toInt(),
                            romName = parts[3],
                            romBuild = parts[4],
                            androidVersion = parts[5],
                            isInstalled = parts[6].toBoolean(),
                            installedFrom = parts[7],
                            googleLib = parts[8],
                            myRatingScore = parts[9].toInt(),
                            notes = parts[10])
        }
    }
    
}
