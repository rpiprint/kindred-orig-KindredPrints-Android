package com.kindred.kindredprints_android_sdk;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.kindred.kindredprints_android_sdk.data.CartManager;
import com.kindred.kindredprints_android_sdk.data.CartObject;
import com.kindred.kindredprints_android_sdk.data.PartnerImage;
import com.kindred.kindredprints_android_sdk.data.PrintProduct;
import com.kindred.kindredprints_android_sdk.data.Size;
import com.kindred.kindredprints_android_sdk.data.UserObject;
import com.kindred.kindredprints_android_sdk.helpers.cache.ImageManager;
import com.kindred.kindredprints_android_sdk.helpers.prefs.DevPrefHelper;
import com.kindred.kindredprints_android_sdk.helpers.prefs.InterfacePrefHelper;
import com.kindred.kindredprints_android_sdk.helpers.prefs.UserPrefHelper;
import com.kindred.kindredprints_android_sdk.remote.KindredRemoteInterface;
import com.kindred.kindredprints_android_sdk.remote.NetworkCallback;

public class KindredOrderFlow {

	private InterfacePrefHelper interfacePrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private KindredRemoteInterface kindredRemoteInterface_;
	private CartManager cartManager_;
	private ImageManager imManager_;
	
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
	}
	
	// CART MANAGEMENT
	
	public void addImageToCart(KPhoto photo) {
		flexAddToCart(photo);
	}
	public void addImagesToCart(ArrayList<KPhoto> photos) {
		for (KPhoto photo : photos) {
			addImageToCart(photo);
		}
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
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		this.devPrefHelper_ = new DevPrefHelper(context);
		this.userPrefHelper_ = new UserPrefHelper(context);
		this.kindredRemoteInterface_ = new KindredRemoteInterface(context);
		this.kindredRemoteInterface_.setNetworkCallbackListener(new ConfigServerCallback());
		this.imManager_ = ImageManager.getInstance(context);
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
		PartnerImage pImage = new PartnerImage(photo);
		CartObject cartObj = new CartObject();		
		cartObj.setImage(pImage);
		this.cartManager_.addOrderImage(cartObj);
		if (photo instanceof KMEMPhoto) {
			this.imManager_.cacheOrigImageFromMemory(pImage, ((KMEMPhoto)photo).getBm());
		} else if (photo instanceof KLOCPhoto) {
			this.imManager_.cacheOrigImageFromFile(pImage, ((KLOCPhoto)photo).getFilename());
		} else {
			this.imManager_.startPrefetchingOrigImageToCache(pImage);
		}
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
