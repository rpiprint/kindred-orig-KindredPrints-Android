package com.kindred.kindredprints_android_sdk.data;

public class UserObject {
	public static final String USER_VALUE_NONE = "kp_no_user";
	
	private String id;
	private String name;
	private String email;
	private String authKey;
	private String creditType;
	private String lastFour;
	private boolean paymentSaved;
	
	public UserObject() {
		this.id = USER_VALUE_NONE;
		this.name = USER_VALUE_NONE;
		this.email = USER_VALUE_NONE;
		this.authKey = USER_VALUE_NONE;
		this.creditType = USER_VALUE_NONE;
		this.lastFour = USER_VALUE_NONE;
		this.paymentSaved = false;
	}
	
	public UserObject copy() {
		UserObject uCopy = new UserObject();
		uCopy.setId(this.getId());
		uCopy.setName(this.getName());
		uCopy.setEmail(this.getEmail());
		uCopy.setAuthKey(this.getAuthKey());
		uCopy.setCreditType(this.getCreditType());
		uCopy.setLastFour(this.getLastFour());
		uCopy.setPaymentSaved(this.isPaymentSaved());
		return uCopy;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public String getCreditType() {
		return creditType;
	}

	public void setCreditType(String creditType) {
		this.creditType = creditType;
	}

	public String getLastFour() {
		return lastFour;
	}

	public void setLastFour(String lastFour) {
		this.lastFour = lastFour;
	}

	public boolean isPaymentSaved() {
		return paymentSaved;
	}

	public void setPaymentSaved(boolean paymentSaved) {
		this.paymentSaved = paymentSaved;
	}
}
