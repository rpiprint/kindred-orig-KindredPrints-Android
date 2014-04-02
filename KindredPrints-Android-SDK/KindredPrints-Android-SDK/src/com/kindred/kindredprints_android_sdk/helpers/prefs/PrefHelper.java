package com.kindred.kindredprints_android_sdk.helpers.prefs;

import com.kindred.kindredprints_android_sdk.data.Size;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class PrefHelper {
	public static final String NO_STRING_VALUE = "none";
	
	private static final String SHARED_PREF_FILE = "kindred_prints_shared_pref";

	private static PrefHelper prefHelper_;
	private SharedPreferences appSharedPrefs_;
	private Editor prefsEditor_;	
	
	private int screenWidth;
	private int screenHeight;
	
	public PrefHelper() {}
	
	private PrefHelper(Context context) {
		this.appSharedPrefs_ = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
		this.prefsEditor_ = this.appSharedPrefs_.edit();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(displaymetrics);
		this.screenWidth = displaymetrics.widthPixels;
		this.screenHeight = displaymetrics.heightPixels;
	}
	
	public static PrefHelper getInstance(Context context) {
		if (prefHelper_ == null) {
			prefHelper_ = new PrefHelper(context);
		}
		return prefHelper_;
	}
	
	public Size getScreenSize() {
		return new Size(this.screenWidth, this.screenHeight);
	}
	
	public int getInteger(String key) {
		return prefHelper_.appSharedPrefs_.getInt(key, 0);
	}
	public long getLong(String key) {
		return prefHelper_.appSharedPrefs_.getLong(key, 0);
	}
	public float getFloat(String key) {
		return prefHelper_.appSharedPrefs_.getFloat(key, 0);
	}
	public String getString(String key) {
		return prefHelper_.appSharedPrefs_.getString(key, NO_STRING_VALUE);
	}
	public boolean getBool(String key) {
		return prefHelper_.appSharedPrefs_.getBoolean(key, false);
	}
	
	public void setInteger(String key, int value) {
		prefHelper_.prefsEditor_.putInt(key, value);
		prefHelper_.prefsEditor_.commit();
	}
	public void setLong(String key, long value) {
		prefHelper_.prefsEditor_.putLong(key, value);
		prefHelper_.prefsEditor_.commit();
	}
	public void setFloat(String key, float value) {
		prefHelper_.prefsEditor_.putFloat(key, value);
		prefHelper_.prefsEditor_.commit();
	}
	public void setString(String key, String value) {
		prefHelper_.prefsEditor_.putString(key, value);
		prefHelper_.prefsEditor_.commit();
	}
	public void setBool(String key, Boolean value) {
		prefHelper_.prefsEditor_.putBoolean(key, value);
		prefHelper_.prefsEditor_.commit();
	}
}
