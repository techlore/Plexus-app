/*
 * Copyright (c) 2022 Techlore
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
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    companion object {
        // Shared pref keys
        const val THEME_PREF = "theme"
        const val A_Z_SORT_PREF = "a_z_sort"
        const val STATUS_RADIO_PREF = "status_radio"
        const val DG_STATUS_SORT_PREF = "dg_status_sort"
        const val MG_STATUS_SORT_PREF = "mg_status_sort"
        const val FILTER_PREF = "filter_pref"
    }

    init {
        sharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
    }

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun setInt(key: String, integer: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, integer)
        editor.apply()
    }
}