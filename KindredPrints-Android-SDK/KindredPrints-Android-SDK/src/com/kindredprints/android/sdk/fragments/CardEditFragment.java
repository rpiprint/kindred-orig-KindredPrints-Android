package com.kindredprints.android.sdk.fragments;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.R;
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
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CardEditFragment extends KindredFragment {
	private Activity activity_;
	
	private KindredFragmentHelper fragmentHelper_;
	private KindredRemoteInterface kindredRemoteInt_;
	
	private OrderProcessingHelper orderProcessingHelper_;
	private InterfacePrefHelper interfacePrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private UserObject currUser_;
	
	private TextView txtTotal_;
	private TextView txtTotalAmount_;
	
	private TextView txtTitle_;
	private TextView txtError_;
	private Button cmdCompleteOrder_;
	private EditText editTextName_;
	private EditText editTextCard_;
	private EditText editTextMonthYear_;
	private EditText editTextCode_; 
	
	private View viewDiv0_;
	private View viewDiv1_;
	private View viewDiv2_;
	private View viewDiv3_;
	
	private boolean payImmediately_;
	private boolean continueCheck_;
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		this.activity_ = activity;
		
		this.kindredRemoteInt_ = new KindredRemoteInterface(activity);
		this.kindredRemoteInt_.setNetworkCallbackListener(new CardUpdateNetworkCallback());
		
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
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_edit_card, container, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
	
		this.txtTitle_ = (TextView) view.findViewById(R.id.txtSubTitle);
		this.txtTitle_.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		this.txtTotal_ = (TextView) view.findViewById(R.id.txtTotal);
		this.txtTotal_.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		this.txtTotalAmount_ = (TextView) view.findViewById(R.id.txtTotalAmount);
		this.txtTotalAmount_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.txtTotalAmount_.setText(this.devPrefHelper_.getOrderTotal());
		
		this.txtError_ = (TextView) view.findViewById(R.id.txtError);
		this.txtError_.setTextColor(activity_.getResources().getColor(R.color.color_red));
		this.txtError_.setVisibility(View.INVISIBLE);
		
		this.cmdCompleteOrder_ = (Button) view.findViewById(R.id.cmdCompleteOrder);
		this.cmdCompleteOrder_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.cmdCompleteOrder_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				txtError_.setVisibility(View.INVISIBLE);
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
					    Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				if (validateCardInfo()) {
					fragmentHelper_.showProgressBarWithMessage("registering card..");
					getStripeToken();
				} else if (currUser_.isPaymentSaved()) {
					fragmentHelper_.showProgressBarWithMessage("initiating payment..");
					orderProcessingHelper_.initiateCheckoutSequence();
				}
			}
		});
		
		if (this.currUser_.isPaymentSaved()) {
			payImmediately_ = false;
			this.cmdCompleteOrder_.setVisibility(View.INVISIBLE);
		} else {
			payImmediately_ = true;
		}
		
		this.editTextCard_ = (EditText) view.findViewById(R.id.editTextCard);
		this.editTextCard_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextCard_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextCard_.addTextChangedListener(new CardInputListener());
		this.editTextCard_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN ) {
					editTextMonthYear_.requestFocus();
				}
				return false;
			}			
		});
		
		this.editTextName_ = (EditText) view.findViewById(R.id.editTextName);
		this.editTextName_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextName_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextName_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN ) {
					editTextCard_.requestFocus();
				}
				return false;
			}			
		});
		this.editTextMonthYear_ = (EditText) view.findViewById(R.id.editTextMonthYear);
		this.editTextMonthYear_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextMonthYear_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextMonthYear_.addTextChangedListener(new DateInputListener());
		this.editTextMonthYear_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_ENTER ) {
					editTextCode_.requestFocus();
				}
				return false;
			}			
		});
		
		this.editTextCode_ = (EditText) view.findViewById(R.id.editTextCode);
		this.editTextCode_.setBackgroundColor(Color.TRANSPARENT);
		this.editTextCode_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.editTextCode_.addTextChangedListener(new CodeInputListener());
		this.editTextCode_.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.FLAG_EDITOR_ACTION) {
					fragmentHelper_.triggerNextButton();
				}
				return false;
			}			
		});
		
		this.viewDiv0_ = (View) view.findViewById(R.id.viewDiv0);
		this.viewDiv0_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		
		this.viewDiv1_ = (View) view.findViewById(R.id.viewDiv1);
		this.viewDiv1_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		
		this.viewDiv2_ = (View) view.findViewById(R.id.viewDiv2);
		this.viewDiv2_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		
		this.viewDiv3_ = (View) view.findViewById(R.id.viewDiv3);
		this.viewDiv3_.setBackgroundColor(this.interfacePrefHelper_.getTextColor());
		
		return view;
	}
	
	private boolean validateCardInfo() {
		this.txtError_.setVisibility(View.INVISIBLE);
		if (!isFormFilledOut()) {
			this.txtError_.setVisibility(View.VISIBLE);
			this.txtError_.setText(activity_.getResources().getString(R.string.card_edit_err_blank));
			return false;
		} else if (!luhnCheckOnCard(removeNonDigits(this.editTextCard_.getText().toString()))) {
			this.txtError_.setVisibility(View.VISIBLE);
			this.txtError_.setText(activity_.getResources().getString(R.string.card_edit_err_card_number));
			return false;
		} else if (!expDateCheck(removeNonDigits(this.editTextMonthYear_.getText().toString()))) {
			this.txtError_.setVisibility(View.VISIBLE);
			this.txtError_.setText(activity_.getResources().getString(R.string.card_edit_err_card_exp));
			return false;
		}
		return true;
	}
	
	private void getStripeToken() {
		Card card = null;
		if (!this.devPrefHelper_.getIsStripeLive()) {
			card = new Card(
					"4242424242424242",
					11,
					15,
					"333");
		} else {
			String monthyear = removeNonDigits(editTextMonthYear_.getText().toString());
			card = new Card(
					removeNonDigits(editTextCard_.getText().toString()),
					getMonth(monthyear),
					getYear(monthyear),
					editTextCode_.getText().toString());
		}
		
		if (!card.validateCard()) {
			if (!card.validateNumber()) {
				this.txtError_.setText("This card number is invalid.");
				this.txtError_.setVisibility(View.VISIBLE);
			} if (!card.validateExpiryDate()) {
				this.txtError_.setText("This expiration date is invalid.");
				this.txtError_.setVisibility(View.VISIBLE);
			} if (!card.validateCVC()) {
				this.txtError_.setText("This CVV code is invalid.");
				this.txtError_.setVisibility(View.VISIBLE);
			}
		}
		
		Stripe stripe = new Stripe();
		stripe.createToken(
				card, 
				this.devPrefHelper_.getStripeKey(),
				new TokenCallback() {
					@Override
					public void onError(Exception error) {
						fragmentHelper_.hideProgressBar();
						Log.i("KindredSDK", "Stripe error: " + error.getLocalizedMessage());
						txtError_.setVisibility(View.VISIBLE);
						txtError_.setText(activity_.getResources().getString(R.string.card_edit_err_card_declined));
					}

					@Override
					public void onSuccess(final Token token) {
						fragmentHelper_.updateProgressBarWithMessage("updating records..");
						new Thread(new Runnable() {
							public void run() {
								JSONObject post = new JSONObject();
								try {
									post.put("auth_key", currUser_.getAuthKey());
									post.put("stripe_token", token.getId());
									kindredRemoteInt_.registerStripeToken(post, currUser_.getId());
								} catch (JSONException ex) {
									ex.printStackTrace();
								}
							}
						}).start();
					}
				});
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				JSONObject post = new JSONObject();
				try {
					post.put("auth_key", currUser_.getAuthKey());
					post.put("name", editTextName_.getText().toString());
					kindredRemoteInt_.registerName(post, currUser_.getId());
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
		}).start();
	}
	
	private boolean luhnCheckOnCard(String stringToTest) {		
	    char[] stringAsChars = new char[stringToTest.length()];
	    for (int i = 0; i < stringToTest.length(); i++) {
	    	stringAsChars[i] = stringToTest.charAt(i);
	    }
	    
	    boolean isOdd = true;
	    int oddSum = 0;
	    int evenSum = 0;
	    
	    for (int i = stringToTest.length() - 1; i >= 0; i--) {
	        
	        int digit = Character.getNumericValue(stringAsChars[i]);
	        
	        if (isOdd)
	            oddSum += digit;
	        else
	            evenSum += digit/5 + (2*digit) % 10;
	        
	        isOdd = !isOdd;
	    }
	    
	    return ((oddSum + evenSum) % 10 == 0);
	}
	
	private boolean expDateCheck(String stringToTest) {
		 if (stringToTest.length() < 2)
		        return true;
		    
		    int month = getMonth(stringToTest);
		    
		    if (month < 1 || month > 12)
		        return false;
		    
		    if (stringToTest.length() < 4)
		        return true;
		    
		    Calendar c = Calendar.getInstance();
		    int thisYear = c.get(Calendar.YEAR);;
		    int thisMonth = c.get(Calendar.MONTH)+1;
		    
		    
		    int year = getYear(stringToTest);
		    
		    if (year+2000 < thisYear || ((year+2000) == thisYear && month < thisMonth))
		        return false;
		    
		    if ((year+2000) > thisYear+15)
		        return false;
		    
		    return true;
	}
	
	private boolean isFormFilledOut() {
		if (this.editTextName_.getText().length() == 0) {
			
			return false;
		}
		if (this.editTextCard_.getText().length() == 0) {
			this.txtError_.setText("Please fill in the card number field.");
			this.txtError_.setVisibility(View.VISIBLE);
			return false;
		}
		if (this.editTextMonthYear_.getText().length() == 0) {
			this.txtError_.setText("Please fill in the expiration date field.");
			this.txtError_.setVisibility(View.VISIBLE);
			return false;
		}
		if (this.editTextCode_.getText().length() == 0) {
			this.txtError_.setText("Please fill in the CVV field.");
			this.txtError_.setVisibility(View.VISIBLE);
			return false;
		}
			
		return true;
	}
	
	private String removeNonDigits(String string) {
	    String digitsOnlyString = "";
	    for (int i = 0; i < string.length(); i++) {
	        char characterToAdd = string.charAt(i);
	        if (Character.isDigit(characterToAdd) || characterToAdd == '*') {
	            digitsOnlyString = digitsOnlyString + characterToAdd;
	        }
	    }
	    return digitsOnlyString;
	}
	
	private String insertSpacesEveryFourDigitsIntoString(String string, boolean ignore) {
	    String stringWithAddedSpaces = "";
	    for (int i = 0; i < string.length(); i++) {
	        char characterToAdd = string.charAt(i);
	        stringWithAddedSpaces = stringWithAddedSpaces + characterToAdd;
	        if ((i > 0) && (((i + 1) % 4) == 0) && (i + 1) < 16 && !(ignore && (i+1) == string.length())) {
	            stringWithAddedSpaces = stringWithAddedSpaces + " ";
	        }
	    }
	    return stringWithAddedSpaces;
	}

	private String insertSlashEveryTwoDigitsIntoString(String string, boolean ignore) {
	    String stringWithAddedSpaces = "";
	    for (int i = 0; i < string.length(); i++) {
	        char characterToAdd = string.charAt(i);
	        stringWithAddedSpaces = stringWithAddedSpaces + characterToAdd;
	        if ((i>0) && (((i+1) % 2) == 0) && (i+1)<4 && !(ignore && (i+1) == string .length())) {
	            stringWithAddedSpaces = stringWithAddedSpaces + " / ";
	        }
	    }
	    
	    return stringWithAddedSpaces;
	}
	
	private int getMonth(String string) {
	    int month = -1;
	    try {
	    	month = Integer.valueOf(string.substring(0, 2));
	    } catch (NumberFormatException ex) { }
	    return month;
	}
	private int getYear(String string) {
		int year = -1;
	    try {
	    	year = Integer.valueOf(string.substring(2, 4));
	    } catch (NumberFormatException ex) { }
	    return year;
	}
	

	public class CardInputListener implements TextWatcher {
		private boolean isDelete;
		@Override
		public void afterTextChanged(Editable s) {
			String rawString = s.toString();
			String stringOnlyNumbers = removeNonDigits(rawString);

			if (stringOnlyNumbers.length() > 16) {
	           s = s.delete(rawString.length()-1, rawString.length());
	        }
	        
	        if (!luhnCheckOnCard(stringOnlyNumbers) && stringOnlyNumbers.length() == 16) {
	        	editTextCard_.setTextColor(activity_.getResources().getColor(R.color.color_red));
	        } else {
	        	editTextCard_.setTextColor(interfacePrefHelper_.getTextColor());
	        }
	        
	        String cardNumberWithSpaces = insertSpacesEveryFourDigitsIntoString(stringOnlyNumbers, isDelete);
	        if (rawString.length()-stringOnlyNumbers.length() != cardNumberWithSpaces.length()-stringOnlyNumbers.length() && !isDelete) {
	        	editTextCard_.setText(cardNumberWithSpaces);
	        	editTextCard_.setSelection(cardNumberWithSpaces.length());
	        }
	   
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			isDelete = count > 0 && 0 == after;
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) { }
	}
	
	public class DateInputListener implements TextWatcher {
		boolean isDelete;
		@Override
		public void afterTextChanged(Editable s) {
			String rawString = s.toString();
			String stringOnlyNumbers = removeNonDigits(rawString);
			if (stringOnlyNumbers.length() > 4) {
		           s = s.delete(rawString.length()-1, rawString.length());
		    }
	            
	        
	        if (!expDateCheck(stringOnlyNumbers)) {
	        	editTextMonthYear_.setTextColor(activity_.getResources().getColor(R.color.color_red));
	        } else {
	        	editTextMonthYear_.setTextColor(interfacePrefHelper_.getTextColor());
	        }
	        
	        String expDateWithSpaces =  insertSlashEveryTwoDigitsIntoString(stringOnlyNumbers, isDelete);
	        
	        if (rawString.length()-stringOnlyNumbers.length() != expDateWithSpaces.length()-stringOnlyNumbers.length() && !isDelete) {
	        	editTextMonthYear_.setText(expDateWithSpaces);
	        	editTextMonthYear_.setSelection(expDateWithSpaces.length());
	        }
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			isDelete = count > 0 && 0 == after;
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) { }
	}
	
	public class CodeInputListener implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
			int maxCode = 4;
			if (editTextCard_.getText().toString().length() > 2) {
				char firstChar = editTextCard_.getText().toString().charAt(0);
				char secondChar = editTextCard_.getText().toString().charAt(1);
				int firstNum = Character.getNumericValue(firstChar);
				int secondNum = Character.getNumericValue(secondChar);
				
				if (firstNum == 3 && (secondNum == 4 || secondNum == 7)) {
					maxCode = 4;
				} else {
					maxCode = 3;
				}
			}
			if (s.length() > maxCode) {
	           s = s.delete(s.length()-1, s.length());
	        }
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) { }
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) { }
	}
			
	public class NextButtonHandler implements NextButtonPressInterrupter {
		@Override
		public boolean interruptNextButton() {
			if (!continueCheck_) {
				
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
					    Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				if (validateCardInfo()) {
					fragmentHelper_.showProgressBarWithMessage("registering card..");
					getStripeToken();
				}
				return true;
			}
			return false;
		}
	}
	
	public class OrderCheckoutHelperListener implements OrderProcessingUpdateListener {
		@Override
		public void orderCreatedOrUpdated(ArrayList<LineItem> orderItems) { }

		@Override
		public void orderProcessingUpdateProgress(float progress, String message) {
			fragmentHelper_.updateProgressBarWithMessage(message);
			fragmentHelper_.updateProgressBarWithProgress(progress);
		}
		
		@Override
		public void orderNeedsPayment() { }
		
		@Override
		public void orderFailedToProcess(String error) {
			fragmentHelper_.hideProgressBar();
			txtError_.setVisibility(View.VISIBLE);
			txtError_.setText(error);
			KindredAlertDialog kad = new KindredAlertDialog(activity_, error, false);
			kad.show();
		}

		@Override
		public void orderProcessed() {
			fragmentHelper_.hideProgressBar();
			fragmentHelper_.moveToFragment(KindredFragmentHelper.FRAG_ORDER_FINISHED);
		}
	}
	
	public class CardUpdateNetworkCallback implements NetworkCallback {
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
							
							if (requestTag.equals(KindredRemoteInterface.REQ_TAG_STRIPE_REG)) {
								if (status == 200) {
									currUser_.setPaymentSaved(serverResponse.getInt("payment_status")==1);
									currUser_.setLastFour(serverResponse.getString("last_four"));
									currUser_.setCreditType(serverResponse.getString("card_type"));
									userPrefHelper_.setUserObject(currUser_);
									if (payImmediately_) {
										fragmentHelper_.showProgressBarWithMessage("initiating payment..");
										orderProcessingHelper_.initiateCheckoutSequence();
									} else {
										fragmentHelper_.hideProgressBar();
										fragmentHelper_.moveLastFragment();
									}
								} else {
									fragmentHelper_.hideProgressBar();
									txtError_.setVisibility(View.VISIBLE);
									txtError_.setText(activity_.getResources().getString(R.string.card_edit_err_card_declined));
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
