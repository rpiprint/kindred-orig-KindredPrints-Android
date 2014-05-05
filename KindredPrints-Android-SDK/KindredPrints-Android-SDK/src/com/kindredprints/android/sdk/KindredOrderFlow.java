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
	
	public KindredOrderFlow(Context context) {
		initHelpers(context);
		initLocalConfig();
	}
	public KindredOrderFlow(Context context, String key) {
		initHelpers(context);
		devPrefHelper_.setAppKey(key);
		initLocalConfig();
	}
	
	public void setAppKey(String key) {
		devPrefHelper_.setAppKey(key);
		JSONObject keyObject = new JSONObject();
		try {
			keyObject.put("key", key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.mixpanel_.track("partner_key", keyObject);
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
		UserObject currUser = this.userPrefHelper_.getUserObject();
		
		if (currUser.getId().equals(UserObject.USER_VALUE_NONE)) {
			this.mixpanel_.track("preregister_email", null);
			
			currUser = new UserObject();
			currUser.setEmail(email);
			currUser.setName(name);
			this.userPrefHelper_.setUserObject(currUser);
			
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
						kindredRemoteInterface_.createUser(obj);
					}
				}).start();
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void setNavBarBackgroundColor(int color) {
		this.interfacePrefHelper_.setNavColor(color);
	}
	public void setBaseBackgroundColor(int color) {
		this.interfacePrefHelper_.setBackgroundColor(color);
	}
	public void setTextColor(int color) {
		this.interfacePrefHelper_.setTextColor(color);
	}
	public void setImageBorderColor(int color) {
		this.interfacePrefHelper_.setBorderColor(color);
	}
	public void setImageBorderDisabled(boolean disabled) {
		if (disabled)
			this.interfacePrefHelper_.disableBorder();
		else
			this.interfacePrefHelper_.enableBorder();
	}
	
	// INTERNAL CONFIG
	
	private void initHelpers(Context context) {
		this.mixpanel_ = MixpanelAPI.getInstance(context, context.getResources().getString(R.string.mixpanel_token));
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		this.devPrefHelper_ = new DevPrefHelper(context);
		this.userPrefHelper_ = new UserPrefHelper(context);
		this.kindredRemoteInterface_ = new KindredRemoteInterface(context);
		this.kindredRemoteInterface_.setNetworkCallbackListener(new ConfigServerCallback());
		this.cartManager_ = CartManager.getInstance(context);
	}
	
	private void initLocalConfig() {
		this.asyncConfigRoutineCount_ = 0;
		this.returnedAsyncConfigRoutines_ = 0;
		
		if (this.devPrefHelper_.needDownloadCountries()) {
			this.asyncConfigRoutineCount_ = this.asyncConfigRoutineCount_ + 1;
			new Thread(new Runnable() {
				@Override
				public void run() {
					kindredRemoteInterface_.getCountryList();
				}
			}).start();	
		}
		if (this.devPrefHelper_.needDownloadSizes()) {
			this.asyncConfigRoutineCount_ = this.asyncConfigRoutineCount_ + 1;
			new Thread(new Runnable() {
				@Override
				public void run() {
					kindredRemoteInterface_.getCurrentImageSizes();
				}
			}).start();
		}
		if (this.devPrefHelper_.needPartnerDetails()) {
			this.asyncConfigRoutineCount_ = this.asyncConfigRoutineCount_ + 1;
			new Thread(new Runnable() {
				@Override
				public void run() {
					kindredRemoteInterface_.getPartnerDetails();
				}
			}).start();
		}
	}
	
	private void flexAddToCart(KPhoto photo) {
		this.cartManager_.addPartnerImage(photo);
	}
	
	private void flexAddManyToCart(ArrayList<KPhoto> photos) {
		this.cartManager_.addManyPartnerImages(photos);
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
									this.interfacePrefHelper_.getThumbMaxSize(), 
									this.interfacePrefHelper_.getThumbMaxSize()*printProd.getTrimmed().getHeight()/printProd.getTrimmed().getWidth()));
					printProd.setPreviewSize(
							new Size(
									this.interfacePrefHelper_.getPreviewMaxSize(), 
									this.interfacePrefHelper_.getPreviewMaxSize()*printProd.getTrimmed().getHeight()/printProd.getTrimmed().getWidth()));
				} else {
					printProd.setThumbSize(
							new Size(
									this.interfacePrefHelper_.getThumbMaxSize()*printProd.getTrimmed().getHeight()/printProd.getTrimmed().getWidth(), 
									this.interfacePrefHelper_.getThumbMaxSize()));
					printProd.setPreviewSize(
							new Size(
									this.interfacePrefHelper_.getPreviewMaxSize()*printProd.getTrimmed().getHeight()/printProd.getTrimmed().getWidth(), 
									this.interfacePrefHelper_.getPreviewMaxSize()));
				}
				printProducts.add(printProd);
			}
			
			this.devPrefHelper_.setCurrentSizes(printProducts);
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
							
							devPrefHelper_.setCountries(countries);
							devPrefHelper_.resetDownloadCountryStatus();
						}
						returnedAsyncConfigRoutines_ = returnedAsyncConfigRoutines_ + 1;
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_IMAGE_SIZES)) {
						if (status == 200) {
							processReturnedProductTypes(serverResponse);
							devPrefHelper_.resetSizeDownloadStatus();
							cartManager_.updateAllOrdersWithNewSizes();
						}
						returnedAsyncConfigRoutines_ = returnedAsyncConfigRoutines_ + 1;
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_PARTNER_DETAILS)) {
						if (status == 200) {
							JSONObject obj = serverResponse.getJSONObject("partner");
							devPrefHelper_.setPartnerName(obj.getString("name"));
							devPrefHelper_.setPartnerUrl(obj.getString("logo"));
							devPrefHelper_.resetPartnerDetailStatus();
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
			                
							userPrefHelper_.setUserObject(currUser);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
