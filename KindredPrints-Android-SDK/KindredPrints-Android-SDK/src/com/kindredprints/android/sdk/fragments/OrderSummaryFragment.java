package com.kindredprints.android.sdk.fragments;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.adapters.OrderSummaryAdapter;
import com.kindredprints.android.sdk.adapters.OrderSummaryAdapter.AddressUpdateCallback;
import com.kindredprints.android.sdk.customviews.KindredAlertDialog;
import com.kindredprints.android.sdk.data.LineItem;
import com.kindredprints.android.sdk.data.UserObject;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.BackButtonPressInterrupter;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindredprints.android.sdk.helpers.OrderProcessingHelper;
import com.kindredprints.android.sdk.helpers.OrderProcessingHelper.OrderProcessingUpdateListener;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;
import com.kindredprints.android.sdk.remote.KindredRemoteInterface;
import com.kindredprints.android.sdk.remote.NetworkCallback;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class OrderSummaryFragment extends KindredFragment {
	private static final int NUM_CONFIG_DOWNLOADS = 2;
	
	private Activity activity_;
	
	private KindredFragmentHelper fragmentHelper_;
	private KindredRemoteInterface kindredRemoteInt_;
	private OrderProcessingHelper orderProcessingHelper_;
		
	private DevPrefHelper devPrefHelper_;
	private InterfacePrefHelper interfacePrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private UserObject currUser_;
	
	private MixpanelAPI mixpanel_;
	
	private TextView txtTotal_;
	private Button cmdCompleteOrder_;
	private ListView lvOrderLineItems_;
	private OrderSummaryAdapter lineItemAdapter_;

	private int returnedDownloads_;
	private boolean continueCheck_;
	
	public OrderSummaryFragment() { }
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		this.activity_ = activity;
		
		this.mixpanel_ = MixpanelAPI.getInstance(activity, activity.getResources().getString(R.string.mixpanel_token));
		this.mixpanel_.track("order_summary_page_view", null);
		
		this.kindredRemoteInt_ = new KindredRemoteInterface(activity);
		this.kindredRemoteInt_.setNetworkCallbackListener(new OrderSummaryNetworkCallback());
		
		this.orderProcessingHelper_ = OrderProcessingHelper.getInstance(activity);
		this.orderProcessingHelper_.setOrderProcessingUpdateListener(new OrderCheckoutHelperListener());
		
		this.devPrefHelper_ = new DevPrefHelper(activity);
		this.interfacePrefHelper_ = new InterfacePrefHelper(activity);
		this.userPrefHelper_ = new UserPrefHelper(activity);
		this.currUser_ = this.userPrefHelper_.getUserObject();
		
		this.fragmentHelper_ = fragmentHelper;
		this.fragmentHelper_.setNextButtonDreamCatcher_(new NextButtonHandler());
		this.fragmentHelper_.setBackButtonDreamCatcher_(new BackButtonPressInterrupter() {
			@Override
			public boolean interruptBackButton() {
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
					    Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				return false;
			}
		});
		this.fragmentHelper_.configNavBar();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_order_summary, container, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		this.cmdCompleteOrder_ = (Button) view.findViewById(R.id.cmdCheckout);
		this.cmdCompleteOrder_.setTextColor(this.interfacePrefHelper_.getHighlightTextColor());
		this.cmdCompleteOrder_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mixpanel_.track("order_summary_complete_order", null);
				fragmentHelper_.showProgressBarWithMessage("validating payment..");
				orderProcessingHelper_.initiateCheckoutSequence();
			}
		});
		
		this.txtTotal_ = (TextView) view.findViewById(R.id.txtTotal);
		this.txtTotal_.setTextColor(this.interfacePrefHelper_.getHighlightTextColor());
		
		this.lvOrderLineItems_ = (ListView) view.findViewById(R.id.lvOrderItemList);
		this.lvOrderLineItems_.setBackgroundColor(Color.TRANSPARENT);
		this.lineItemAdapter_ = new OrderSummaryAdapter(getActivity(), this.fragmentHelper_, this.lvOrderLineItems_, this.txtTotal_);
		this.lineItemAdapter_.setEditCartOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fragmentHelper_.moveToFragment(KindredFragmentHelper.FRAG_CART);
			}
		});
		this.lineItemAdapter_.setAddressUpdateCallback(new AddressUpdateCallback() {
			@Override
			public void addressesUpdated() {
				updateCheckoutButtons();
			}
		});
		this.lvOrderLineItems_.setAdapter(lineItemAdapter_);
		
		if (this.devPrefHelper_.needUpdateOrderId()) {
			fragmentHelper_.showProgressBarWithMessage("checking prices..");
			initOrderSummaryPage();
		} else {
			this.returnedDownloads_ = NUM_CONFIG_DOWNLOADS;
			this.lineItemAdapter_.updateLineItemList(this.userPrefHelper_.getLineItems());
		}
		
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		updateCheckoutButtons();
	}
	
	private void initOrderSummaryPage() {
		this.returnedDownloads_ = 0;
		launchCardRefreshCall();
		launchOrderObjectCreateOrUpdate();
	}
	
	private void updateCheckoutButtons() {
		if (this.userPrefHelper_ != null) {
			if (this.userPrefHelper_.getSelectedAddresses().size() == 0) {
				this.fragmentHelper_.setNextButtonEnabled(false);
				this.cmdCompleteOrder_.setEnabled(false);
				this.cmdCompleteOrder_.setBackgroundResource(R.drawable.rounded_filled_grey);
				this.txtTotal_.setBackgroundResource(R.drawable.rounded_filled_grey);
			} else {
				this.fragmentHelper_.setNextButtonEnabled(true);
				this.cmdCompleteOrder_.setEnabled(true);
				this.cmdCompleteOrder_.setBackgroundResource(R.drawable.cmd_rounded_blue_filled_button);
				this.txtTotal_.setBackgroundResource(R.drawable.cmd_rounded_blue_filled_button);
			}
		} 
	}
	
	private void launchCardRefreshCall() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject postObj = new JSONObject();
				try {
					postObj.put("auth_key", currUser_.getAuthKey());
					kindredRemoteInt_.getUserPaymentDetails(postObj, currUser_.getId());
				} catch (JSONException e) {
					Log.i(getClass().getSimpleName(), "JSON exception: " + e.getMessage());
				}
			}
		}).start();
	}
	
	private void launchOrderObjectCreateOrUpdate() {
		this.orderProcessingHelper_.initiateOrderCreationOrUpdateSequence();
	}
	
	private void checkConfigDownloadsComplete() {
		if (this.returnedDownloads_ >= NUM_CONFIG_DOWNLOADS) {
			fragmentHelper_.hideProgressBar();
		}
	}
	
	public class OrderCheckoutHelperListener implements OrderProcessingUpdateListener {
		@Override
		public void orderCreatedOrUpdated(ArrayList<LineItem> orderItems) {
			returnedDownloads_ = returnedDownloads_ + 1;
			lineItemAdapter_.updateLineItemList(orderItems);
			devPrefHelper_.setNeedUpdateOrderId(false);
			checkConfigDownloadsComplete();
		}

		@Override
		public void orderProcessingUpdateProgress(float progress, String message) {
			fragmentHelper_.updateProgressBarWithMessage(message);
			fragmentHelper_.updateProgressBarWithProgress(progress);
		}
		
		@Override
		public void orderNeedsPayment() {
			fragmentHelper_.hideProgressBar();
			fragmentHelper_.moveToFragment(KindredFragmentHelper.FRAG_ORDER_CARD_EDIT);
		}
		
		@Override
		public void orderFailedToProcess(String error) {
			fragmentHelper_.hideProgressBar();

			KindredAlertDialog kad = new KindredAlertDialog(activity_, error, false);
			kad.show();
		}

		@Override
		public void orderProcessed() {
			fragmentHelper_.hideProgressBar();

			continueCheck_ = true;
			fragmentHelper_.triggerNextButton();
		}
	}
	
	public class NextButtonHandler implements NextButtonPressInterrupter {
		@Override
		public boolean interruptNextButton() {
			if (!continueCheck_) {
				
				fragmentHelper_.showProgressBarWithMessage("validating payment..");
				orderProcessingHelper_.initiateCheckoutSequence();
				
				JSONObject cardStored = new JSONObject();
				try {
					cardStored.put("card_stored", currUser_.getCreditType());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mixpanel_.track("order_summary_click_next", cardStored);

				
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
					    Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				return true;
			}
			return false;
		}
	}
	
	public class OrderSummaryNetworkCallback implements NetworkCallback {
		@Override
		public void finished(final JSONObject serverResponse) {
			if (serverResponse != null) {
				Handler mainHandler = new Handler(getActivity().getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						try {
							int status = serverResponse.getInt(KindredRemoteInterface.KEY_SERVER_CALL_STATUS_CODE);
							String requestTag = serverResponse.getString(KindredRemoteInterface.KEY_SERVER_CALL_TAG);
							
							if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_USER_PAYMENT)) {
								returnedDownloads_ = returnedDownloads_ + 1;
								if (status == 200) {
									currUser_.setPaymentSaved(serverResponse.getInt("payment_status")==1);
									currUser_.setCreditType(serverResponse.getString("card_type"));
									currUser_.setLastFour(serverResponse.getString("last_four"));
									
									userPrefHelper_.setUserObject(currUser_);
									lineItemAdapter_.updateCreditCardInfo(currUser_);
								}
								checkConfigDownloadsComplete();
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
