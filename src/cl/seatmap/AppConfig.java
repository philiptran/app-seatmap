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
	private String key_DBVersion = "DB_VER";

	public AppConfig(Context context) {
		this.appSharedPrefs = context.getSharedPreferences(PREF_FILE,
				Activity.MODE_PRIVATE);
		this.prefsEditor = appSharedPrefs.edit();
	}

	public int getDBVer() {
		return appSharedPrefs.getInt(key_DBVersion, -1);
	}

	public void setDBVer(int version) {
		prefsEditor.putInt(key_DBVersion, version).commit();
	}
}
