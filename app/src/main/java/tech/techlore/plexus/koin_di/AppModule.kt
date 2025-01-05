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

package tech.techlore.plexus.koin_di

import android.app.Application
import coil3.ImageLoader
import coil3.request.crossfade
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.techlore.plexus.api.ApiManager.Companion.apiBuilder
import tech.techlore.plexus.database.MainDatabase
import tech.techlore.plexus.database.MainDatabase.Companion.getDatabase
import tech.techlore.plexus.preferences.EncryptedPreferenceManager
import tech.techlore.plexus.preferences.PreferenceManager
import tech.techlore.plexus.repositories.api.ApiRepository
import tech.techlore.plexus.repositories.database.MainDataMinimalRepository
import tech.techlore.plexus.repositories.database.MainDataRepository
import tech.techlore.plexus.repositories.database.MyRatingsRepository
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx

val appModule =
    module {
        single { PreferenceManager(get()) }
        single { EncryptedPreferenceManager(get()) }
        single { ImageLoader.Builder(get()).crossfade(true).build() }
        single(named("displayedIconSize")) { convertDpToPx(get<Application>(), 55f) }
        single { Json { ignoreUnknownKeys = true } }
        single { apiBuilder() }
        single { ApiRepository(get()) }
        single { getDatabase(get()) }
        single { MainDataRepository(get<MainDatabase>().mainDataDao()) }
        single { MainDataMinimalRepository(get(), get<MainDatabase>().mainDataDao()) }
        single { MyRatingsRepository(get<MainDatabase>().myRatingsDao()) }
    }