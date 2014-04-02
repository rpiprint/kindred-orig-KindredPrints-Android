package com.kindred.kindredprints_android_sdk.customviews;

import android.app.ProgressDialog;
import android.content.Context;

public class NetworkProgressBar {

	private ProgressDialog progressBar;
	
	private Context context;

	public NetworkProgressBar(Context context) {		
		this.context = context;		
		this.progressBar = new ProgressDialog(this.context);
		this.progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}
	
	public boolean is_open() {
		return progressBar.isShowing();
	}
	
	public void show(String message, float progress) {
		progressBar.setMessage(message);
		progressBar.show();
		this.progressBar.setProgress((int)(this.progressBar.getMax()*progress));
	}
	
	public void change_progress(float progress) {
		this.progressBar.setProgress((int)(this.progressBar.getMax()*progress));
	}
	
	public void change_message(String message) {
		progressBar.setMessage(message);
	}
	
	public void hide() {
		progressBar.dismiss();
	}
	
	public void cleanup() {
		context = null;
	}
	
}
