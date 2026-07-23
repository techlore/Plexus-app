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
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import tech.techlore.plexus.dao.MainDataDao
import tech.techlore.plexus.dao.MyRatingsDao
import tech.techlore.plexus.models.main.MainData
import tech.techlore.plexus.models.myratings.MyRating

@Database(
    entities = [MainData::class, MyRating::class],
    version = 2,
    exportSchema = false
)
abstract class MainDatabase : RoomDatabase() {
    
    abstract fun mainDataDao(): MainDataDao
    abstract fun myRatingsDao(): MyRatingsDao
    
    companion object {
        
        @Volatile
        private var INSTANCE: MainDatabase? = null
        
        val MIGRATION_1_TO_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE my_ratings_table ADD COLUMN totalRatings INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("UPDATE my_ratings_table SET totalRatings = coalesce(json_array_length(ratingsDetails), 0)")
            }
        }
        
        fun getDatabase(context: Context): MainDatabase {
            return INSTANCE ?: synchronized(this) {
                Room
                    .databaseBuilder(
                        context.applicationContext,
                        MainDatabase::class.java,
                        "main_database.db"
                    )
                    .setDriver(BundledSQLiteDriver())
                    .addMigrations(MIGRATION_1_TO_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
        
    }
}