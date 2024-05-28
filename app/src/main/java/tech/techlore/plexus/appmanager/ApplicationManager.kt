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

package tech.techlore.plexus.appmanager

import android.app.Application
import com.google.android.material.color.DynamicColors
import tech.techlore.plexus.api.ApiManager.Companion.apiBuilder
import tech.techlore.plexus.database.MainDatabase.Companion.getDatabase
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MATERIAL_YOU
import tech.techlore.plexus.preferences.PreferenceManager.Companion.THEME
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.UiUtils.Companion.setAppTheme

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
        setAppTheme(preferenceManager.getInt(THEME))
        
        // Material you
        if (preferenceManager.getBoolean(MATERIAL_YOU, defValue = false)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
    
}