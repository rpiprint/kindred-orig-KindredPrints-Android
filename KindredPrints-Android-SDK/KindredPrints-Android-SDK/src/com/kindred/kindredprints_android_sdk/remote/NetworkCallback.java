package com.kindred.kindredprints_android_sdk.remote;

import org.json.JSONObject;

public interface NetworkCallback {
	public void finished(JSONObject serverResponse);
}
