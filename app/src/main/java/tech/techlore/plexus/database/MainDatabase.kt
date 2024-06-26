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

package tech.techlore.plexus.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.dao.MyRatingsDao
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating

@Database(entities = [MainData::class, MyRating::class],
          version = 1,
          exportSchema = false)
abstract class MainDatabase : RoomDatabase() {
    
    abstract fun mainDataDao(): MainDataDao
    abstract fun myRatingsDao(): MyRatingsDao
    
    companion object {
        
        @Volatile
        private var INSTANCE: MainDatabase? = null
        
        fun getDatabase(context: Context): MainDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext,
                                     MainDatabase::class.java,
                                     "main_database.db")
                    .build()
                    .also { INSTANCE = it }
            }
        }
        
    }
}