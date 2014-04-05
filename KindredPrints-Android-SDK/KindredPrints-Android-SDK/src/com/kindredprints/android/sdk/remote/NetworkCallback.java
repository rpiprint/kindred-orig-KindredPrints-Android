package com.kindredprints.android.sdk.remote;

import org.json.JSONObject;

public interface NetworkCallback {
	public void finished(JSONObject serverResponse);
}
