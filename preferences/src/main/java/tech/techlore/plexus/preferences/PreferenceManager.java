package tech.techlore.plexus.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    // SHARED PREF KEYS
    public static final String THEME_PREF="theme";

    public PreferenceManager(Context context){
        sharedPreferences=context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
    }

    public int getInt(String key){
        return sharedPreferences.getInt(key,0);
    }

    public void setInt(String key, int integer){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(key, integer);
        editor.apply();
    }
}
