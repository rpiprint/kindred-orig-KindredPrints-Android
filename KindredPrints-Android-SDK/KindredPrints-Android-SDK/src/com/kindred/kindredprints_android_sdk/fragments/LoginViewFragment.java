package com.kindred.kindredprints_android_sdk.fragments;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.kindred.kindredprints_android_sdk.R;
import com.kindred.kindredprints_android_sdk.customviews.EditTextView;
import com.kindred.kindredprints_android_sdk.customviews.EditTextView.TextFieldModifiedListener;
import com.kindred.kindredprints_android_sdk.data.UserObject;
import com.kindred.kindredprints_android_sdk.fragments.KindredFragmentHelper.BackButtonPressInterrupter;
import com.kindred.kindredprints_android_sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindred.kindredprints_android_sdk.helpers.ImageUploadHelper;
import com.kindred.kindredprints_android_sdk.helpers.prefs.InterfacePrefHelper;
import com.kindred.kindredprints_android_sdk.helpers.prefs.UserPrefHelper;
import com.kindred.kindredprints_android_sdk.remote.KindredRemoteInterface;
import com.kindred.kindredprints_android_sdk.remote.NetworkCallback;

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
	private KindredRemoteInterface kindredRemoteInterface_;
	private UserObject currUser_;
		
	private KindredFragmentHelper fragmentHelper_;
	
	private TextView txtTitle_;
	private TextView txtError_;
	private EditTextView editTextEmail_;
	private EditTextView editTextPassword_;
	
	private String savedEmail_;
	
	private int currState_;
	private boolean continueCheck_;
	
	public LoginViewFragment() { }
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		this.interfacePrefHelper_ = new InterfacePrefHelper(activity);
		this.userPrefHelper_ = new UserPrefHelper(activity);
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
				this.txtError_.setVisibility(View.INVISIBLE);
				break;
			case STATE_NEED_PASSWORD:
				this.fragmentHelper_.configNavBarForHash(KindredFragmentHelper.FRAG_LOGIN+String.valueOf(STATE_NEED_PASSWORD));
				this.editTextPassword_.setVisibility(View.VISIBLE);
				this.txtTitle_.setText(getActivity().getResources().getString(R.string.login_title_password));
				this.txtError_.setVisibility(View.INVISIBLE);
				break;
			case STATE_WRONG_PASSWORD:
				this.fragmentHelper_.configNavBarForHash(KindredFragmentHelper.FRAG_LOGIN+String.valueOf(STATE_WRONG_PASSWORD));
				this.editTextPassword_.setVisibility(View.VISIBLE);
				this.txtTitle_.setText(getActivity().getResources().getString(R.string.login_title_password));
				this.txtError_.setVisibility(View.VISIBLE);
				this.txtError_.setText(errorMsg);
				break;
			case STATE_OTHER_ERROR:
				this.fragmentHelper_.configNavBarForHash(KindredFragmentHelper.FRAG_LOGIN);
				this.txtTitle_.setText(getActivity().getResources().getString(R.string.login_title));
				this.txtError_.setVisibility(View.VISIBLE);
				this.txtError_.setText(errorMsg);
				break;
		}
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
				kindredRemoteInterface_.createUser(postObj);
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
				kindredRemoteInterface_.loginUser(postObj);
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
								createUser();
								break;
							case STATE_NEED_PASSWORD:
								loginUser();
								break;
							case STATE_WRONG_PASSWORD:
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
					                
									userPrefHelper_.setUserObject(currUser_);
									
									ImageUploadHelper.getInstance(getActivity()).validateAllOrdersInit();
									
									continueCheck_ = true;
									fragmentHelper_.moveNextFragment();
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
