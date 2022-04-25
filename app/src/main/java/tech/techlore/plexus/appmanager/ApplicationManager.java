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

package tech.techlore.plexus.appmanager;

import static tech.techlore.plexus.preferences.PreferenceManager.THEME_PREF;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import tech.techlore.plexus.R;
import tech.techlore.plexus.preferences.PreferenceManager;

public class ApplicationManager extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager preferenceManager=new PreferenceManager(this);

        // THEME
        if (preferenceManager.getInt(THEME_PREF) == 0){
            if (Build.VERSION.SDK_INT>=29){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        else if (preferenceManager.getInt(THEME_PREF) == R.id.sys_default){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.light){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.dark){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

}
