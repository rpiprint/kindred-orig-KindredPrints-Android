package com.kindredprints.android.sdk.adapters;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.customviews.DeleteButtonView;
import com.kindredprints.android.sdk.customviews.KindredAlertDialog;
import com.kindredprints.android.sdk.customviews.PlusButtonView;
import com.kindredprints.android.sdk.data.Address;
import com.kindredprints.android.sdk.data.LineItem;
import com.kindredprints.android.sdk.data.UserObject;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper;
import com.kindredprints.android.sdk.helpers.OrderProcessingHelper;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;
import com.kindredprints.android.sdk.remote.KindredRemoteInterface;
import com.kindredprints.android.sdk.remote.NetworkCallback;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
 
public class OrderSummaryAdapter extends BaseAdapter {
	private final static int MAX_NAME_LENGTH = 7;

	private final static int HEADER_ORDER_SUMMARY = 0;
	private final static int HEADER_SHIPPING_SUMMARY = 1;
	private final static int HEADER_PAYMENT_SUMMARY = 2;
	private final static int HEADER_COUPON_SUMMARY = 3;
	
	private KindredFragmentHelper fragmentHelper_;
	private KindredRemoteInterface kindredRemoteInterface_;
	private DevPrefHelper devPrefHelper_;
	private InterfacePrefHelper interfacePrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private Activity context_;
	private UserObject currUser_;
	
	private TextView txtTotal_;
	
	private MixpanelAPI mixpanel_;
	
	private AddressUpdateCallback addressCallback_;
	private OnClickListener editCartClickListener_;
	
	private ArrayList<LineItem> lineItems_;
	private ArrayList<View> printRowViews_;
	private ArrayList<View> couponRowViews_;
	private ArrayList<View> addressRowViews_;
	private ArrayList<View> paymentRowViews_;

	private View rowViewTotal_;
	
	private ListView currParentListView_;
		
	public OrderSummaryAdapter(Activity context, KindredFragmentHelper fragmentHelper, ListView listView, TextView txtTotal) {
		this.context_ = context;
		this.currParentListView_ = listView;
		this.txtTotal_ = txtTotal;
		
		this.mixpanel_ = MixpanelAPI.getInstance(context, context.getResources().getString(R.string.mixpanel_token));
		
		this.kindredRemoteInterface_ = new KindredRemoteInterface(context);
		this.kindredRemoteInterface_.setNetworkCallbackListener(new EditShippingNetworkCallback());
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		this.devPrefHelper_ = new DevPrefHelper(context);
		this.userPrefHelper_ = new UserPrefHelper(context);
		this.fragmentHelper_ = fragmentHelper;
		this.currUser_ = this.userPrefHelper_.getUserObject();
		
		initialInit();
		updateRows();
	}
	
	public void setEditCartOnClickListener(OnClickListener editCartOnClickListener) {
		this.editCartClickListener_ = editCartOnClickListener;
	}
	
	public void setAddressUpdateCallback(AddressUpdateCallback addressUpdated) {
		this.addressCallback_ = addressUpdated;
	}
	
	private void initialInit() {
		this.lineItems_ = new ArrayList<LineItem>();
		this.printRowViews_ = new ArrayList<View>();
		this.couponRowViews_ = new ArrayList<View>();
		this.addressRowViews_ = new ArrayList<View>();
		this.paymentRowViews_ = new ArrayList<View>();
	}
	
	public void updateCreditCardInfo(UserObject updatedUserObject) {
		this.currUser_ = updatedUserObject;
		updateRows();
		this.notifyDataSetChanged();
	}
	
	public void updateCouponRow() {
		updateRows();
		this.notifyDataSetChanged();
	}
	
	private void updateRows() {
		ArrayList<LineItem> printLineItems = new ArrayList<LineItem>();
		ArrayList<LineItem> couponLineItems = new ArrayList<LineItem>();
		ArrayList<LineItem> addressLineItems = new ArrayList<LineItem>();
 		LineItem totalLineItem = null;
		
 		for (LineItem item : this.lineItems_) {
 			if (item.getLiType().equals(LineItem.ORDER_PRODUCT_LINE_TYPE) || item.getLiType().equals(LineItem.ORDER_SUBTOTAL_LINE_TYPE)) {
 				printLineItems.add(item);
 			} else if (item.getLiType().equals(LineItem.ORDER_COUPON_APPLIED_LINE_TYPE) || item.getLiType().equals(LineItem.ORDER_CREDITS_LINE_TYPE)) {
 				couponLineItems.add(item);
 			} else if (item.getLiType().equals(LineItem.ORDER_SHIPPING_LINE_TYPE)) {
 				addressLineItems.add(item);
 			} else if (item.getLiType().equals(LineItem.ORDER_TOTAL_LINE_TYPE)) {
 				totalLineItem = item;
 			}
 		}
 		
		constructPrintRows(printLineItems);
		constructAddressRows(addressLineItems);
		constructCouponRows(couponLineItems);
		constructPaymentRows();
		this.rowViewTotal_ = generateViewForLineItem(totalLineItem);
	}
	
	private void constructPrintRows(ArrayList<LineItem> lineItems) {
		this.printRowViews_.clear();
		this.printRowViews_.add(generateHeaderRowView(HEADER_ORDER_SUMMARY));
		for (LineItem item : lineItems) {
			this.printRowViews_.add(generateViewForLineItem(item));
		}
		this.printRowViews_.add(generateBlankRowView());
	}
	
	private void constructAddressRows(ArrayList<LineItem> lineItems) {
		this.addressRowViews_.clear();
		this.addressRowViews_.add(generateHeaderRowView(HEADER_SHIPPING_SUMMARY));
		for (LineItem item : lineItems) {
			this.addressRowViews_.add(generateViewForLineItem(item));
		}
		this.addressRowViews_.add(generateAddShippingButton());
		this.addressRowViews_.add(generateBlankRowView());
	}
	
	private void constructCouponRows(ArrayList<LineItem> lineItems) {
		this.couponRowViews_.clear();
		this.couponRowViews_.add(generateHeaderRowView(HEADER_COUPON_SUMMARY));
		for (LineItem item : lineItems) {
			this.couponRowViews_.add(generateViewForLineItem(item));
		}
		this.couponRowViews_.add(generateCouponEditView());
		this.couponRowViews_.add(generateBlankRowView());
	}
	
	private void constructPaymentRows() {
		this.paymentRowViews_.clear();
		this.paymentRowViews_.add(generateHeaderRowView(HEADER_PAYMENT_SUMMARY));
		this.paymentRowViews_.add(generateRowViewCreditCard());
		this.paymentRowViews_.add(generateBlankRowView());
	}
	
	public void updateLineItemList(ArrayList<LineItem> lineItems) {
		this.lineItems_ = lineItems;
		updateRows();
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return countOfPrintRows() + countOfShippingRows() + countOfPaymentRows() + countOfCouponRows() + 1;
	}
	
	private int countOfPrintRows() {
		return this.printRowViews_.size();
	}
	
	private int countOfShippingRows() {
		return this.addressRowViews_.size();
	}
	
	private int countOfPaymentRows() {
		return 3;
	}
	
	private int countOfCouponRows() {
		return this.couponRowViews_.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parentView) {
		if (position < this.printRowViews_.size()) {
			return this.printRowViews_.get(position);
		} else if (position < this.printRowViews_.size() + this.addressRowViews_.size()) {
			return this.addressRowViews_.get(position-this.printRowViews_.size());
		} else if (position < this.printRowViews_.size() + this.addressRowViews_.size() + this.paymentRowViews_.size()) {
			return this.paymentRowViews_.get(position-this.printRowViews_.size()-this.addressRowViews_.size());
		} else if (position < this.printRowViews_.size() + this.addressRowViews_.size() + this.paymentRowViews_.size() + this.couponRowViews_.size()) {
			return this.couponRowViews_.get(position-this.printRowViews_.size()-this.addressRowViews_.size()-this.paymentRowViews_.size());
		} else
			return this.rowViewTotal_;
	}

	private View generateViewForLineItem(LineItem item) {
		View view = null;
		
		if (item != null) {
			if (item.getLiType().equals(LineItem.ORDER_PRODUCT_LINE_TYPE)) {
				view = generateProductLineItemView(item);
			} else if (item.getLiType().equals(LineItem.ORDER_SUBTOTAL_LINE_TYPE)) {
				view = generateSubtotalLineItemView(item);
			} else if (item.getLiType().equals(LineItem.ORDER_SHIPPING_LINE_TYPE)) {
				view = generateShippingLineItemView(item);
			} else if (item.getLiType().equals(LineItem.ORDER_CREDITS_LINE_TYPE)) {
				view = generateCreditsLineItemView(item);
			} else if (item.getLiType().equals(LineItem.ORDER_COUPON_APPLIED_LINE_TYPE)) {
				view = generateCouponAppliedLineItemView(item);
			} else if (item.getLiType().equals(LineItem.ORDER_TOTAL_LINE_TYPE)) {
				view = generateTotalLineItemView(item);
			}
		} else {
			view = generateBlankRowView(); 
		}
		
		return view;
	}
	
	private View generateBlankRowView() {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_blank, this.currParentListView_, false);
		
		view.setBackgroundColor(Color.TRANSPARENT);
		
		return view;
	}
	
	private View generateHeaderRowView(int type) {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_header, this.currParentListView_, false);
		
		view.setBackgroundColor(Color.TRANSPARENT);
		Button cmdEdit = (Button) view.findViewById(R.id.cmdEdit);
		TextView txtHeader = (TextView) view.findViewById(R.id.txtHeaderTitle);
		txtHeader.setTextColor(this.interfacePrefHelper_.getTextColor());
		cmdEdit.setTextColor(this.interfacePrefHelper_.getTextColor());
		if (type == HEADER_ORDER_SUMMARY) {
			txtHeader.setText(this.context_.getResources().getString(R.string.order_summary_title));
			cmdEdit.setVisibility(View.VISIBLE);
			cmdEdit.setOnClickListener(this.editCartClickListener_);
		} else if (type == HEADER_SHIPPING_SUMMARY) {
			txtHeader.setText(this.context_.getResources().getString(R.string.order_summary_shipping_header));
			cmdEdit.setVisibility(View.INVISIBLE);
		} else if (type == HEADER_PAYMENT_SUMMARY) {
			txtHeader.setText(this.context_.getResources().getString(R.string.order_summary_payment));
			cmdEdit.setVisibility(View.INVISIBLE);
		} else if (type == HEADER_COUPON_SUMMARY) {
			txtHeader.setText(this.context_.getResources().getString(R.string.order_summary_coupon_header));
			cmdEdit.setVisibility(View.INVISIBLE);
		}
		
		return view;
	}
	
	private View generateProductLineItemView(LineItem item) {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_lineitem, this.currParentListView_, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
		
		TextView txtItemQuantity = (TextView) view.findViewById(R.id.txtLineItemQuantity);
		txtItemQuantity.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtItemQuantity.setText(String.valueOf(item.getLiQuantity()));
		txtItemQuantity.setBackgroundColor(Color.TRANSPARENT);
		
		TextView txtItemDescription = (TextView) view.findViewById(R.id.txtLineItemDescription);
		txtItemDescription.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtItemDescription.setText(item.getLiName());
		
		TextView txtItemTotal = (TextView) view.findViewById(R.id.txtLineItemTotal);
		txtItemTotal.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtItemTotal.setText(item.getLiAmount());
		txtItemTotal.setBackgroundColor(Color.TRANSPARENT);
		
		return view;
	}
	
	private View generateSubtotalLineItemView(LineItem item) {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_subtotal, this.currParentListView_, false);
		
		view.setBackgroundColor(Color.TRANSPARENT);
		
		TextView txtSubtotalName = (TextView) view.findViewById(R.id.txtSubtotalDescription);
		txtSubtotalName.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtSubtotalName.setBackgroundColor(Color.TRANSPARENT);
		txtSubtotalName.setText(item.getLiName());

		TextView txtSubtotal = (TextView) view.findViewById(R.id.txtSubtotalTotal);
		txtSubtotal.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtSubtotal.setText(item.getLiAmount());
		txtSubtotal.setBackgroundColor(Color.TRANSPARENT);
		
		return view;
	}
	
	private View generateAddShippingButton() {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_add_shipping, this.currParentListView_, false);
		
		if (this.addressCallback_ != null) addressCallback_.addressesUpdated();
		
		PlusButtonView cmdAdd = (PlusButtonView) view.findViewById(R.id.cmdAddShipping);
		TextView txtTitle = (TextView) view.findViewById(R.id.txtShippingDescription);
		
		if (this.userPrefHelper_.getSelectedAddresses().size() > 0) {
			view.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
			txtTitle.setTextColor(this.interfacePrefHelper_.getTextColor());
			cmdAdd.updatePaints(this.interfacePrefHelper_.getTextColor());
		} else {
			view.setBackgroundColor(this.context_.getResources().getColor(R.color.color_blue_highlight));
			txtTitle.setTextColor(this.interfacePrefHelper_.getHighlightTextColor());
			cmdAdd.updatePaints(this.interfacePrefHelper_.getHighlightTextColor());
		}
		
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (userPrefHelper_.getAllAddresses().size() == 0) {
					Bundle bun = new Bundle();
					bun.putBoolean("return_to_order", true);
					fragmentHelper_.moveToFragmentWithBundle(KindredFragmentHelper.FRAG_SHIPPING_EDIT, bun);
				} else {
					fragmentHelper_.moveToFragment(KindredFragmentHelper.FRAG_SHIPPING);
				}
			}
		});
		
		return view;
	}
	
	private View generateShippingLineItemView(final LineItem item) {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_shipping, this.currParentListView_, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
		
		Button cmdEditShipping = (Button) view.findViewById(R.id.cmdEditShipping);
		cmdEditShipping.setTextColor(this.interfacePrefHelper_.getTextColor());
		cmdEditShipping.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				fragmentHelper_.showProgressBarWithMessage("quoting shipment prices..");
				mixpanel_.track("order_summary_edit_shipping", null);

				new Thread(new Runnable() {
					@Override
					public void run() {
						kindredRemoteInterface_.getShipQuoteFor(userPrefHelper_.getCurrentOrderId(), item.getLiAddressId());
					}
				}).start();
			}
		});
		
		DeleteButtonView cmdDeleteShipping = (DeleteButtonView) view.findViewById(R.id.cmdDelete);
		cmdDeleteShipping.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				fragmentHelper_.showProgressBarWithMessage("removing shipment address..");
				ArrayList<Address> selAddresses = userPrefHelper_.getSelectedAddresses();
				for (int i = 0; i < selAddresses.size(); i++) {
					Address currAddress = selAddresses.get(i);
					if (currAddress.getAddressId().equals(item.getLiAddressId())) {
						selAddresses.remove(i);
						break;
					}
				}
				userPrefHelper_.setSelectedShippingAddresses(selAddresses);
				devPrefHelper_.setNeedUpdateOrderId(true);
				OrderProcessingHelper.getInstance(context_).initiateOrderCreationOrUpdateSequence();
			}
		});
		
		String[] splitName = item.getLiName().split(" ");
		String showName = splitName[0];
		if (splitName.length > 2) {
			showName = showName + " " + splitName[1] + " " + splitName[2].substring(0, Math.min(splitName[2].length(), MAX_NAME_LENGTH));
		} 
		
		TextView txtShippingName = (TextView) view.findViewById(R.id.txtShippingDescription);
		txtShippingName.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtShippingName.setText(showName);
		txtShippingName.setBackgroundColor(Color.TRANSPARENT);

		TextView txtShippingTotal = (TextView) view.findViewById(R.id.txtShippingTotal);
		txtShippingTotal.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtShippingTotal.setText(item.getLiAmount());
		txtShippingTotal.setBackgroundColor(Color.TRANSPARENT);
		
		return view;
	}
	
	private View generateCreditsLineItemView(LineItem item) {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_credits, this.currParentListView_, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
		
		TextView txtCreditTitle = (TextView) view.findViewById(R.id.txtCreditTitle);
		txtCreditTitle.setTextColor(this.interfacePrefHelper_.getHighlightColor());
		txtCreditTitle.setText(item.getLiName());
		txtCreditTitle.setBackgroundColor(Color.TRANSPARENT);

		TextView txtCreditTotal = (TextView) view.findViewById(R.id.txtCreditTotal);
		txtCreditTotal.setTextColor(this.interfacePrefHelper_.getHighlightColor());
		txtCreditTotal.setText(item.getLiAmount());
		txtCreditTotal.setBackgroundColor(Color.TRANSPARENT);
		
		return view;
	}
	
	private View generateCouponAppliedLineItemView(LineItem item) {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_credits, this.currParentListView_, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
		
		TextView txtCreditTitle = (TextView) view.findViewById(R.id.txtCreditTitle);
		txtCreditTitle.setTextColor(this.interfacePrefHelper_.getHighlightColor());
		txtCreditTitle.setText(item.getLiName());
		txtCreditTitle.setBackgroundColor(Color.TRANSPARENT);

		TextView txtCreditTotal = (TextView) view.findViewById(R.id.txtCreditTotal);
		txtCreditTotal.setTextColor(this.interfacePrefHelper_.getHighlightColor());
		txtCreditTotal.setText(item.getLiAmount());
		txtCreditTotal.setBackgroundColor(Color.TRANSPARENT);
		
		return view;
	}
	
	private View generateCouponEditView() {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		final View view = inflater.inflate(R.layout.order_summary_row_coupons, this.currParentListView_, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
	

		final EditText editTextCoupon = (EditText) view.findViewById(R.id.editTextCoupon);
		editTextCoupon.setTextColor(this.interfacePrefHelper_.getTextColor());
		editTextCoupon.setBackgroundColor(Color.TRANSPARENT);
		
		final Button cmdEditApplyCoupon = (Button) view.findViewById(R.id.cmdEditApplyCoupon);
		cmdEditApplyCoupon.setTextColor(this.interfacePrefHelper_.getTextColor());
		cmdEditApplyCoupon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				InputMethodManager imm = (InputMethodManager)context_.getSystemService(
					      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(context_.getWindow().getDecorView().findViewById(android.R.id.content).getWindowToken(), 0);
				mixpanel_.track("order_summary_apply_coupon", null);

				if (editTextCoupon.getText().length() > 0) {
					fragmentHelper_.showProgressBarWithMessage("checking coupon code..");
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								JSONObject post = new JSONObject();
								post.put("coupon", editTextCoupon.getText().toString());
								kindredRemoteInterface_.applyCouponToOrder(post, userPrefHelper_.getCurrentOrderId(), editTextCoupon.getText().toString());
							} catch (JSONException ex) {
								ex.printStackTrace();
							}
						}
					}).start();	
				} else {
					notifyDataSetChanged();
				}
			}
		});

		cmdEditApplyCoupon.setText("APPLY");
		editTextCoupon.setVisibility(View.VISIBLE);
					
		return view;
	}
	
	private View generateTotalLineItemView(LineItem item) {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View view = inflater.inflate(R.layout.order_summary_row_total, this.currParentListView_, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
		
		TextView txtTotalTitle = (TextView) view.findViewById(R.id.txtTotalTitle);
		txtTotalTitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtTotalTitle.setText(item.getLiName());

		TextView txtTotalTotal = (TextView) view.findViewById(R.id.txtTotalTotal);
		txtTotalTotal.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtTotalTotal.setText(item.getLiAmount());
		this.txtTotal_.setText(item.getLiAmount());
		txtTotalTotal.setBackgroundColor(Color.TRANSPARENT);
		
		this.devPrefHelper_.setOrderTotal(item.getLiAmount());
		
		return view;
	}
	
	
	private View generateRowViewCreditCard() {
		LayoutInflater inflater = this.context_.getLayoutInflater();
		View viewCC = inflater.inflate(R.layout.order_summary_row_card, this.currParentListView_, false);	
	
		viewCC.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
		
		TextView txtTitle = (TextView) viewCC.findViewById(R.id.txtCardTitle);
		txtTitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		TextView txtCard = (TextView) viewCC.findViewById(R.id.txtUserCard);
		txtCard.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		String card = "No card on file";
		if (this.currUser_.isPaymentSaved()) {
			card = this.currUser_.getCreditType() + " XXXX " + this.currUser_.getLastFour();
		}
		txtCard.setText(card);
		
		Button cmdEditCard = (Button) viewCC.findViewById(R.id.cmdEditCard);
		cmdEditCard.setTextColor(this.interfacePrefHelper_.getTextColor());
		cmdEditCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				fragmentHelper_.moveToFragment(KindredFragmentHelper.FRAG_ORDER_CARD_EDIT);
			}
		});
		
		return viewCC;
	}
	
	public void showShippingSelectionDialog(final JSONArray shippingList, final String addressId) {
		LineItem currItem = null;
		for (LineItem item : this.lineItems_) {
			if (item.getLiType().equals(LineItem.ORDER_SHIPPING_LINE_TYPE)) {
				if (item.getLiAddressId().equals(addressId)) {
					currItem = item;
					break;
				}
			}
		}
		
		ArrayList<String> displayMethods = new ArrayList<String>();
		int selIndex = -1;
		try {
			DecimalFormat digitMoneyFormat = new DecimalFormat("$0.00");
			DecimalFormat moneyFormat = new DecimalFormat("$0");
			for (int i = 0; i < shippingList.length(); i++) {
				if (currItem.getLiShipMethod().equals(shippingList.getJSONObject(i).get("name"))) {
					selIndex = i;
				}
				
				String price = "";
				if (shippingList.getJSONObject(i).getInt("price")%100 == 0) {
					price = moneyFormat.format((float)shippingList.getJSONObject(i).getInt("price")/100.0f);
				} else {
					price = digitMoneyFormat.format((float)shippingList.getJSONObject(i).getInt("price")/100.0f);
				}
				
				String methodDesc = shippingList.getJSONObject(i).getString("speed") 
						+ " for "
						+ price;
				displayMethods.add(methodDesc);
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context_);
		builder.setTitle(currItem.getLiName());
		builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int position) {
				fragmentHelper_.showProgressBarWithMessage("updating order..");
				devPrefHelper_.setNeedUpdateOrderId(true);
				OrderProcessingHelper.getInstance(context_).initiateOrderCreationOrUpdateSequence();
			}
		});
		String[] stringArray = new String[displayMethods.size()];
		builder.setSingleChoiceItems((CharSequence[]) displayMethods.toArray(stringArray), selIndex, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ArrayList<Address> selAddresses = userPrefHelper_.getSelectedAddresses();
				try {
					String selMethod = shippingList.getJSONObject(which).getString("name");
					for (Address selAddress : selAddresses) {
						if (selAddress.getAddressId().equals(addressId)) {
							selAddress.setShipMethod(selMethod);
							break;
						}
					}
					userPrefHelper_.setSelectedShippingAddresses(selAddresses);
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
		});
		fragmentHelper_.hideProgressBar();
		builder.show();
	}
	
	public class EditShippingNetworkCallback implements NetworkCallback {
		@Override
		public void finished(final JSONObject serverResponse) {
			if (serverResponse != null) {
				Handler mainHandler = new Handler(context_.getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						try {
							int status = serverResponse.getInt(KindredRemoteInterface.KEY_SERVER_CALL_STATUS_CODE);
							String requestTag = serverResponse.getString(KindredRemoteInterface.KEY_SERVER_CALL_TAG);
							String identTag = serverResponse.getString(KindredRemoteInterface.KEY_SERVER_CALL_IDENT);
							
							if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_SHIP_QUOTE)) {
								if (status == 200) {
									showShippingSelectionDialog(serverResponse.getJSONArray("quotes"), identTag);
								} 
							} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_APPLY_COUPON)) {
								String couponMessage = "";
								if (status == 200) {
									boolean success = serverResponse.getBoolean("success");
									if (success) {
										devPrefHelper_.setNeedUpdateOrderId(true);
										OrderProcessingHelper.getInstance(context_).initiateOrderCreationOrUpdateSequence();
										couponMessage = "The coupon worked!";
									} 
									
								} else {
									fragmentHelper_.hideProgressBar();
									couponMessage = serverResponse.getString("message");
									updateCouponRow();
								}
								KindredAlertDialog kad = new KindredAlertDialog(context_, couponMessage, false);
								kad.show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			}

		}
	}
	
	public interface AddressUpdateCallback {
		public void addressesUpdated();
	}
	
}
