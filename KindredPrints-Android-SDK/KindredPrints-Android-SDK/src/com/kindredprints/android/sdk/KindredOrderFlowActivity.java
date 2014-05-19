package com.kindredprints.android.sdk;

import com.kindredprints.android.sdk.customviews.NavBarClickCallback;
import com.kindredprints.android.sdk.customviews.NavBarView;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;

public class KindredOrderFlowActivity extends FragmentActivity {
	public static final int KP_RESULT_CANCELLED = 101;
	public static final int KP_RESULT_PURCHASED = 102;
	
	private NavBarView navBar_;
	private FragmentManager fManager_;
	private KindredFragmentHelper fHelper_;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_kporder_flow_process);
		
		this.fManager_ = getSupportFragmentManager();
		this.navBar_ = (NavBarView) findViewById(R.id.navBar);
		this.navBar_.setButtonClickListener(new MainNavBarClickCallback());
		this.navBar_.show();
		
		this.fHelper_ = KindredFragmentHelper.getInstance(this.fManager_, this.navBar_, this);
		this.fHelper_.updateActivity(this);
		this.fHelper_.initRootFragment();
		fHelper_.configNavBar();
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}	
	
	@Override
	protected void onStart() {
		super.onStart();
		this.fHelper_.updateActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		MixpanelAPI.getInstance(this, this.getResources().getString(R.string.mixpanel_token)).flush();
		super.onDestroy();
	}
	
	public class MainNavBarClickCallback implements NavBarClickCallback {
		@Override
		public void onBackClick() {
			if (!fHelper_.moveLastFragment()) {
				setResult(KP_RESULT_CANCELLED);
				finish();
			} else {
				fHelper_.configNavBar();
			}
		}
		@Override
		public void onNextClick() {
			if (!fHelper_.moveNextFragment()) {
				setResult(KP_RESULT_PURCHASED);
				finish();
			} else {
				fHelper_.configNavBar();
			}
		}
		
	}
	
	@Override
	public void onBackPressed() {
		this.navBar_.triggerBackButton();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		   super.onActivityResult(requestCode, resultCode, data);
	}
}
