package com.kindred.kindredprints_android_sdk.adapters;

import java.util.ArrayList;

import com.kindred.kindredprints_android_sdk.R;
import com.kindred.kindredprints_android_sdk.customviews.CheckBoxView;
import com.kindred.kindredprints_android_sdk.data.Address;
import com.kindred.kindredprints_android_sdk.fragments.KindredFragmentHelper;
import com.kindred.kindredprints_android_sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindred.kindredprints_android_sdk.helpers.prefs.DevPrefHelper;
import com.kindred.kindredprints_android_sdk.helpers.prefs.InterfacePrefHelper;
import com.kindred.kindredprints_android_sdk.helpers.prefs.UserPrefHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ShippingAddressAdapter extends BaseAdapter {
	private KindredFragmentHelper fragmentHelper_;
	private InterfacePrefHelper interfacePrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private Activity context_;
	
	private ArrayList<Address> prevSelectedAddresses_;
	private ArrayList<Address> selectedAddresses_;
	private ArrayList<Address> addresses_;
	
	public ShippingAddressAdapter(Activity activity, KindredFragmentHelper fragmentHelper) {
		this.context_ = activity;
		this.interfacePrefHelper_ = new InterfacePrefHelper(activity);
		this.userPrefHelper_ = new UserPrefHelper(activity);
		this.devPrefHelper_ = new DevPrefHelper(activity);
		this.fragmentHelper_ = fragmentHelper;
		this.addresses_ = this.userPrefHelper_.getAllAddresses();
		this.selectedAddresses_ = this.userPrefHelper_.getSelectedAddresses();
		this.prevSelectedAddresses_ = new ArrayList<Address>();
		for (Address addr : this.selectedAddresses_) {
			this.prevSelectedAddresses_.add(addr.copy());
		}
		this.fragmentHelper_.setNextButtonDreamCatcher_(new NextButtonPressInterrupter() {
			@Override
			public boolean interruptNextButton() {
				setNeedUpdateOrderId();
				return false;
			}
		});
		updateNextButtonPendingSelection();
	}
	
	public void updateAddressList(ArrayList<Address> addresses) {
		ArrayList<Address> prevAddresses = new ArrayList<Address>();
		for (Address addr : this.addresses_) {
			prevAddresses.add(addr.copy());
		}
		this.addresses_.clear();
		for (Address addr : addresses) {
			for (Address prevAddr : prevAddresses) {
				if (prevAddr.getAddressId().equals(addr.getAddressId())) {
					addr.setShipMethod(prevAddr.getShipMethod());
				}
			}
			this.addresses_.add(addr);

		}
		for (int i = 0; i < this.selectedAddresses_.size(); i++) {
			Address selAddr = this.selectedAddresses_.get(i);
			boolean found = false;
			for (Address addr : this.addresses_) {
				if (addr.getAddressId().equals(selAddr.getAddressId())) {
					selAddr.setShipMethod(addr.getShipMethod());
					found = true;
					break;
				}
			}
			if (!found) {
				this.selectedAddresses_.remove(i);
				i = Math.max(i-1, 0);
			}
		}
		
		if (this.selectedAddresses_.size() == 0 && this.addresses_.size() > 0) {
			this.selectedAddresses_.add(this.addresses_.get(0));
		}
		
		this.userPrefHelper_.setSelectedShippingAddresses(this.selectedAddresses_);
		this.userPrefHelper_.setAllShippingAddresses(this.addresses_);
		updateNextButtonPendingSelection();
		this.notifyDataSetChanged();
	}
	
	private void setNeedUpdateOrderId() {
		if (this.prevSelectedAddresses_.size() == this.selectedAddresses_.size()) {
			for (Address addr : this.prevSelectedAddresses_) {
				boolean found = false;
				
				for (Address cAddr : this.selectedAddresses_) {
					if (addr.getAddressId().equals(cAddr.getAddressId())) {
						found = true;
						break;
					}
				}
				
				if (!found) {
					this.devPrefHelper_.setNeedUpdateOrderId(true);
					break;
				}
			}
			
		} else {
			this.devPrefHelper_.setNeedUpdateOrderId(true);
		}
	}
	
	@Override
	public int getCount() {
		return this.addresses_.size();
	}

	@Override
	public Object getItem(int position) {
		return this.addresses_.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private void addIndexToSelected(int index) {
		Address addr = this.addresses_.get(index);
		this.selectedAddresses_.add(addr);
		this.userPrefHelper_.setSelectedShippingAddresses(this.selectedAddresses_);
	}
	
	private void removeIndexFromSelected(int index) {
		Address addr = this.addresses_.get(index);
		for (int i = 0; i < this.selectedAddresses_.size(); i++) {
			if (addr.getAddressId().equals(this.selectedAddresses_.get(i).getAddressId())) {
				this.selectedAddresses_.remove(i);
				this.userPrefHelper_.setSelectedShippingAddresses(this.selectedAddresses_);
				return;
			}
		}
	}
	
	private boolean isAddressSelected(int position) {
		String id = this.addresses_.get(position).getAddressId();
		for (Address addr : this.selectedAddresses_) {
			if (addr.getAddressId().equals(id)) {
				return true;
			}
		}
		
		return false;
	}
	
	private void startEditOfAddress(int position) {
		Address currAddress = this.addresses_.get(position);
		Bundle bun = new Bundle();
		bun.putString("address", currAddress.packAddress());
		fragmentHelper_.moveToFragmentWithBundle(KindredFragmentHelper.FRAG_SHIPPING_EDIT, bun);
	}
	
	private void updateNextButtonPendingSelection() {
		if (selectedAddresses_.size() > 0) {
			fragmentHelper_.setNextButtonEnabled(true);
		} else {
			fragmentHelper_.setNextButtonEnabled(false);
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View shippingView = null;		
		if (convertView == null) {
			LayoutInflater inflater = this.context_.getLayoutInflater();
			shippingView = inflater.inflate(R.layout.shipping_list_view, null, true);		
		} else {
			shippingView =  convertView;
		}
				
		Address currAddress = this.addresses_.get(position);
		
		Button cmdEdit = (Button) shippingView.findViewById(R.id.cmdEdit);
		TextView txtTitle = (TextView) shippingView.findViewById(R.id.txtTitle);
		TextView txtSubtitle = (TextView) shippingView.findViewById(R.id.txtSubtitle);
		final CheckBoxView chkBoxView = (CheckBoxView) shippingView.findViewById(R.id.chkBoxChecked);
		
		cmdEdit.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		txtTitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtSubtitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		txtTitle.setText(currAddress.getName());
		txtSubtitle.setText(currAddress.getStreet() + ", " + currAddress.getCity() + ", " + currAddress.getCountry());
		
		chkBoxView.setChecked(isAddressSelected(position));
		
		shippingView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isAddressSelected(position)) {
					removeIndexFromSelected(position);
					chkBoxView.setChecked(false);
				} else {
					addIndexToSelected(position);
					chkBoxView.setChecked(true);
				}
				updateNextButtonPendingSelection();
			}
		});
		
		cmdEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startEditOfAddress(position);
			}
		});
		
		return shippingView;
	}

}
