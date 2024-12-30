/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.objects

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import tech.techlore.plexus.models.myratings.MyRatingDetails

object MyRatingsDetailsConverter : KoinComponent {
    
    @TypeConverter
    fun fromJsonToMyRatingsDetails(ratingsDetailsString: String): List<MyRatingDetails> {
        return get<Json>().decodeFromString (ratingsDetailsString)
    }
    
    @TypeConverter
    fun fromMyRatingsDetailsToJson(ratingsDetails: List<MyRatingDetails>): String {
        return get<Json>().encodeToString(ListSerializer(MyRatingDetails.serializer()), ratingsDetails)
    }
    
}