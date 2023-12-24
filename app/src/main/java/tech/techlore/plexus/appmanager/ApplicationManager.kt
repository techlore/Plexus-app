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

package tech.techlore.plexus.appmanager

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import tech.techlore.plexus.R
import tech.techlore.plexus.api.ApiManager.Companion.apiBuilder
import tech.techlore.plexus.database.MainDatabase.Companion.getDatabase
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MATERIAL_YOU
import tech.techlore.plexus.preferences.PreferenceManager.Companion.THEME
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository

class ApplicationManager : Application() {
    
    val preferenceManager by lazy { PreferenceManager(this) }
    private val apiService by lazy { apiBuilder() }
    private val database by lazy { getDatabase(this) }
    
    val apiRepository by lazy { ApiRepository(apiService) }
    val mainRepository by lazy { MainDataRepository(database.mainDataDao()) }
    val miniRepository by lazy { MainDataMinimalRepository(this, database.mainDataDao()) }
    val myRatingsRepository by lazy { MyRatingsRepository(database.myRatingsDao()) }
    
    var deviceIsMicroG = false
    var deviceIsDeGoogled = false
    var isDataUpdated = false
    
    override fun onCreate() {
        super.onCreate()

        // Theme
        when (preferenceManager.getInt(THEME)) {

            0 -> {
                if (Build.VERSION.SDK_INT >= 29){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
            R.id.followSystem -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            R.id.light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            R.id.dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        }
        
        // Material you
        if (preferenceManager.getBooleanDefValFalse(MATERIAL_YOU)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
    
}