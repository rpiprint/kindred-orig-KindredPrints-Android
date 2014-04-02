package com.kindred.kindredprints_android_sdk.customviews;

import com.kindred.kindredprints_android_sdk.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class KindredAlertDialog extends Dialog implements OnClickListener {	
	Button cmdOK;
	Button cmdCancel;
	
	public KindredAlertDialog(Context context, String message, String buttonOneTitle, String buttonTwoTitle, boolean twoButton) {
		super(context);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (twoButton) {
			setContentView(R.layout.dialog_choice);
			cmdCancel = (Button) findViewById(R.id.cmdCancel);
			cmdCancel.setText(buttonTwoTitle);
			cmdCancel.setOnClickListener(this);
		} else
			setContentView(R.layout.dialog_no_choice);
		
		TextView txtMessage = (TextView) findViewById(R.id.txtMessage);
		txtMessage.setText(message);
		
		cmdOK = (Button) findViewById(R.id.cmdOK);
		cmdOK.setText(buttonOneTitle);
		cmdOK.setOnClickListener(this);
	}
	
	public KindredAlertDialog(Context context, String message, boolean twoButton) {
		super(context);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (twoButton) {
			setContentView(R.layout.dialog_choice);
			cmdCancel = (Button) findViewById(R.id.cmdCancel);
			cmdCancel.setOnClickListener(this);
		} else
			setContentView(R.layout.dialog_no_choice);
		
		TextView txtMessage = (TextView) findViewById(R.id.txtMessage);
		txtMessage.setText(message);
		
		cmdOK = (Button) findViewById(R.id.cmdOK);
		cmdOK.setOnClickListener(this);
	}
	
	public KindredAlertDialog(Context context, boolean twoButton) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (twoButton) {
			setContentView(R.layout.dialog_warning_choice);
		} else {
			setContentView(R.layout.dialog_warning_no_choice);
		}
		
		cmdOK = (Button) findViewById(R.id.cmdOK);
		cmdOK.setOnClickListener(this);
		if (twoButton) {
			cmdCancel = (Button) findViewById(R.id.cmdCancel);
			cmdCancel.setOnClickListener(this);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == cmdOK) {
			cancel();
		} else {
			dismiss();
		}
	}
}
