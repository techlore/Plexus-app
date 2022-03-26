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

        else if (preferenceManager.getInt(THEME_PREF) == R.id.option_1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.option_2){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.option_3){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

}
