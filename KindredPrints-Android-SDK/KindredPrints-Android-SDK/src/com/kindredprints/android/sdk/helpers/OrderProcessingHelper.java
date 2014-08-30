package com.kindredprints.android.sdk.helpers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.data.Address;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.LineItem;
import com.kindredprints.android.sdk.data.PrintableImage;
import com.kindredprints.android.sdk.data.UserObject;
import com.kindredprints.android.sdk.helpers.ImageUploadHelper.ImageUploadCallback;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;
import com.kindredprints.android.sdk.remote.KindredRemoteInterface;
import com.kindredprints.android.sdk.remote.NetworkCallback;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

public class OrderProcessingHelper {
	private static final int MAX_ATTEMPTS = 3;
	
	
	private Activity activity_;
	
	private OrderProcessingUpdateListener callback_;
	
	private UserPrefHelper userPrefHelper_;
	private KindredRemoteInterface kindredRemoteInt_;
	private CartManager cartManager_;
	private UserObject currUser_;
	private ImageUploadHelper imageUploadHelper_;
	
	private int numAttempts_;
	private boolean initialUploadComplete_;
	
	private static OrderProcessingHelper orderHelper_;
	
	
	
	public OrderProcessingHelper(Activity activity) {
		this.activity_ = activity;
		
		this.imageUploadHelper_ = ImageUploadHelper.getInstance(activity);
		this.imageUploadHelper_.setUploadCallback(new ImageCreationCallback());
		
		this.kindredRemoteInt_ = new KindredRemoteInterface(activity);
		this.kindredRemoteInt_.setNetworkCallbackListener(new OrderSummaryNetworkCallback());
		
		this.userPrefHelper_ = new UserPrefHelper(activity);
		this.currUser_ = this.userPrefHelper_.getUserObject();
		this.cartManager_ = CartManager.getInstance(activity);
	}
	
	public static OrderProcessingHelper getInstance(Activity activity) {
		if (orderHelper_ == null) {
			orderHelper_ = new OrderProcessingHelper(activity);
		}
		return orderHelper_;
	}
	
	public void setOrderProcessingUpdateListener(OrderProcessingUpdateListener callback) {
		this.callback_ = callback;
	}
	
	public void initiateCheckoutSequence() {
		this.currUser_ = this.userPrefHelper_.getUserObject();
		if (this.currUser_.isPaymentSaved()) {	
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						JSONObject post = new JSONObject();
						String orderId = userPrefHelper_.getCurrentOrderId();
						post.put("order_id", orderId);
						kindredRemoteInt_.checkoutExistingOrder(post, orderId);
					} catch (JSONException ex) {
						ex.printStackTrace();
					}
				}
			}).start();
		} else {
			this.callback_.orderNeedsPayment();
		}
	}
	
	public void initiateOrderCreationOrUpdateSequence() {
		this.initialUploadComplete_ = false;
		this.imageUploadHelper_.validateAllOrdersInit();
	}
	
	private void createOrUpdateOrderObject() {
		if (this.numAttempts_ >= MAX_ATTEMPTS) {
			if (this.callback_ != null) {
				this.callback_.orderFailedToProcess("Something went wrong and the server did not respond. Please try again.");
			}
			return;
		}
		
		ArrayList<Address> currAddresses = this.userPrefHelper_.getSelectedAddresses();
		ArrayList<PrintableImage> currItems = this.cartManager_.getSelectedOrderImages();
		String orderId = this.userPrefHelper_.getCurrentOrderId();
		JSONObject postObj = new JSONObject();
		JSONArray destinationsObj = new JSONArray();
		JSONArray itemsObj = new JSONArray();
		
		try {
			for (PrintableImage pImage :currItems) {
				itemsObj.put(pImage.getServerLineItemId());
			}
			postObj.put("lineitem_ids", itemsObj);
			for (Address addr : currAddresses) {
				JSONObject jsonAddr = new JSONObject();
				jsonAddr.put("address_id", addr.getAddressId());
				if (!addr.getShipMethod().equals(Address.ADDRESS_VALUE_NONE)) {
					jsonAddr.put("ship_method", addr.getShipMethod());
				}
				destinationsObj.put(jsonAddr);
			}
			postObj.put("destinations", destinationsObj);
			if (orderId.equals(UserPrefHelper.NO_STRING_VALUE)) {
				postObj.put("user_id", currUser_.getId());
				this.kindredRemoteInt_.createOrderObject(postObj);
			} else {
				postObj.put("id", orderId);
				this.kindredRemoteInt_.updateOrderObject(postObj, orderId);
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
	}
	
	private ArrayList<LineItem> generateLineItemList(JSONArray list) {
		ArrayList<LineItem> liList = new ArrayList<LineItem>();
		ArrayList<Address> selAddresses = this.userPrefHelper_.getSelectedAddresses();
		try {
			for (int i = 0; i < list.length(); i++) {
				JSONObject listItem = list.getJSONObject(i);
				String type = listItem.getString("type");
				LineItem lItem = new LineItem();
				lItem.setLiName(listItem.getString("name"));
				lItem.setLiAmount(listItem.getString("amount"));
				if (type.equals(LineItem.ORDER_PRODUCT_LINE_TYPE)) {
					lItem.setLiType(LineItem.ORDER_PRODUCT_LINE_TYPE);
					lItem.setLiQuantity(listItem.getInt("quantity"));
				} else if (type.equals(LineItem.ORDER_SUBTOTAL_LINE_TYPE)) {
					lItem.setLiType(LineItem.ORDER_SUBTOTAL_LINE_TYPE);
				} else if (type.equals(LineItem.ORDER_SHIPPING_LINE_TYPE)) {
					lItem.setLiType(LineItem.ORDER_SHIPPING_LINE_TYPE);
					lItem.setLiAddressId(listItem.getString("address_id"));
					
					for (Address address : selAddresses) {
						if (address.getAddressId().equals(lItem.getLiAddressId())) {
							address.setShipMethod(listItem.getString("ship_method"));
						}
					}
					
					lItem.setLiShipMethod(listItem.getString("ship_method"));
				} else if (type.equals(LineItem.ORDER_CREDITS_LINE_TYPE)) {
					lItem.setLiType(LineItem.ORDER_CREDITS_LINE_TYPE);
				} else if (type.equals(LineItem.ORDER_COUPON_APPLIED_LINE_TYPE)) {
					lItem.setLiType(LineItem.ORDER_COUPON_APPLIED_LINE_TYPE);
				} else if (type.equals(LineItem.ORDER_TOTAL_LINE_TYPE)) {
					lItem.setLiType(LineItem.ORDER_TOTAL_LINE_TYPE);
				}
				liList.add(lItem);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		this.userPrefHelper_.setSelectedShippingAddresses(selAddresses);
		return liList;
	}
	
	public interface OrderProcessingUpdateListener {
		public void orderCreatedOrUpdated(ArrayList<LineItem> orderItems);
		public void orderProcessingUpdateProgress(float progress, String message);
		public void orderFailedToProcess(String error);
		public void orderNeedsPayment();
		public void orderProcessed();
	}
	
	public class ImageCreationCallback implements ImageUploadCallback {
		@Override
		public void uploadsHaveCompleted() {
			if (!initialUploadComplete_) {
				initialUploadComplete_ = true;
				numAttempts_ = 0;
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						createOrUpdateOrderObject();
					}
				}).start();
				
			}
		}
		@Override
		public void uploadFinishedWithOverallProgress(final float progress) {
			Handler mainHandler = new Handler(activity_.getMainLooper());
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					if (callback_ != null) {
						callback_.orderProcessingUpdateProgress(progress, "registering images..");
					}
				}
			});
		}
	}
	
	public class OrderSummaryNetworkCallback implements NetworkCallback {
		@Override
		public void finished(final JSONObject serverResponse) {
			if (serverResponse != null) {
				Handler mainHandler = new Handler(activity_.getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						try {
							int status = serverResponse.getInt(KindredRemoteInterface.KEY_SERVER_CALL_STATUS_CODE);
							String requestTag = serverResponse.getString(KindredRemoteInterface.KEY_SERVER_CALL_TAG);
							
							if (requestTag.equals(KindredRemoteInterface.REQ_TAG_CREATE_ORDER_OBJ) || requestTag.equals(KindredRemoteInterface.REQ_TAG_UPDATE_ORDER_OBJ)) {
								if (status == 200) {
									String orderId = serverResponse.getString("id");
									userPrefHelper_.setCurrentOrderId(orderId);
																	
									ArrayList<LineItem> lineItems = generateLineItemList(serverResponse.getJSONArray("checkout_items"));
									userPrefHelper_.setLineItems(lineItems);
									
									if (callback_ != null) {
										callback_.orderCreatedOrUpdated(lineItems);
									}
								} else {
									numAttempts_ = numAttempts_ + 1;
									createOrUpdateOrderObject();
								}
							} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_CREATE_PAYMENT_OBJ)) {
								if (status == 200) {
									cartManager_.cleanUpCart();
									userPrefHelper_.setSelectedShippingAddresses(new ArrayList<Address>());
									userPrefHelper_.setCurrentOrderId(UserPrefHelper.NO_STRING_VALUE);
									
									if (callback_ != null) {
										Log.i("KindredSDK", "order processed!!");
										callback_.orderProcessed();
									}
								} else {
									if (callback_ != null) {
										callback_.orderFailedToProcess(serverResponse.getString("message"));
									}
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			}
	
		}
	}
}
