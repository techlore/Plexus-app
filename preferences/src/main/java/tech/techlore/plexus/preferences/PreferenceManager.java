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
