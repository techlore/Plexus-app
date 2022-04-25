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

package tech.techlore.plexus.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    // SHARED PREF KEYS
    public static final String THEME_PREF = "theme";
    public static final String A_Z_SORT_PREF = "a_z_sort";
    public static final String RATING_RADIO_PREF = "rating_radio";
    public static final String DG_RATING_SORT_PREF = "dg_rating_sort";
    public static final String MG_RATING_SORT_PREF = "mg_rating_sort";

    public PreferenceManager(Context context){
        sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
    }

    public int getInt(String key){
        return sharedPreferences.getInt(key,0);
    }

    public void setInt(String key, int integer){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, integer);
        editor.apply();
    }

}
