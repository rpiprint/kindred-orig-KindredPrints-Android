package com.kindredprints.android.sdk.fragments;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.customviews.EditTextView;
import com.kindredprints.android.sdk.customviews.EditTextView.TextFieldModifiedListener;
import com.kindredprints.android.sdk.data.Address;
import com.kindredprints.android.sdk.data.UserObject;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.BackButtonPressInterrupter;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindredprints.android.sdk.helpers.ImageUploadHelper;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;
import com.kindredprints.android.sdk.remote.KindredRemoteInterface;
import com.kindredprints.android.sdk.remote.NetworkCallback;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class LoginViewFragment extends KindredFragment {
	protected static final int STATE_NO_PASSWORD = 0;
	protected static final int STATE_NEED_PASSWORD = 1;
	protected static final int STATE_WRONG_PASSWORD = 2;
	protected static final int STATE_OTHER_ERROR = 3;
	
	private InterfacePrefHelper interfacePrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private KindredRemoteInterface kindredRemoteInterface_;
	private UserObject currUser_;
		
	private KindredFragmentHelper fragmentHelper_;
	
	private TextView txtTitle_;
	private TextView txtReasonOne_;
	private TextView txtReasonTwo_;
	private TextView txtReasonThree_;
	private TextView txtSubtitle_;
	private TextView txtError_;
	private EditTextView editTextEmail_;
	private EditTextView editTextPassword_;
		
	private MixpanelAPI mixpanel_;
	
	private String savedEmail_;
	
	private int currState_;
	private boolean continueCheck_;
	
	public LoginViewFragment() { }
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		mixpanel_ = MixpanelAPI.getInstance(activity, activity.getResources().getString(R.string.mixpanel_token));
		mixpanel_.track("login_page_view", null);

		this.interfacePrefHelper_ = new InterfacePrefHelper(activity);
		this.userPrefHelper_ = new UserPrefHelper(activity);
		this.devPrefHelper_ = new DevPrefHelper(activity);
		this.currUser_ = this.userPrefHelper_.getUserObject();
		this.kindredRemoteInterface_ = new KindredRemoteInterface(activity);
		this.kindredRemoteInterface_.setNetworkCallbackListener(new UserLoginRegCallback());
		this.fragmentHelper_ = fragmentHelper;
		this.fragmentHelper_.configNavBar();
		this.continueCheck_ = false;
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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_login_view, container, false);
	
		this.txtTitle_ = (TextView) view.findViewById(R.id.txtTitle);
		this.txtReasonOne_ = (TextView) view.findViewById(R.id.txtReasonOne);
		this.txtReasonTwo_ = (TextView) view.findViewById(R.id.txtReasonTwo);
		this.txtReasonThree_ = (TextView) view.findViewById(R.id.txtReasonThree);
		this.txtSubtitle_ = (TextView) view.findViewById(R.id.txtSubtitle);
		this.txtError_ = (TextView) view.findViewById(R.id.txtError);
		this.editTextEmail_ = (EditTextView) view.findViewById(R.id.editTextEmail);
		this.editTextPassword_ = (EditTextView) view.findViewById(R.id.editTextPassword);
		this.editTextEmail_.initEditTextView(EditTextView.EDIT_TYPE_EMAIL);
		this.editTextEmail_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN ) {
					if (currState_ == STATE_NO_PASSWORD) {
						fragmentHelper_.triggerNextButton();
					} else {
						editTextPassword_.requestFocus();
					}
				}
				return false;
			}			
		});
		this.editTextPassword_.initEditTextView(EditTextView.EDIT_TYPE_PASSWORD);
		this.editTextPassword_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN ) {
					fragmentHelper_.triggerNextButton();
				}
				return false;
			}			
		});
		
		this.editTextEmail_.setTextFieldModifiedListener(new TextFieldsClearForLogin());
		this.editTextPassword_.setTextFieldModifiedListener(new TextFieldsClearForLogin());
		
		this.txtTitle_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.txtSubtitle_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.txtReasonOne_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.txtReasonTwo_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.txtReasonThree_.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		this.txtSubtitle_.setText(getActivity().getResources().getString(R.string.login_title_more));
		this.txtReasonOne_.setText(getActivity().getResources().getString(R.string.login_title_order_conf));
		this.txtReasonTwo_.setText(getActivity().getResources().getString(R.string.login_title_shipment_conf));
		this.txtReasonThree_.setText(getActivity().getResources().getString(R.string.login_title_customer_support));
		
		this.txtError_.setTextColor(getActivity().getResources().getColor(R.color.color_red));
		
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		if (!this.currUser_.getEmail().equals(UserObject.USER_VALUE_NONE) && this.currUser_.getId().equals(UserObject.USER_VALUE_NONE)) {
			this.editTextEmail_.setText(this.currUser_.getEmail());
			setInterfaceState(STATE_NEED_PASSWORD, null);
		} else {
			setInterfaceState(STATE_NO_PASSWORD, null);
		}
		
		return view;
	}
	
	private void setInterfaceState(int state, String errorMsg) {
		this.currState_ = state;
		switch (state) {
			case STATE_NO_PASSWORD:
				this.fragmentHelper_.configNavBarForHash(KindredFragmentHelper.FRAG_LOGIN);
				this.editTextPassword_.setVisibility(View.INVISIBLE);
				this.txtTitle_.setText(getActivity().getResources().getString(R.string.login_title));
				this.txtSubtitle_.setVisibility(View.VISIBLE);
				this.txtReasonOne_.setVisibility(View.VISIBLE);
				this.txtReasonTwo_.setVisibility(View.VISIBLE);
				this.txtReasonThree_.setVisibility(View.VISIBLE);
				this.txtError_.setVisibility(View.INVISIBLE);
				break;
			case STATE_NEED_PASSWORD:
				this.fragmentHelper_.configNavBarForHash(KindredFragmentHelper.FRAG_LOGIN+String.valueOf(STATE_NEED_PASSWORD));
				this.editTextPassword_.setVisibility(View.VISIBLE);
				this.txtTitle_.setText(getActivity().getResources().getString(R.string.login_title_password));
				this.txtReasonOne_.setVisibility(View.GONE);
				this.txtReasonTwo_.setVisibility(View.GONE);
				this.txtReasonThree_.setVisibility(View.GONE);
				this.txtError_.setVisibility(View.INVISIBLE);
				break;
			case STATE_WRONG_PASSWORD:
				this.fragmentHelper_.configNavBarForHash(KindredFragmentHelper.FRAG_LOGIN+String.valueOf(STATE_WRONG_PASSWORD));
				this.editTextPassword_.setVisibility(View.VISIBLE);
				this.txtTitle_.setText(getActivity().getResources().getString(R.string.login_title_password));
				this.txtSubtitle_.setText(getActivity().getResources().getString(R.string.login_title_password_more));
				this.txtReasonOne_.setVisibility(View.GONE);
				this.txtReasonTwo_.setVisibility(View.GONE);
				this.txtReasonThree_.setVisibility(View.GONE);
				this.txtError_.setVisibility(View.VISIBLE);
				this.txtError_.setText(errorMsg);
				break;
			case STATE_OTHER_ERROR:
				this.fragmentHelper_.configNavBarForHash(KindredFragmentHelper.FRAG_LOGIN);
				this.txtTitle_.setText(getActivity().getResources().getString(R.string.login_title));
				this.txtSubtitle_.setText(getActivity().getResources().getString(R.string.login_title_password_more));
				this.txtSubtitle_.setVisibility(View.VISIBLE);
				this.txtReasonOne_.setVisibility(View.VISIBLE);
				this.txtReasonTwo_.setVisibility(View.VISIBLE);
				this.txtReasonThree_.setVisibility(View.VISIBLE);
				this.txtError_.setVisibility(View.VISIBLE);
				this.txtError_.setText(errorMsg);
				break;
		}
		JSONObject code = new JSONObject();
		try {
			code.put("state_code", state);
			if (errorMsg != null) code.put("error_message", errorMsg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mixpanel_.track("login_interface_state_change", code);
	}
	
	private void resetPassword() {
		JSONObject postObj = new JSONObject();
		try {
			postObj.put("email", savedEmail_);
			kindredRemoteInterface_.startPasswordReset(postObj);
		} catch (JSONException e) {
			Log.i(getClass().getSimpleName(), "JSON exception: " + e.getMessage());
		}
	}
	
	private void createUser() {
		JSONObject postObj = new JSONObject();
		try {
			if (!editTextEmail_.isBlank()) {
				savedEmail_ = editTextEmail_.getText();
				postObj.put("name", "Kindred Prints family member");
				postObj.put("os", "android");
				postObj.put("email", savedEmail_);
				postObj.put("password", String.valueOf(Calendar.getInstance().getTimeInMillis()/1000));
				postObj.put("sdk", true);
				postObj.put("send_welcome", true);
				kindredRemoteInterface_.createUser(postObj);
			} else {
				Handler mainHandler = new Handler(getActivity().getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						fragmentHelper_.hideProgressBar();
						handleErrors(422);
					}
				});
			}
		} catch (JSONException e) {
			Log.i(getClass().getSimpleName(), "JSON exception: " + e.getMessage());
		}
	}
	
	private void loginUser() {
		JSONObject postObj = new JSONObject();
		try {
			if (!editTextEmail_.isBlank() && !editTextPassword_.isBlank()) {
				savedEmail_ = editTextEmail_.getText();
				postObj.put("email", savedEmail_);
				postObj.put("password", editTextPassword_.getText());
				postObj.put("sdk", true);
				postObj.put("send_welcome", true);
				kindredRemoteInterface_.loginUser(postObj);
			} else {
				Handler mainHandler = new Handler(getActivity().getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						fragmentHelper_.hideProgressBar();
						handleErrors(424);
					}
				});
			}
		} catch (JSONException e) {
			Log.i(getClass().getSimpleName(), "JSON exception: " + e.getMessage());
		}
	}
	
	private void handleErrors(int errorCode) {
		switch (errorCode) {
			case 421:
				setInterfaceState(STATE_NEED_PASSWORD, null);
				break;
			case 422:
				setInterfaceState(STATE_OTHER_ERROR, getActivity().getResources().getString(R.string.err_login_email_blank));
	            break;
	        case 423:
				setInterfaceState(STATE_OTHER_ERROR, getActivity().getResources().getString(R.string.err_login_email_invalid));
	            break;
	        case 424:
	        	setInterfaceState(STATE_OTHER_ERROR, getActivity().getResources().getString(R.string.err_login_password_blank));
	            break;
	        case 426:
	        	setInterfaceState(STATE_OTHER_ERROR, getActivity().getResources().getString(R.string.err_login_password_short));
	            break;
	        case 429:
	        	setInterfaceState(STATE_WRONG_PASSWORD, getActivity().getResources().getString(R.string.err_login_password_wrong));
	            break;
	        default:
	        	setInterfaceState(STATE_OTHER_ERROR, getActivity().getResources().getString(R.string.err_login_unknown));
	            break;
		}
	}
	
	public class NextButtonHandler implements NextButtonPressInterrupter {
		@Override
		public boolean interruptNextButton() {
			if (!continueCheck_) {
				fragmentHelper_.showProgressBarWithMessage("checking credentials..");

				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
					    Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						switch (currState_) {
							case STATE_NO_PASSWORD:
								mixpanel_.track("login_pressed_create", null);
								createUser();
								break;
							case STATE_NEED_PASSWORD:
								mixpanel_.track("login_pressed_login", null);
								loginUser();
								break;
							case STATE_WRONG_PASSWORD:
								mixpanel_.track("login_pressed_reset", null);
								resetPassword();
								break;
							case STATE_OTHER_ERROR:
								if (editTextPassword_.getVisibility() == View.VISIBLE) {
									loginUser();
								} else {
									createUser();
								}
								break;
						}
					}
				};
				new Thread(runnable).start();
				return true;
			}
			return false;
		}
	}
	
	public class UserLoginRegCallback implements NetworkCallback {
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

							if (requestTag.equals(KindredRemoteInterface.REQ_TAG_LOGIN) || requestTag.equals(KindredRemoteInterface.REQ_TAG_REGISTER)) {
								if (status == 200) {
									String userId = serverResponse.getString("user_id");
					                String name = serverResponse.getString("name");
					                String email = serverResponse.getString("email");
					                String authKey = serverResponse.getString("auth_key");
									currUser_.setId(userId);
									currUser_.setName(name);
									currUser_.setEmail(email);
									currUser_.setAuthKey(authKey);
									currUser_.setPaymentSaved(false);
					                
									mixpanel_.alias(email, null);
									
									userPrefHelper_.setUserObject(currUser_);
									
									ImageUploadHelper.getInstance(getActivity()).validateAllOrdersInit();
									
									fragmentHelper_.showProgressBarWithMessage("importing user data..");
									new Thread(new Runnable() {
										@Override
										public void run() {
											JSONObject postObj = new JSONObject();
											try {
												postObj.put("auth_key", currUser_.getAuthKey());
												kindredRemoteInterface_.downloadAllAddresses(postObj, currUser_.getId());
											} catch (JSONException e) {
												Log.i(getClass().getSimpleName(), "JSON exception: " + e.getMessage());
											}
										}
									}).start();
								} else if (status == 421) {
									setInterfaceState(STATE_NEED_PASSWORD, null);
								} else {
									handleErrors(status);
								}
								
							} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_PASSWORD_RESET)) {
								if (status == 200) {
									setInterfaceState(STATE_OTHER_ERROR, getActivity().getResources().getString(R.string.err_login_reset_done));
									editTextPassword_.clearText();
								}
							} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_GET_ADDRESSES)) {
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
									
									userPrefHelper_.setAllShippingAddresses(addresses);
									
									devPrefHelper_.resetAddressDownloadStatus();
									
									continueCheck_ = true;
									fragmentHelper_.moveNextFragment();
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
	
	public class TextFieldsClearForLogin implements TextFieldModifiedListener {
		@Override
		public void textFieldWasModified(String postChangeText) {
			if (editTextEmail_.isBlank() && editTextPassword_.isBlank()) {
				fragmentHelper_.setNextButtonEnabled(false);
			} else {
				fragmentHelper_.setNextButtonEnabled(true);
			}
		}
	}
}
