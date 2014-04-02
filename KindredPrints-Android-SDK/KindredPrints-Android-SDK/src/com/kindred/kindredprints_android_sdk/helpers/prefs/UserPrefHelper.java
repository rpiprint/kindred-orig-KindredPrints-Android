package com.kindred.kindredprints_android_sdk.helpers.prefs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kindred.kindredprints_android_sdk.data.Address;
import com.kindred.kindredprints_android_sdk.data.CartObject;
import com.kindred.kindredprints_android_sdk.data.LineItem;
import com.kindred.kindredprints_android_sdk.data.PrintableImage;
import com.kindred.kindredprints_android_sdk.data.UserObject;

import android.content.Context;

public class UserPrefHelper extends PrefHelper {
	private static final String KEY_CART_ADDRESSES = "kp_cart_addresses";
	private static final String KEY_ALL_ADDRESSES = "kp_all_addresses";
	private static final String KEY_CART_ORDERS = "kp_cart_orders";
	private static final String KEY_SELECTED_ORDERS = "kp_selected_orders";
	private static final String KEY_LINE_ITEMS = "kp_line_items";
	private static final String KEY_CURRENT_USER = "kp_current_user";
	private static final String KEY_ORDER_ID = "kp_current_order_id";
	
	private PrefHelper prefHelper_;
	
	public UserPrefHelper(Context context) {
		this.prefHelper_ = getInstance(context); 
	}	
	
	public void setCurrentOrderId(String orderId) {
		this.prefHelper_.setString(KEY_ORDER_ID, orderId);
	}
	
	public String getCurrentOrderId() {
		return this.prefHelper_.getString(KEY_ORDER_ID);
	}
	
	public UserObject getUserObject() {
		UserObject userObj;
		String serializedUser = this.prefHelper_.getString(KEY_CURRENT_USER);
		
		if(serializedUser.equals(NO_STRING_VALUE)) {
			userObj = new UserObject();
		} else {
			Type userType = new TypeToken<UserObject>() {}.getType();
			userObj = new Gson().fromJson(serializedUser, userType);
		}
		
		return userObj;
	}
	public void setUserObject(UserObject user) {
		Type userType = new TypeToken<UserObject>() {}.getType();
		String serializedUser = new Gson().toJson(user, userType);
		this.prefHelper_.setString(KEY_CURRENT_USER, serializedUser);
	}

	public void setLineItems(ArrayList<LineItem> lineItems) {
		Type lineItemListType = new TypeToken<ArrayList<LineItem>>() {}.getType();
		String serializedLineItems = new Gson().toJson(lineItems, lineItemListType);
		this.prefHelper_.setString(KEY_LINE_ITEMS, serializedLineItems);
	}
	public ArrayList<LineItem> getLineItems() {
		ArrayList<LineItem> lineItems;
		String serializedArray = this.prefHelper_.getString(KEY_LINE_ITEMS);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			lineItems = new ArrayList<LineItem>();
		} else {
			Type lineItemListType = new TypeToken<ArrayList<LineItem>>() {}.getType();
			lineItems = new Gson().fromJson(serializedArray, lineItemListType);
		}
		
		return lineItems;
	}
	
	public void setSelectedOrders(ArrayList<PrintableImage> orders) {
		Type orderListType = new TypeToken<ArrayList<PrintableImage>>() {}.getType();
		String serializedOrders = new Gson().toJson(orders, orderListType);
		this.prefHelper_.setString(KEY_SELECTED_ORDERS, serializedOrders);
	}
	public ArrayList<PrintableImage> getSelectedOrders() {
		ArrayList<PrintableImage> currOrders;
		String serializedArray = this.prefHelper_.getString(KEY_SELECTED_ORDERS);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			currOrders = new ArrayList<PrintableImage>();
		} else {
			Type printProductListType = new TypeToken<ArrayList<PrintableImage>>() {}.getType();
			currOrders = new Gson().fromJson(serializedArray, printProductListType);
		}
		
		return currOrders;
	}
	public void setAllShippingAddresses(ArrayList<Address> addresses) {
		Type addressListType = new TypeToken<ArrayList<Address>>() {}.getType();
		String serializedOrders = new Gson().toJson(addresses, addressListType);
		this.prefHelper_.setString(KEY_ALL_ADDRESSES, serializedOrders);
	}
	public ArrayList<Address> getAllAddresses() {
		ArrayList<Address> currAddresses;
		String serializedArray = this.prefHelper_.getString(KEY_ALL_ADDRESSES);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			currAddresses = new ArrayList<Address>();
		} else {
			Type addressListType = new TypeToken<ArrayList<Address>>() {}.getType();
			currAddresses = new Gson().fromJson(serializedArray, addressListType);
		}
		
		return currAddresses;
	}
	public void setSelectedShippingAddresses(ArrayList<Address> addresses) {
		Type addressListType = new TypeToken<ArrayList<Address>>() {}.getType();
		String serializedOrders = new Gson().toJson(addresses, addressListType);
		this.prefHelper_.setString(KEY_CART_ADDRESSES, serializedOrders);
	}
	public ArrayList<Address> getSelectedAddresses() {
		ArrayList<Address> currAddresses;
		String serializedArray = this.prefHelper_.getString(KEY_CART_ADDRESSES);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			currAddresses = new ArrayList<Address>();
		} else {
			Type addressListType = new TypeToken<ArrayList<Address>>() {}.getType();
			currAddresses = new Gson().fromJson(serializedArray, addressListType);
		}
		
		return currAddresses;
	}

	public void setCartOrders(ArrayList<CartObject> orders) {
		Type orderListType = new TypeToken<ArrayList<CartObject>>() {}.getType();
		String serializedOrders = new Gson().toJson(orders, orderListType);
		this.prefHelper_.setString(KEY_CART_ORDERS, serializedOrders);
	}
	public ArrayList<CartObject> getCartOrders() {
		ArrayList<CartObject> currOrders;
		String serializedArray = this.prefHelper_.getString(KEY_CART_ORDERS);
		
		if(serializedArray.equals(NO_STRING_VALUE)) {
			currOrders = new ArrayList<CartObject>();
		} else {
			Type addressListType = new TypeToken<ArrayList<CartObject>>() {}.getType();
			currOrders = new Gson().fromJson(serializedArray, addressListType);
		}
		
		return currOrders;
	}

}
