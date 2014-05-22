package com.kindredprints.android.sdk.customviews;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class NetworkProgressBar {

	private ProgressDialog progressBar;
	
	private NetworkProgressBarCallback callback_;
	
	private Context context;

	public NetworkProgressBar(Context context) {		
		this.context = context;		
		this.progressBar = new ProgressDialog(this.context);
		this.progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.progressBar.setCanceledOnTouchOutside(false);
		this.progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		       	//hide();
		        if (callback_ != null) callback_.progressBarCancelled(); 
		    }
		});
	}
	
	public void setNetworkProgressCallback(NetworkProgressBarCallback callback) {
		this.callback_ = callback;
	}
	
	public boolean is_open() {
		return progressBar.isShowing();
	}
	
	public void show(String message, float progress) {
		progressBar.setMessage(message);
		if (!is_open())
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
	
	public interface NetworkProgressBarCallback {
		public void progressBarCancelled();
	}
}
