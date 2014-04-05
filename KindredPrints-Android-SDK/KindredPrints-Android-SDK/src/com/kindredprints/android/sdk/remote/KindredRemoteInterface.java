package com.kindredprints.android.sdk.remote;

import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;

import android.content.Context;

public class KindredRemoteInterface extends RemoteInterface {
	public static final String REQ_TAG_GET_PARTNER_DETAILS = "t_partner_details";
	
	public static final String REQ_TAG_LOGIN = "t_login";
	public static final String REQ_TAG_REGISTER = "t_register";
	public static final String REQ_TAG_PASSWORD_RESET = "t_password_reset";
	public static final String REQ_TAG_STRIPE_REG = "t_reg_stripe";
	public static final String REQ_TAG_NAME_REG = "t_reg_name";

	public static final String REQ_TAG_GET_ADDRESSES = "t_get_addresses";
	public static final String REQ_TAG_CREATE_NEW_ADDRESS = "t_create_address";
	public static final String REQ_TAG_UPDATE_ADDRESS = "t_update_address";
	public static final String REQ_TAG_GET_COUNTRIES = "t_get_countries";
	public static final String REQ_TAG_GET_SHIP_QUOTE = "t_get_ship_quote";

	public static final String REQ_TAG_GET_IMAGE_SIZES = "t_get_image_sizes";

	public static final String REQ_TAG_CREATE_URL_IMAGE = "t_create_url_image";
	public static final String REQ_TAG_CREATE_IMAGE = "t_create_image";
	public static final String REQ_TAG_CHECK_IMAGE_STATUS = "t_check_image_status";
	public static final String REQ_TAG_UPLOAD_IMAGE = "t_upload_image";

	public static final String REQ_TAG_CREATE_PRINTABLE_IMAGE = "t_create_printable_image";
	public static final String REQ_TAG_CREATE_LINE_ITEM = "t_create_line_item";
	public static final String REQ_TAG_UPDATE_LINE_ITEM = "t_update_line_item";
	
	public static final String REQ_TAG_CREATE_ORDER_OBJ = "t_create_order_obj";
	public static final String REQ_TAG_UPDATE_ORDER_OBJ = "t_update_order_obj";
	public static final String REQ_TAG_APPLY_COUPON = "t_get_apply_coupon";
	public static final String REQ_TAG_CREATE_PAYMENT_OBJ = "t_create_payment_obj";
	public static final String REQ_TAG_GET_USER_PAYMENT = "t_get_user_payment";
	
	private static final String REQ_NO_IDENT = "no_ident";
	
	private DevPrefHelper devPrefHelper;
	private NetworkCallback callback;
	
	
	public KindredRemoteInterface() {}
	
	public KindredRemoteInterface(Context context) {
		this.devPrefHelper = new DevPrefHelper(context);
	}
	
	public void setNetworkCallbackListener(NetworkCallback callback) {
		this.callback = callback;
	}
	
	/***********************************************
	 * PARNTER RELATED
	 * **********************************************
	 */
	
	public void getPartnerDetails() {
		String urlExtend = "v1/partner";
		this.callback.finished(make_restful_get(this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_GET_PARTNER_DETAILS, REQ_NO_IDENT, this.devPrefHelper.getAppKey()));
	}
	
	/***********************************************
	 * USER RELATED
	 * **********************************************
	 */
	
	public void loginUser(JSONObject post) {
		String urlExtend = "v0/api/users/login/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_LOGIN, REQ_NO_IDENT, this.devPrefHelper.getAppKey()));
	}
	public void createUser(JSONObject post) {
		String urlExtend = "v0/api/users/create/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_REGISTER, REQ_NO_IDENT, this.devPrefHelper.getAppKey()));
	}
	public void startPasswordReset(JSONObject post) {
		String urlExtend = "v0/api/send/password/reset/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_PASSWORD_RESET, REQ_NO_IDENT, this.devPrefHelper.getAppKey()));
	}
	public void registerStripeToken(JSONObject post, String userId) {
		String urlExtend = "v0/api/users/" + userId + "/register/stripe/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_STRIPE_REG, REQ_NO_IDENT, this.devPrefHelper.getAppKey()));
	}
	public void registerName(JSONObject post, String userId) {
		String urlExtend = "v0/api/users/" + userId + "/register/name/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_NAME_REG, REQ_NO_IDENT, this.devPrefHelper.getAppKey()));
	}

	
	
	/***********************************************
	 * IMAGE RELATED
	 * **********************************************
	 */
	public void getCurrentImageSizes() {
		String urlExtend = "v1/prices";
		this.callback.finished(make_restful_get(
				this.devPrefHelper.getServerAPIUrl() + urlExtend, 
				REQ_TAG_GET_IMAGE_SIZES, 
				REQ_NO_IDENT, 
				this.devPrefHelper.getAppKey())); 
	}
	public void createURLImage(JSONObject post, String ident) {
		String urlExtend = "v1/images";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CREATE_URL_IMAGE, ident, this.devPrefHelper.getAppKey()));
	}
	public void createImage(JSONObject post, String ident) {
		String urlExtend = "v1/images";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CREATE_IMAGE, ident, this.devPrefHelper.getAppKey()));
	}
	public void checkStatusOfImage(JSONObject post, String imageId, String ident) {
		String urlExtend = "v1/images/" + imageId + "/reupload";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CHECK_IMAGE_STATUS, ident, this.devPrefHelper.getAppKey()));
	}
	public void uploadImage(JSONObject post, String imageFileName, String ident) {
		JSONObject paramsObj = new JSONObject();
		String url = "";
		try {
			paramsObj = post.getJSONObject("params");
			url = post.getString("url");
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		this.callback.finished(make_restful_post_with_image(paramsObj, imageFileName, url, REQ_TAG_UPLOAD_IMAGE, ident, null));
	}
	public void createPrintableImage(JSONObject post, String ident) {
		String urlExtend = "v1/printableimages";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CREATE_PRINTABLE_IMAGE, ident, this.devPrefHelper.getAppKey())); 
	}
	public void createLineItem(JSONObject post, String ident) {
		String urlExtend = "v1/lineitems";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CREATE_LINE_ITEM, ident, this.devPrefHelper.getAppKey())); 
	}
	
	public void updateLineItem(JSONObject post, String lineitemId, String ident) {
		String urlExtend = "v1/lineitems/" + lineitemId;
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CREATE_LINE_ITEM, ident, this.devPrefHelper.getAppKey())); 
	}
	
	/***********************************************
	 * ADDRESS RELATED
	 * **********************************************
	 */
	
	public void downloadAllAddresses(JSONObject post, String userId) {
		String urlExtend = "v0/api/users/" + userId + "/addresses/list/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_GET_ADDRESSES, REQ_NO_IDENT, this.devPrefHelper.getAppKey())); 
	}
	public void createNewAddress(JSONObject post, String userId) {
		String urlExtend = "v0/api/users/" + userId + "/addresses/create/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CREATE_NEW_ADDRESS, REQ_NO_IDENT, this.devPrefHelper.getAppKey())); 
	}
	public void updateAddress(JSONObject post, String userId) {
		String urlExtend = "v0/api/users/" + userId + "/addresses/edit/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_UPDATE_ADDRESS, REQ_NO_IDENT, this.devPrefHelper.getAppKey())); 	
	}
	public void getCountryList() {
		String urlExtend = "v1/countries";
		this.callback.finished(make_restful_get(
				this.devPrefHelper.getServerAPIUrl() + urlExtend, 
				REQ_TAG_GET_COUNTRIES, 
				REQ_NO_IDENT, 
				this.devPrefHelper.getAppKey())); 
	}
	
	public void getShipQuoteFor(String orderId, String addressId) {
		String urlExtend = "v1/orders/" + orderId + "/ship-quotes/" + addressId;
		this.callback.finished(make_restful_get(
				this.devPrefHelper.getServerAPIUrl() + urlExtend, 
				REQ_TAG_GET_SHIP_QUOTE, 
				addressId, 
				this.devPrefHelper.getAppKey())); 
	}
	
	/***********************************************
	 * PAYMENT RELATED
	 * **********************************************
	 */
	public void getUserPaymentDetails(JSONObject post, String userId) {
		String urlExtend = "v0/api/users/" + userId + "/get/payment/status/";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_GET_USER_PAYMENT, REQ_NO_IDENT, this.devPrefHelper.getAppKey())); 
	}
	public void createOrderObject(JSONObject post) {
		String urlExtend = "v1/orders";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CREATE_ORDER_OBJ, REQ_NO_IDENT, this.devPrefHelper.getAppKey())); 
	}
	public void updateOrderObject(JSONObject post, String oid) {
		String urlExtend = "v1/orders/" + oid;
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_UPDATE_ORDER_OBJ, REQ_NO_IDENT, this.devPrefHelper.getAppKey())); 
	}
	public void applyCouponToOrder(JSONObject post, String oid, String coupon) {
		String urlExtend = "v1/orders/" + oid + "/coupon";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_APPLY_COUPON, coupon, this.devPrefHelper.getAppKey())); 
	}
	public void checkoutExistingOrder(JSONObject post, String oid) {
		String urlExtend = "v1/orders/" + oid + "/checkout";
		this.callback.finished(make_restful_post(post, this.devPrefHelper.getServerAPIUrl() + urlExtend, REQ_TAG_CREATE_PAYMENT_OBJ, REQ_NO_IDENT, this.devPrefHelper.getAppKey())); 
	}
	
}
