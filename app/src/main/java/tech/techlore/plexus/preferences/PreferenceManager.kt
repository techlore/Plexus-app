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

package tech.techlore.plexus.preferences

import android.content.Context

class PreferenceManager(context: Context) {
    
    companion object {
        // Shared pref keys
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val THEME = "theme"
        const val MATERIAL_YOU = "material_you"
        const val A_Z_SORT = "a_z_sort"
        const val STATUS_RADIO = "status_radio"
        const val DG_STATUS_SORT = "dg_status_sort"
        const val MG_STATUS_SORT = "mg_status_sort"
        const val INSTALLED_FROM_SORT = "installed_from_sort"
        const val IS_FIRST_SUBMISSION = "is_first_submission"
    }

    private val sharedPreferences =
        context.getSharedPreferences("tech.techlore.plexus_prefs", Context.MODE_PRIVATE)

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun setInt(key: String, value: Int) {
        sharedPreferences.edit().apply {
            putInt(key, value)
            apply()
        }
    }
    
    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, true)
    }
    
    fun getBooleanDefValFalse(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }
    
    fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(key, value)
            apply()
        }
    }
}