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
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import tech.techlore.plexus.koin_di.appModule
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MATERIAL_YOU
import tech.techlore.plexus.preferences.PreferenceManager.Companion.THEME
import tech.techlore.plexus.utils.UiUtils.Companion.setAppTheme

class ApplicationManager : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@ApplicationManager)
            modules(appModule)
        }
        
        get<PreferenceManager>().apply {
            // Theme
            setAppTheme(getInt(THEME))
            
            // Material you
            if (getBoolean(MATERIAL_YOU, defValue = false)) {
                DynamicColors.applyToActivitiesIfAvailable(this@ApplicationManager)
            }
        }
        
    }
    
}