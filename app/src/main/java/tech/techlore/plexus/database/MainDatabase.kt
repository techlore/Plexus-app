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

package tech.techlore.plexus.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.models.main.MainData

@Database(entities = [MainData::class], version = 1)
abstract class MainDatabase : RoomDatabase() {
    
    abstract fun mainDataDao(): MainDataDao
    
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