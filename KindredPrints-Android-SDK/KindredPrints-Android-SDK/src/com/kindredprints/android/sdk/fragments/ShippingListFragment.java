package com.kindredprints.android.sdk.fragments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.adapters.ShippingAddressAdapter;
import com.kindredprints.android.sdk.customviews.PlusButtonView;
import com.kindredprints.android.sdk.data.Address;
import com.kindredprints.android.sdk.data.UserObject;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShippingListFragment extends KindredFragment {
	
	private KindredFragmentHelper fragmentHelper_;
	private KindredRemoteInterface kindredRemoteInt_;
	private InterfacePrefHelper interfacePrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private UserObject currUser_;
	
	private PlusButtonView cmdAdd_;
	private TextView txtAddTitle_;
	private RelativeLayout cmdAddContainer_;
	private ListView lvAddresses_;
	private View viewSeparator_;
	
	private MixpanelAPI mixpanel_;
	
	private ShippingAddressAdapter addressAdapter_;
	
	public ShippingListFragment() { }
	
	public void initFragment(KindredFragmentHelper fragHelper, Activity activity) {
		this.mixpanel_ = MixpanelAPI.getInstance(activity, activity.getResources().getString(R.string.mixpanel_token));
		this.mixpanel_.track("shipping_list_page_view", null);
		this.kindredRemoteInt_ = new KindredRemoteInterface(activity);
		this.kindredRemoteInt_.setNetworkCallbackListener(new ShippingDownloadCallback());
		this.interfacePrefHelper_ = new InterfacePrefHelper(activity);
		this.devPrefHelper_ = new DevPrefHelper(activity);
		this.userPrefHelper_ = new UserPrefHelper(activity);
		this.currUser_ = this.userPrefHelper_.getUserObject();
		this.fragmentHelper_ = fragHelper;
		this.fragmentHelper_.setBackButtonDreamCatcher_(null);
		this.fragmentHelper_.setNextButtonDreamCatcher_(null);
		this.fragmentHelper_.configNavBar();	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_shipping_list, container, false);
				
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		this.cmdAdd_ = (PlusButtonView) view.findViewById(R.id.cmdAdd);
		this.cmdAdd_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bun = new Bundle();
				bun.putBoolean("return_to_order", false);
				fragmentHelper_.moveToFragmentWithBundle(KindredFragmentHelper.FRAG_SHIPPING_EDIT, bun);
			}
		});
		
		this.cmdAddContainer_ = (RelativeLayout) view.findViewById(R.id.addContainer);
		this.cmdAddContainer_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				cmdAdd_.performClick();
			}
		});
		this.txtAddTitle_ = (TextView) view.findViewById(R.id.txtAddTitle);
		this.txtAddTitle_.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		this.viewSeparator_ = (View) view.findViewById(R.id.viewSeparator);
		this.viewSeparator_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		
		this.lvAddresses_ = (ListView) view.findViewById(R.id.lvAddressList);
		this.lvAddresses_.setBackgroundColor(Color.TRANSPARENT);
		this.addressAdapter_ = new ShippingAddressAdapter(getActivity(), this.fragmentHelper_);
		this.lvAddresses_.setAdapter(this.addressAdapter_);
		
		if (this.devPrefHelper_.needDownloadAddresses()) {
			fragmentHelper_.showProgressBarWithMessage("loading past addresses..");
			new Thread(new Runnable() {
				@Override
				public void run() {
					JSONObject postObj = new JSONObject();
					try {
						postObj.put("auth_key", currUser_.getAuthKey());
						kindredRemoteInt_.downloadAllAddresses(postObj, currUser_.getId());
					} catch (JSONException e) {
						Log.i(getClass().getSimpleName(), "JSON exception: " + e.getMessage());
					}
				}
			}).start();
		} else {
			addressAdapter_.updateAddressList(this.userPrefHelper_.getAllAddresses());
		}
		return view;
	}
	
	public class ShippingDownloadCallback implements NetworkCallback {
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
							
							if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_ADDRESSES)) {
								if (status == 200) {
									ArrayList<Address> addresses = new ArrayList<Address>();
									
									JSONArray addressList = serverResponse.getJSONArray("addresses");
									JSONObject obj;
									for (int i = 0; i < addressList.length(); i++) {
										obj = (JSONObject)addressList.get(i);
										
										Address addy = new Address();
										addy.setAddressId(obj.getString("address_id")); 
										addy.setName(obj.getString("name"));
										addy.setStreet(obj.getString("street1"));
										addy.setCity(obj.getString("city"));
										addy.setState(obj.getString("state"));
										addy.setZip(obj.getString("zip"));
										addy.setCountry(obj.getString("country"));
										addy.setEmail(obj.getString("email"));
										addy.setPhone(obj.getString("number"));

										if (addy.getCountry().equals("waiting"))
											addy.setCountry("United States");
																				
										addresses.add(addy);
									}
									
									addressAdapter_.updateAddressList(addresses);
									
									devPrefHelper_.resetAddressDownloadStatus();
								}
							}
							fragmentHelper_.hideProgressBar();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
}
