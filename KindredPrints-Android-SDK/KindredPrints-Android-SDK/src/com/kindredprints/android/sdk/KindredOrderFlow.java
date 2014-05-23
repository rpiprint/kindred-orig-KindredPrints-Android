package com.kindredprints.android.sdk;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import com.kindredprints.android.sdk.data.CartManager;

import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.data.Size;
import com.kindredprints.android.sdk.data.UserObject;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;
import com.kindredprints.android.sdk.remote.KindredRemoteInterface;
import com.kindredprints.android.sdk.remote.NetworkCallback;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class KindredOrderFlow {

	private InterfacePrefHelper interfacePrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private KindredRemoteInterface kindredRemoteInterface_;
	private CartManager cartManager_;
	
	private MixpanelAPI mixpanel_;
	
	private int asyncConfigRoutineCount_;
	private int returnedAsyncConfigRoutines_;
	
	private static KindredOrderFlow orderFlow_;
	
	public KindredOrderFlow() {
		
	}
	
	public KindredOrderFlow(Context context) {
		if (orderFlow_ == null) {
			orderFlow_ = new KindredOrderFlow();
			initHelpers(context);
			initLocalConfig();
		}	
	}
	public KindredOrderFlow(Context context, String key) {
		if (orderFlow_ == null) {
			orderFlow_ = new KindredOrderFlow();
			initHelpers(context);
			orderFlow_.devPrefHelper_.setAppKey(key);
			initLocalConfig();
		} else {
			orderFlow_.devPrefHelper_.setAppKey(key);
		}
	}
	
	public void setAppKey(String key) {
		orderFlow_.devPrefHelper_.setAppKey(key);
		JSONObject keyObject = new JSONObject();
		try {
			keyObject.put("key", key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		orderFlow_.mixpanel_.track("partner_key", keyObject);
	}
	
	// CART MANAGEMENT
	
	public void addImageToCart(KPhoto photo) {
		flexAddToCart(photo);
	}
	public void addImagesToCart(ArrayList<KPhoto> photos) {
		flexAddManyToCart(photos);
	}
	public ArrayList<KPhoto> getCurrentCartImages() {
		return getPartnerImageList();
	}
	public boolean removePhotoFromCart(KPhoto photo) {
		return flexDeleteFromCart(photo);
	}
	
	// OPTIONAL CONFIG
	
	public void preRegisterEmail(String email) {
		preRegisterEmail(email, "a Kindred user");
	}
	public void preRegisterEmail(String email, String name) {
		UserObject currUser = orderFlow_.userPrefHelper_.getUserObject();
		
		if (currUser.getId().equals(UserObject.USER_VALUE_NONE)) {
			orderFlow_.mixpanel_.track("preregister_email", null);
			
			currUser = new UserObject();
			currUser.setEmail(email);
			currUser.setName(name);
			orderFlow_.userPrefHelper_.setUserObject(currUser);
			
			final JSONObject obj = new JSONObject();
			try {
				obj.put("name", name);
				obj.put("email", email);
				obj.put("os", "ios");
				obj.put("sdk", true);
				obj.put("send_welcome", true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						orderFlow_.kindredRemoteInterface_.createUser(obj);
					}
				}).start();
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void setNavBarBackgroundColor(int color) {
		orderFlow_.interfacePrefHelper_.setNavColor(color);
	}
	public void setBaseBackgroundColor(int color) {
		orderFlow_.interfacePrefHelper_.setBackgroundColor(color);
	}
	public void setTextColor(int color) {
		orderFlow_.interfacePrefHelper_.setTextColor(color);
	}
	public void setImageBorderColor(int color) {
		orderFlow_.interfacePrefHelper_.setBorderColor(color);
	}
	public void setImageBorderDisabled(boolean disabled) {
		if (disabled)
			orderFlow_.interfacePrefHelper_.disableBorder();
		else
			orderFlow_.interfacePrefHelper_.enableBorder();
	}
	
	// INTERNAL CONFIG
	
	private void initHelpers(Context context) {
		orderFlow_.mixpanel_ = MixpanelAPI.getInstance(context, context.getResources().getString(R.string.mixpanel_token));
		orderFlow_.interfacePrefHelper_ = new InterfacePrefHelper(context);
		orderFlow_.devPrefHelper_ = new DevPrefHelper(context);
		orderFlow_.userPrefHelper_ = new UserPrefHelper(context);
		orderFlow_.kindredRemoteInterface_ = new KindredRemoteInterface(context);
		orderFlow_.kindredRemoteInterface_.setNetworkCallbackListener(new ConfigServerCallback());
		orderFlow_.cartManager_ = CartManager.getInstance(context);
	}
	
	private void initLocalConfig() {
		orderFlow_.asyncConfigRoutineCount_ = 0;
		orderFlow_.returnedAsyncConfigRoutines_ = 0;
		
		if (orderFlow_.devPrefHelper_.needDownloadCountries()) {
			orderFlow_.asyncConfigRoutineCount_ = orderFlow_.asyncConfigRoutineCount_ + 1;
			new Thread(new Runnable() {
				@Override
				public void run() {
					orderFlow_.kindredRemoteInterface_.getCountryList();
				}
			}).start();	
		}
		if (orderFlow_.devPrefHelper_.needDownloadSizes()) {
			orderFlow_.asyncConfigRoutineCount_ = orderFlow_.asyncConfigRoutineCount_ + 1;
			new Thread(new Runnable() {
				@Override
				public void run() {
					orderFlow_.kindredRemoteInterface_.getCurrentImageSizes();
				}
			}).start();
		}
		if (orderFlow_.devPrefHelper_.needPartnerDetails()) {
			orderFlow_.asyncConfigRoutineCount_ = orderFlow_.asyncConfigRoutineCount_ + 1;
			new Thread(new Runnable() {
				@Override
				public void run() {
					orderFlow_.kindredRemoteInterface_.getPartnerDetails();
				}
			}).start();
		}
	}
	
	private void flexAddToCart(KPhoto photo) {
		orderFlow_.cartManager_.addPartnerImage(photo);
	}
	
	private void flexAddManyToCart(ArrayList<KPhoto> photos) {
		orderFlow_.cartManager_.addManyPartnerImages(photos);
	}
	
	private boolean flexDeleteFromCart(KPhoto photo) {
		// TODO add delete
		return false;
	}
	
	private ArrayList<KPhoto> getPartnerImageList() {
		// TODO build interface for partner list
		return new ArrayList<KPhoto>();
	}

	private void processReturnedProductTypes(JSONObject response) {
		try {
			JSONArray jsonPrintProducts = response.getJSONArray("prices");
			ArrayList<PrintProduct> printProducts = new ArrayList<PrintProduct>();
			
			for (int i = 0; i < jsonPrintProducts.length(); i++) {
				JSONObject printObject = jsonPrintProducts.getJSONObject(i);
				PrintProduct printProd = new PrintProduct();
				
				printProd.setId(printObject.getString("id"));
				printProd.setTitle(printObject.getString("title"));
				printProd.setDescription(printObject.getString("description"));
				printProd.setPrice(printObject.getInt("price"));
				printProd.setBorderPerc((float)printObject.getDouble("border_percentage"));
				printProd.setMinDPI(printObject.getInt("min_dpi"));
				printProd.setWarnDPI(printObject.getInt("warn_dpi"));
				printProd.setType(printObject.getString("type"));
				printProd.setTrimmed(
						new Size(
								(float)printObject.getDouble("trim_width"), 
								(float)printObject.getDouble("trim_height")));
				
				if (printProd.getTrimmed().getWidth() >= printProd.getTrimmed().getHeight()) {
					printProd.setThumbSize(
							new Size(
									orderFlow_.interfacePrefHelper_.getThumbMaxSize(), 
									orderFlow_.interfacePrefHelper_.getThumbMaxSize()*printProd.getTrimmed().getHeight()/printProd.getTrimmed().getWidth()));
					printProd.setPreviewSize(
							new Size(
									orderFlow_.interfacePrefHelper_.getPreviewMaxSize(), 
									orderFlow_.interfacePrefHelper_.getPreviewMaxSize()*printProd.getTrimmed().getHeight()/printProd.getTrimmed().getWidth()));
				} else {
					printProd.setThumbSize(
							new Size(
									orderFlow_.interfacePrefHelper_.getThumbMaxSize()*printProd.getTrimmed().getHeight()/printProd.getTrimmed().getWidth(), 
									orderFlow_.interfacePrefHelper_.getThumbMaxSize()));
					printProd.setPreviewSize(
							new Size(
									orderFlow_.interfacePrefHelper_.getPreviewMaxSize()*printProd.getTrimmed().getHeight()/printProd.getTrimmed().getWidth(), 
									orderFlow_.interfacePrefHelper_.getPreviewMaxSize()));
				}
				printProducts.add(printProd);
			}
			
			orderFlow_.devPrefHelper_.setCurrentSizes(printProducts);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public class ConfigServerCallback implements NetworkCallback {
		@Override
		public void finished(JSONObject serverResponse) {
			if (serverResponse != null) {
				try {
					int status = serverResponse.getInt(KindredRemoteInterface.KEY_SERVER_CALL_STATUS_CODE);
					String requestTag = serverResponse.getString(KindredRemoteInterface.KEY_SERVER_CALL_TAG);
					
					if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_COUNTRIES)) {
						if (status == 200) {
							ArrayList<String> countries = new ArrayList<String>();
							JSONArray countryList = serverResponse.getJSONArray("countries");
							for (int i = 0; i < countryList.length(); i++) {
								String country = countryList.getString(i);
								if (country != null && !country.equals("")) {
									countries.add(country);
								}
							}
							
							orderFlow_.devPrefHelper_.setCountries(countries);
							orderFlow_.devPrefHelper_.resetDownloadCountryStatus();
						}
						orderFlow_.returnedAsyncConfigRoutines_ = orderFlow_.returnedAsyncConfigRoutines_ + 1;
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_IMAGE_SIZES)) {
						if (status == 200) {
							processReturnedProductTypes(serverResponse);
							orderFlow_.devPrefHelper_.resetSizeDownloadStatus();
							orderFlow_.cartManager_.updateAllOrdersWithNewSizes();
						}
						orderFlow_.returnedAsyncConfigRoutines_ = orderFlow_.returnedAsyncConfigRoutines_ + 1;
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_PARTNER_DETAILS)) {
						if (status == 200) {
							JSONObject obj = serverResponse.getJSONObject("partner");
							orderFlow_.devPrefHelper_.setPartnerName(obj.getString("name"));
							orderFlow_.devPrefHelper_.setPartnerUrl(obj.getString("logo"));
							orderFlow_.devPrefHelper_.resetPartnerDetailStatus();
							if (obj.has("intro_pages")) {
								JSONArray pages = obj.getJSONArray("intro_pages");
								ArrayList<String> pageList = new ArrayList<String>();
								for (int i = 0; i < pages.length(); i++) {
									pageList.add(pages.getString(i));
								}
								orderFlow_.devPrefHelper_.setIntroUrls(pageList);
								orderFlow_.cartManager_.updateIntroImageUrls(pageList);
							}
						}
						returnedAsyncConfigRoutines_ = returnedAsyncConfigRoutines_ + 1;
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_REGISTER)) {
						if (status == 200) {
							String userId = serverResponse.getString("user_id");
			                String name = serverResponse.getString("name");
			                String email = serverResponse.getString("email");
			                String authKey = serverResponse.getString("auth_key");
			                
			                UserObject currUser = new UserObject();
			                currUser.setId(userId);
			                currUser.setName(name);
			                currUser.setEmail(email);
			                currUser.setAuthKey(authKey);
			                currUser.setPaymentSaved(false);
			                
			                orderFlow_.userPrefHelper_.setUserObject(currUser);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
