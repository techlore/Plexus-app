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

    public boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }

    public void setBoolean(String key, boolean bool){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(key, bool);
        editor.apply();
    }

    public int getInt(String key){
        return sharedPreferences.getInt(key,0);
    }

    public void setInt(String key, int integer){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(key, integer);
        editor.apply();
    }

    public String getString(String key){
        return sharedPreferences.getString(key, "descending");
    }

    public void setString(String key, String value){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

}
