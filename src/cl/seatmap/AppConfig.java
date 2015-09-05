package cl.seatmap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 
 * @author philiptrannp
 *
 */
public class AppConfig {
    private static final String PREF_FILE = "CLSeatMapPrefs";
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;
    private String key_isDatabaseInitialized = "isDatabaseInitialized";

    public AppConfig(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public boolean isDatabaseInitialized() {
        return appSharedPrefs.getBoolean(key_isDatabaseInitialized, false);
    }

    public void setDatabaseInitialized() {
        prefsEditor.putBoolean(key_isDatabaseInitialized, true).commit();
    }
}
