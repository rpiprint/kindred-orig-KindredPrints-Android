package com.kindredprints.android.sdk.helpers.prefs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.helpers.Base64;

import android.content.Context;

public class DevPrefHelper extends PrefHelper {
	
	public static final Boolean IS_TEST = false;
	
	private static final String SERVER_API_LIVE_URL = "https://api.kindredprints.com/";
	private static final String SERVER_API_DEV_URL = "http://apidev.kindredprints.com/";
	
	private static final String STRIPE_LIVE_KEY = "pk_test_9pMXnrGjrTrJ0mBwflF7lCMK";
	private static final String STRIPE_TEST_KEY = "pk_live_InAYJo4PSgFdffdHorYqNLl9";
	
	
	private static final int PARTNER_DOWNLOAD_INTERVAL = 60*60*24;
	private static final int DOWNLOAD_INTERVAL = 60*60*24*7;
	private static final String KEY_IMAGE_SIZES = "kp_image_sizes";

	private static final String KEY_APP_KEY = "kp_app_key";
	private static final String KEY_DOWNLOAD_IMAGE_SIZE_DATE = "kp_image_size_download";
	
	private static final String KEY_ORDER_TOTAL = "kp_order_total";
	
	private static final String KEY_PARTNER_NAME = "kp_partner_name";
	private static final String KEY_PARTNER_LOGO_URL = "kp_partner_url";
	private static final String KEY_PARTNER_DOWNLOAD_DATE = "kp_partner_download_date";
	
	private static final String KEY_COUNTRIES = "kp_country_list";
	private static final String KEY_COUNTRY_DOWNLOAD_DATE = "kp_country_download_date";
	
	private static final String KEY_ADDRESS_DOWNLOAD_DATE = "kp_address_download_date";

	private static final String KEY_REFRESH_ORDER_ID = "kp_need_refresh_order";
	
	private static final String KEY_FILECACHE_KEYVALUES = "kp_file_cache_keyvalues";
	private static final String KEY_FILECACHE_AGEQUEUE = "kp_file_cache_agequeue";


	private PrefHelper prefHelper_;

	public DevPrefHelper(Context context) {
		this.prefHelper_ = getInstance(context); 
	}

	public String getServerAPIUrl() {
		if (!IS_TEST) {
			return SERVER_API_LIVE_URL;
		} else {
			return SERVER_API_DEV_URL;
		}
	}
	
	public String getStripeKey() {
	    if (!IS_TEST) {
	        return STRIPE_TEST_KEY;
	    } else {
	        return STRIPE_LIVE_KEY;
	    }
	}
	
	public String getPartnerName() {
		return this.prefHelper_.getString(KEY_PARTNER_NAME);
	}
	public void setPartnerName(String name) {
		this.prefHelper_.setString(KEY_PARTNER_NAME, name);
	}
	
	public String getOrderTotal() {
		return this.prefHelper_.getString(KEY_ORDER_TOTAL);
	}
	public void setOrderTotal(String orderTotal) {
		this.prefHelper_.setString(KEY_ORDER_TOTAL, orderTotal);
	}
	
	public String getPartnerUrl() {
		return this.prefHelper_.getString(KEY_PARTNER_LOGO_URL);
	}
	public void setPartnerUrl(String url) {
		this.prefHelper_.setString(KEY_PARTNER_LOGO_URL, url);
	}
	
	public void resetPartnerDetailStatus() {
		this.prefHelper_.setLong(KEY_PARTNER_DOWNLOAD_DATE, Calendar.getInstance().getTimeInMillis());
	}
	public boolean needPartnerDetails() {
  		return checkPastDue(this.prefHelper_.getLong(KEY_PARTNER_DOWNLOAD_DATE), PARTNER_DOWNLOAD_INTERVAL);
	}
	
	public void setAppKey(String key) {
		String fixedUp = key + ":";
		byte[] encodedArray = Base64.encode(fixedUp.getBytes(), Base64.NO_WRAP);
		this.prefHelper_.setString(KEY_APP_KEY, new String(encodedArray));
	}
	public String getAppKey() {
		return this.prefHelper_.getString(KEY_APP_KEY);
	}
	
	public boolean needUpdateOrderId() {
		return this.prefHelper_.getBool(KEY_REFRESH_ORDER_ID);
	}
	public void setNeedUpdateOrderId(boolean need) {
		this.prefHelper_.setBool(KEY_REFRESH_ORDER_ID, need);
	}

	public void setCurrentSizes(ArrayList<PrintProduct> sizes) {
		synchronized (this.prefHelper_) {
			Type printProductListType = new TypeToken<ArrayList<PrintProduct>>() {}.getType();
			String serializedArray = new Gson().toJson(sizes, printProductListType);
			this.prefHelper_.setString(KEY_IMAGE_SIZES, serializedArray);
		}
	}
	public ArrayList<PrintProduct> getCurrentSizes() {
		ArrayList<PrintProduct> currSizes;
		String serializedArray = this.prefHelper_.getString(KEY_IMAGE_SIZES);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			currSizes = new ArrayList<PrintProduct>();
		} else {
			synchronized (this.prefHelper_) {
				Type printProductListType = new TypeToken<ArrayList<PrintProduct>>() {}.getType();
				currSizes = new Gson().fromJson(serializedArray, printProductListType);
			}
		}
		
		return currSizes;
	}

	public void resetAddressDownloadStatus() {
		this.prefHelper_.setLong(KEY_ADDRESS_DOWNLOAD_DATE, Calendar.getInstance().getTimeInMillis());
	}
	public boolean needDownloadAddresses() {
  		return checkPastDue(this.prefHelper_.getLong(KEY_ADDRESS_DOWNLOAD_DATE), DOWNLOAD_INTERVAL);
	}
	public void resetSizeDownloadStatus() {
		this.prefHelper_.setLong(KEY_DOWNLOAD_IMAGE_SIZE_DATE, Calendar.getInstance().getTimeInMillis());
	}
	public boolean needDownloadSizes() {
  		return checkPastDue(this.prefHelper_.getLong(KEY_DOWNLOAD_IMAGE_SIZE_DATE), DOWNLOAD_INTERVAL);
	}
	public void resetDownloadCountryStatus() {
		this.prefHelper_.setLong(KEY_COUNTRY_DOWNLOAD_DATE, Calendar.getInstance().getTimeInMillis());
	}
	public boolean needDownloadCountries() {
  		return checkPastDue(this.prefHelper_.getLong(KEY_COUNTRY_DOWNLOAD_DATE), DOWNLOAD_INTERVAL);
	}

	public ArrayList<String> getCountries() {
		ArrayList<String> currCountries;
		String serializedArray = this.prefHelper_.getString(KEY_COUNTRIES);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			currCountries = new ArrayList<String>();
		} else {
			synchronized (this.prefHelper_) {
				Type countryListType = new TypeToken<ArrayList<String>>() {}.getType();
				currCountries = new Gson().fromJson(serializedArray, countryListType);
			}
		}
		
		return currCountries;
	}
	public void setCountries(ArrayList<String> countries) {
		synchronized (this.prefHelper_) {
			Type countryListType = new TypeToken<ArrayList<String>>() {}.getType();
			String serializedArray = new Gson().toJson(countries, countryListType);
			this.prefHelper_.setString(KEY_COUNTRIES, serializedArray);
		}
	}

	private boolean checkPastDue(long time, long interval) {
		Calendar c = Calendar.getInstance();
		if ((c.getTimeInMillis() - time) > interval*1000) {
  			return true;
  		} else {
  			return false;
  		}
	}
	
	public HashMap<String, String> getFileCacheKV() {
		HashMap<String, String> currMap;
		String serializedArray = this.prefHelper_.getString(KEY_FILECACHE_KEYVALUES);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			currMap = new HashMap<String, String>();
		} else {
			synchronized (this.prefHelper_) {
				Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
				currMap = new Gson().fromJson(serializedArray, mapType);
			}
		}
		
		return currMap;
	}
	public void setFileCacheKV(HashMap<String, String> map) {
		synchronized (this.prefHelper_) {
			Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
			String serializedArray = new Gson().toJson(map, mapType);
			this.prefHelper_.setString(KEY_FILECACHE_KEYVALUES, serializedArray);
		}
	}

	public ArrayList<String> getFileCacheAgeQueue() {
		ArrayList<String> ageQueue;
		String serializedArray = this.prefHelper_.getString(KEY_FILECACHE_AGEQUEUE);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			ageQueue = new ArrayList<String>();
		} else {
			synchronized (this.prefHelper_) {
				Type ageListType = new TypeToken<ArrayList<String>>() {}.getType();
				ageQueue = new Gson().fromJson(serializedArray, ageListType);
			}
		}
		
		return ageQueue;
	}
	public void setFileCacheAgeQueue(ArrayList<String> ageQueue) {
		synchronized (this.prefHelper_) {
			Type ageListType = new TypeToken<ArrayList<String>>() {}.getType();
			String serializedArray = new Gson().toJson(ageQueue, ageListType);
			this.prefHelper_.setString(KEY_FILECACHE_AGEQUEUE, serializedArray);
		}
	}
}
