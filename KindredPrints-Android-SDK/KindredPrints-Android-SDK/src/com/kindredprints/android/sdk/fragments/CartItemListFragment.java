package com.kindredprints.android.sdk.fragments;

import com.kindredprints.android.sdk.KPhoto;
import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.adapters.CartItemListAdapter;
import com.kindredprints.android.sdk.adapters.CartItemListAdapter.PrintSelectedListener;
import com.kindredprints.android.sdk.customviews.OrderTotalView;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.CartUpdatedCallback;
import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.data.PrintProduct;

import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.BackButtonPressInterrupter;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class CartItemListFragment extends KindredFragment {
	private InterfacePrefHelper interfacePrefHelper_;
	private CartManager cartManager_;
	
	private Context context_;
	
	private KindredFragmentHelper fragmentHelper_;
		
	private CartItemListAdapter itemsAdapter_;
	private Button cmdCheckout_;
	private ListView lvCart_;
	private OrderTotalView orderTotalView_;
	private TextView txtSubtotal_;
		
	public CartItemListFragment() { }
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		this.context_ = activity.getApplicationContext();
		this.cartManager_ = CartManager.getInstance(this.context_);
		this.fragmentHelper_ = fragmentHelper;
		this.fragmentHelper_.setNextButtonDreamCatcher_(new NextButtonHandler());
		this.fragmentHelper_.setBackButtonDreamCatcher_(new BackButtonHandler());
		
		KPhoto incPhoto = this.cartManager_.getPendingImages().get(0);
		PartnerImage pImage = new PartnerImage(incPhoto);
		
		this.cartManager_.setCartUpdatedCallback(new CartUpdatedCallback() {
			@Override
			public void ordersHaveAllBeenUpdated() { 
				if (itemsAdapter_ != null) {
					itemsAdapter_.notifyDataSetInvalidated();
				}
			}
			@Override
			public void orderCountHasBeenUpdated() { }
			@Override
			public void orderHasBeenUpdatedWithSize(PartnerImage obj, ArrayList<PrintProduct> fittedProducts) { 
				if (itemsAdapter_ != null) {
					itemsAdapter_.notifyDataSetInvalidated();
				}
			}

			@Override
			public void orderHasBeenServerInit(PartnerImage obj) { }

			@Override
			public void orderHasBeenUploaded(PartnerImage obj) { }
		});
		this.cartManager_.cacheIncomingImage(pImage, incPhoto);
	}
	
	public class BackButtonHandler implements BackButtonPressInterrupter {
		@Override
		public boolean interruptBackButton() {
			
			return false;
		}
	}
	
	public class NextButtonHandler implements NextButtonPressInterrupter {
		@Override
		public boolean interruptNextButton() {
			JSONObject printCount = new JSONObject();
			try {
				printCount.put("print_count", cartManager_.getSelectedOrderImages().size());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			MixpanelAPI.getInstance(context_, context_.getResources().getString(R.string.mixpanel_token)).track("cart_click_next", printCount);

			return false;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = (ViewGroup) inflater.inflate(R.layout.fragment_cart_preview, container, false);
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(getActivity());
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		this.cmdCheckout_ = (Button) view.findViewById(R.id.cmdCheckout);
		this.orderTotalView_ = (OrderTotalView) view.findViewById(R.id.orderTotal);
		this.lvCart_ = (ListView) view.findViewById(R.id.lvCartItems);
		this.txtSubtotal_ = (TextView) view.findViewById(R.id.txtSubtotal);
		
		int orderTotal = this.cartManager_.getOrderTotal();
		this.orderTotalView_.setOrderTotal(orderTotal);
		
		DecimalFormat moneyFormat = null;
		if (orderTotal % 100 == 0) {
			moneyFormat = new DecimalFormat("$0");
		} else {
			moneyFormat = new DecimalFormat("$0.00");
		}
		float orderTotalF = (float)orderTotal/100.0f;
		this.txtSubtotal_.setText(moneyFormat.format(orderTotalF));

		this.itemsAdapter_ = new CartItemListAdapter((Activity)this.context_);
		this.itemsAdapter_.setPrintClickListener(new PrintSelectedListener() {
			@Override
			public void printWasClicked(int index) {
				
			}
		});
		this.lvCart_.setAdapter(this.itemsAdapter_);
		
		this.cmdCheckout_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
			}
		});
		
		return view;
	}
	
	
}
