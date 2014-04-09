package com.kindredprints.android.sdk.fragments;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.data.Address;
import com.kindredprints.android.sdk.data.UserObject;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.BackButtonPressInterrupter;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;
import com.kindredprints.android.sdk.remote.KindredRemoteInterface;
import com.kindredprints.android.sdk.remote.NetworkCallback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ShippingEditFragment extends KindredFragment {
	public static final int CONTACT_PICKER_RESULT = 1001; 
	
	private static final int STATE_NORMAL = 0;
	private static final int STATE_ERROR = 1;
	
	private KindredFragmentHelper fragmentHelper_;
	private KindredRemoteInterface kindredRemoteInt_;
	private InterfacePrefHelper interfacePrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private UserObject currUser_;
	
	private Address currAddress_;
	
	private Button cmdImport_;
	private EditText editTextName_;
	private EditText editTextStreet_;
	private EditText editTextCity_;
	private EditText editTextState_;
	private EditText editTextZip_;
	private Spinner editSpinnerCountry_;
	private ArrayAdapter<String> dataAdapter_;
	
	private String name_;
	private String street_;
	private String city_;
	private String state_;
	private String zip_;
	private String country_;
	private String email_;
	private String phone_;
	private ArrayList<String> countries_;
	
	private TextView txtError_;
	
	private View viewDiv0_;
	private View viewDiv1_;
	private View viewDiv2_;
	private View viewDiv3_;
	
	private boolean continueBackCheck_;
	private boolean continueCheck_;
	
	public ShippingEditFragment() { }
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		this.fragmentHelper_ = fragmentHelper;
		this.fragmentHelper_.setNextButtonDreamCatcher_(new NextButtonHandler());
		this.fragmentHelper_.setBackButtonDreamCatcher_(new BackButtonHandler());
		this.fragmentHelper_.configNavBar();
		this.fragmentHelper_.setNextButtonEnabled(true);
		
		this.kindredRemoteInt_ = new KindredRemoteInterface(activity);
		this.kindredRemoteInt_.setNetworkCallbackListener(new AddressCreateUpdateCallback());
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(activity);
		this.devPrefHelper_ = new DevPrefHelper(activity);
		this.userPrefHelper_ = new UserPrefHelper(activity);
		
		this.continueBackCheck_ = false;
		this.continueCheck_ = false;
		this.currUser_ = this.userPrefHelper_.getUserObject();
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_shipping_edit, container, false);
		
		this.cmdImport_ = (Button) view.findViewById(R.id.cmdImport);
		this.cmdImport_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.cmdImport_.setOnClickListener(new ImportContactClickResponse());
		if (!checkIfReadContactsPermission())
			this.cmdImport_.setVisibility(View.INVISIBLE);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		this.editTextName_ = (EditText) view.findViewById(R.id.editTextName);
		this.editTextName_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextName_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextName_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN ) {
					editTextStreet_.requestFocus();
				}
				return false;
			}			
		});
		
		this.editTextStreet_ = (EditText) view.findViewById(R.id.editTextStreet);
		this.editTextStreet_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextStreet_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextStreet_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN ) {
					editTextCity_.requestFocus();
				}
				return false;
			}			
		});
		
		this.editTextCity_ = (EditText) view.findViewById(R.id.editTextCity);
		this.editTextCity_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextCity_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextCity_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN ) {
					editTextState_.requestFocus();
				}
				return false;
			}			
		});
		
		this.editTextState_ = (EditText) view.findViewById(R.id.editTextState);
		this.editTextState_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextState_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextState_.addTextChangedListener(new StateInputListener());
		this.editTextState_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN ) {
					Log.i("KindredSDK", "focusing on zi");
					editTextZip_.requestFocus();
				}
				return false;
			}			
		});
		
		this.editTextZip_ = (EditText) view.findViewById(R.id.editTextZip);
		this.editTextZip_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextZip_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextZip_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.FLAG_EDITOR_ACTION) {
					fragmentHelper_.triggerNextButton();
				}
				return false;
			}			
		});
		
		this.editSpinnerCountry_ = (Spinner) view.findViewById(R.id.spinCountry);
		this.editSpinnerCountry_.setBackgroundColor(Color.TRANSPARENT);
		
		String addressStr = getArguments().getString("address");
		if (addressStr != null) {
			this.currAddress_ = new Address(addressStr);
			this.editTextName_.setText(this.currAddress_.getName());
			this.editTextStreet_.setText(this.currAddress_.getStreet());
			this.editTextCity_.setText(this.currAddress_.getCity());
			initCountrySpinner();
			this.editTextState_.setText(this.currAddress_.getState());
			this.editTextZip_.setText(this.currAddress_.getZip());
		} else {
			this.currAddress_ = new Address();
			initCountrySpinner();
		}
				
		this.txtError_ = (TextView) view.findViewById(R.id.txtError);
		this.txtError_.setTextColor(getActivity().getResources().getColor(R.color.color_red));

		this.viewDiv0_ = (View) view.findViewById(R.id.viewDiv0);
		this.viewDiv1_ = (View) view.findViewById(R.id.viewDiv1);
		this.viewDiv2_ = (View) view.findViewById(R.id.viewDiv2);
		this.viewDiv3_ = (View) view.findViewById(R.id.viewDiv3);
		this.viewDiv0_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		this.viewDiv1_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		this.viewDiv2_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		this.viewDiv3_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		
		setInterfaceState(STATE_NORMAL);

		return view;
	}
	
	private void initCountrySpinner() {		
		String country = this.currAddress_.getCountry();
		if (country.equals("waiting") || country.equals("") || country.equals(Address.ADDRESS_VALUE_NONE)) {
			country = "United States";
			this.currAddress_.setCountry("United States");
		}
		
		this.countries_ = this.devPrefHelper_.getCountries();
		int selectedIndex = -1;
		for (int i = 0; i < this.countries_.size(); i++) {
			if (this.countries_.get(i).length() == 0) {
				this.countries_.remove(i);
			} else {
				if (this.countries_.get(i).equals("United States")) {
					selectedIndex = i;
				}
			}
		}
	
		this.dataAdapter_ = new ArrayAdapter<String>(getActivity(), R.layout.simple_spinner_white, this.countries_);
		this.dataAdapter_.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.editSpinnerCountry_.setAdapter(this.dataAdapter_);
		this.editSpinnerCountry_.setSelection(selectedIndex);
	}
	
	private boolean checkIfReadContactsPermission() {
	    String permission = "android.permission.READ_CONTACTS";
	    int res = getActivity().checkCallingOrSelfPermission(permission);
	    return (res == PackageManager.PERMISSION_GRANTED);            
	}
	
	private void setInterfaceState(int state) {
		switch (state) {
			case STATE_NORMAL:
				this.txtError_.setVisibility(View.INVISIBLE);
				break;
			case STATE_ERROR:
				this.txtError_.setVisibility(View.VISIBLE);
				break;
		}
	}
	
	private void closeKeyboard() {
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getView().getWindowToken(), 0);
	}
	
	private boolean checkInputs() {
		if (editTextName_.getText().toString().length() == 0) {
			setInterfaceState(STATE_ERROR);
			this.txtError_.setText("Please enter a name.");
			return false;
		}
	
		if (editTextStreet_.getText().toString().length() == 0) {
			setInterfaceState(STATE_ERROR);
			this.txtError_.setText("Please enter a street.");
			
			return false;
		}
		if (editTextCity_.getText().toString().length() == 0) {
			setInterfaceState(STATE_ERROR);
			this.txtError_.setText("Please enter a city.");
			
			return false;
		}
			
		if (editTextState_.getText().toString().length() == 0) {
			setInterfaceState(STATE_ERROR);
			this.txtError_.setText("Please enter a state.");
			
			return false;
		}
		if (editTextZip_.getText().toString().length() == 0) {
			setInterfaceState(STATE_ERROR);
			this.txtError_.setText("Please enter a zip.");
			
			return false;
		}
		return true;
	}
	
	public class BackButtonHandler implements BackButtonPressInterrupter {
		@Override
		public boolean interruptBackButton() {
			closeKeyboard();
			if (!continueBackCheck_) {
				Bundle bun = new Bundle();
				bun.putBoolean("editback", true);
				continueBackCheck_ = true;
				fragmentHelper_.moveLastFragmentWithBundle(bun);
				return true;
			}
			return false;
		}
	}
	
	public class NextButtonHandler implements NextButtonPressInterrupter {
		@Override
		public boolean interruptNextButton() {
			if (!continueCheck_) {
				fragmentHelper_.showProgressBarWithMessage("updating address book..");
				
				closeKeyboard();
				setInterfaceState(STATE_NORMAL);
				
				if (checkInputs()) {
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							try {
								JSONObject obj = new JSONObject();
								obj.put("auth_key", currUser_.getAuthKey());
								obj.put("name", editTextName_.getText().toString().trim());
								obj.put("street1", editTextStreet_.getText().toString().trim());
								obj.put("street2", "");
								obj.put("city", editTextCity_.getText().toString().trim());
								obj.put("state", editTextState_.getText().toString().trim());
								obj.put("zip", editTextZip_.getText().toString().trim());
								obj.put("country", editSpinnerCountry_.getSelectedItem().toString().trim());
								if (email_ != null) {
									obj.put("email", email_.trim());
								}
								if (phone_ != null) {
									obj.put("number", phone_.trim());
								}
								if (currAddress_.getAddressId().equals(Address.ADDRESS_VALUE_NONE)) {
									kindredRemoteInt_.createNewAddress(obj, currUser_.getId());
								} else {
									obj.put("address_id", currAddress_.getAddressId());
									kindredRemoteInt_.updateAddress(obj, currUser_.getId());
								}
							} catch (JSONException e) {
								
							}
						}
					};
					new Thread(runnable).start();
				}
				return true;
			}
			return false;
		}
	}
	
	public class StateInputListener implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
			if (editSpinnerCountry_.getSelectedItem().toString().equals("United States")) {
				if (s.length() > 2) {
					s = s.delete(s.length()-1, s.length());
		        }
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) { }
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) { }
	}
	
	public class AddressCreateUpdateCallback implements NetworkCallback {
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
							
							fragmentHelper_.hideProgressBar();
							
							if (requestTag.equals(KindredRemoteInterface.REQ_TAG_UPDATE_ADDRESS) || requestTag.equals(KindredRemoteInterface.REQ_TAG_CREATE_NEW_ADDRESS)) {
								if (status == 200) {
									currAddress_.setAddressId(serverResponse.getString("address_id"));
									currAddress_.setName(serverResponse.getString("name"));
									currAddress_.setStreet(serverResponse.getString("street1"));
									currAddress_.setCity(serverResponse.getString("city"));
									currAddress_.setState(serverResponse.getString("state"));
									currAddress_.setZip(serverResponse.getString("zip"));
									currAddress_.setCountry(serverResponse.getString("country"));
									currAddress_.setEmail(serverResponse.getString("email"));
									currAddress_.setPhone(serverResponse.getString("number"));
									
									ArrayList<Address> currAddresses = userPrefHelper_.getAllAddresses();
									boolean alreadyExists = false;
									for (int i = 0; i < currAddresses.size(); i++) {
										Address addr = currAddresses.get(i);
										if (addr.getAddressId().equals(currAddress_.getAddressId())) {
											alreadyExists = true;
											currAddress_.setShipMethod(addr.getShipMethod());
											currAddresses.remove(i);
											currAddresses.add(i, currAddress_);
											break;
										}
									}
									if (!alreadyExists) {
										currAddresses.add(currAddress_);
									}
									userPrefHelper_.setAllShippingAddresses(currAddresses);

									ArrayList<Address> currSelectedAddresses = userPrefHelper_.getSelectedAddresses();
									boolean isAlreadySelected = false;
									for (int i = 0; i < currSelectedAddresses.size(); i++) {
										Address selAddr = currSelectedAddresses.get(i);
										if (selAddr.getAddressId().equals(currAddress_.getAddressId())) {
											isAlreadySelected = true;
											currAddress_.setShipMethod(selAddr.getShipMethod());
											currSelectedAddresses.remove(i);
											currSelectedAddresses.add(currAddress_);
											break;
										}
									}
									
									if (!isAlreadySelected) {
										currSelectedAddresses.add(currAddress_);
									}
									
									userPrefHelper_.setSelectedShippingAddresses(currSelectedAddresses);
									
									continueCheck_ = true;
									fragmentHelper_.moveNextFragment();
								} else {
									setInterfaceState(STATE_ERROR);
									txtError_.setText("Unknown server error. Please check the address!");
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
	
	public class ImportContactClickResponse implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);  
		    startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("KindredSDK", "fragment activity result called back with code " + requestCode);
		if (requestCode == CONTACT_PICKER_RESULT && resultCode == Activity.RESULT_OK) {
			Uri result = data.getData();
			String id = result.getLastPathSegment();
			
			this.name_ = "";
			this.street_ = "";
			this.city_ = "";
			this.state_ = "";
			this.zip_ = "";
			this.country_ = "";
			this.email_ = "";
			this.phone_ = "";
			
			Cursor cursor = getActivity().getContentResolver().query(
			        Email.CONTENT_URI, null,
			        Email.CONTACT_ID + "=?",
			        new String[]{id}, null);
			if (cursor.moveToFirst()) {
				int emailIdx = cursor.getColumnIndex(Email.DATA);
			    this.email_ = cursor.getString(emailIdx); 
			}
			// try to grab phone
			cursor = getActivity().getContentResolver().query(
			        Phone.CONTENT_URI, null,
			        Phone.CONTACT_ID + "=?",
			        new String[]{id}, null);
			if (cursor.moveToFirst()) {
				int phoneIdx = cursor.getColumnIndex(Phone.DATA);
			    this.phone_ = cursor.getString(phoneIdx); 
			}
			
			// try to grab name
			cursor = getActivity().getContentResolver().query(result, null, null, null, null);
			if (cursor.moveToFirst()) {
	    		int firstCol = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
	    		this.name_ = cursor.getString(firstCol);
			}
			
			// try to grab address
			cursor = getActivity().getContentResolver().query(
					StructuredPostal.CONTENT_URI, null,
					StructuredPostal.CONTACT_ID + "=?",
			        new String[]{id}, null);
			if (cursor.moveToFirst()) {
				int streetIdx = cursor.getColumnIndex(StructuredPostal.STREET);  
				int cityIdx = cursor.getColumnIndex(StructuredPostal.CITY);
				int stateIdx = cursor.getColumnIndex(StructuredPostal.REGION);
				int zipIdx = cursor.getColumnIndex(StructuredPostal.POSTCODE);
				int countryIdx = cursor.getColumnIndex(StructuredPostal.COUNTRY);
				this.street_ = cursor.getString(streetIdx);
				this.city_ = cursor.getString(cityIdx);
				this.state_ = cursor.getString(stateIdx);
				this.zip_ = cursor.getString(zipIdx);
				this.country_ = cursor.getString(countryIdx);
			}
			
			cursor.close();
			
			this.editTextName_.setText(this.name_);
			this.editTextStreet_.setText(this.street_);
			this.editTextCity_.setText(this.city_);
			this.editTextZip_.setText(this.zip_);
			this.editTextState_.setText(this.state_);
			
			if (this.country_ == null)
				return;
			int index = this.countries_.indexOf(this.country_);
			if (index < 0 && !this.country_.equals("")) {
				this.countries_.add(this.country_);
				index = this.countries_.size()-1;
				this.dataAdapter_.notifyDataSetChanged();
			}
			if (index >= 0)
				this.editSpinnerCountry_.setSelection(index);
		}
	}
}
