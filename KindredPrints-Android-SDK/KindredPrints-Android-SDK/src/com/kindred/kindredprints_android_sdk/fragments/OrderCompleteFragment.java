package com.kindred.kindredprints_android_sdk.fragments;

import com.kindred.kindredprints_android_sdk.R;
import com.kindred.kindredprints_android_sdk.data.Size;
import com.kindred.kindredprints_android_sdk.data.UserObject;
import com.kindred.kindredprints_android_sdk.fragments.KindredFragmentHelper.BackButtonPressInterrupter;
import com.kindred.kindredprints_android_sdk.helpers.cache.ImageManager;
import com.kindred.kindredprints_android_sdk.helpers.prefs.DevPrefHelper;
import com.kindred.kindredprints_android_sdk.helpers.prefs.InterfacePrefHelper;
import com.kindred.kindredprints_android_sdk.helpers.prefs.UserPrefHelper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class OrderCompleteFragment extends KindredFragment {
	private Activity activity_;
	private InterfacePrefHelper interfacePrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private UserObject currUser_;
	
	private KindredFragmentHelper fragmentHelper_;
		
	public OrderCompleteFragment() { }
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {	
		this.activity_ = activity;
		this.fragmentHelper_ = fragmentHelper;
		this.fragmentHelper_.configNavBar();
		
		this.devPrefHelper_ = new DevPrefHelper(activity);
		this.interfacePrefHelper_ = new InterfacePrefHelper(activity);
		this.userPrefHelper_ = new UserPrefHelper(activity);
		this.currUser_ = this.userPrefHelper_.getUserObject();
		
		this.fragmentHelper_.setNextButtonDreamCatcher_(null);
		this.fragmentHelper_.setBackButtonDreamCatcher_(new BackButtonHandler());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_order_complete, container, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
		txtTitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		TextView txtEmail = (TextView) view.findViewById(R.id.txtUserEmail);
		txtEmail.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtEmail.setText(this.currUser_.getEmail());
		
		TextView txtPowered = (TextView) view.findViewById(R.id.txtPoweredBy);
		txtPowered.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		TextView txtSupport = (TextView) view.findViewById(R.id.txtSupport);
		txtSupport.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		TextView txtDoneText = (TextView) view.findViewById(R.id.txtReturnTo);
		txtDoneText.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		TextView txtPartnerName = (TextView) view.findViewById(R.id.txtPartnerApp);
		txtPartnerName.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtPartnerName.setText(this.devPrefHelper_.getPartnerName());
		
		view.invalidate();
		
		final ImageView imgPartnerLogo = (ImageView) view.findViewById(R.id.imgLogo);
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Size displaySize = new Size(activity_.getResources().getDimension(R.dimen.order_complete_logo_width), activity_.getResources().getDimension(R.dimen.order_complete_logo_width));
				final String partnerName = devPrefHelper_.getPartnerName();
				final ImageManager imManager = ImageManager.getInstance(activity_);
				if (imManager.cacheOrigImageFromUrl(partnerName, devPrefHelper_.getPartnerUrl())) {
					Handler mainHandler = new Handler(getActivity().getMainLooper());
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							imgPartnerLogo.setImageBitmap(imManager.getImageFromFileSystem(partnerName, displaySize));
						}
					});
				}
			}
		}).start();
		
		Button cmdDone = (Button) view.findViewById(R.id.cmdDone);
		cmdDone.setTextColor(this.interfacePrefHelper_.getTextColor());
		cmdDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				fragmentHelper_.triggerNextButton();
			}
		});
		
		return view;
	}

	public class BackButtonHandler implements BackButtonPressInterrupter {
		@Override
		public boolean interruptBackButton() {
			fragmentHelper_.triggerNextButton();
			return true;
		}
	}
}
