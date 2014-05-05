package com.kindredprints.android.sdk.fragments;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.GridView;

import com.kindredprints.android.sdk.KPhoto;
import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.adapters.PhotoSelectAdapter;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.BackButtonPressInterrupter;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class PhotoSelectFragment extends KindredFragment {
	private InterfacePrefHelper interfacePrefHelper_;
	
	private KindredFragmentHelper fragmentHelper_;
	private Activity context_;
	private MixpanelAPI mixpanel_;
	
	private CartManager cartManager_;
	
	private PhotoSelectAdapter photoAdapter_;
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		context_ = activity;
		mixpanel_ = MixpanelAPI.getInstance(activity, activity.getResources().getString(R.string.mixpanel_token));
		mixpanel_.track("photo_select_page_view", null);
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(context_);
		
		this.cartManager_ = CartManager.getInstance(activity);
		this.fragmentHelper_ = fragmentHelper;
		fragmentHelper.setNextButtonDreamCatcher_(new SelectNextButtonHandler());
		fragmentHelper.setBackButtonDreamCatcher_(new SelectBackButtonHandler());
		fragmentHelper.configNavBar();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = (ViewGroup) inflater.inflate(R.layout.fragment_select_photos, container, false);
		
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
	
		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				initInterface();
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
		
		return view;
	}
	
	private void initInterface() {
		GridView photoGrid = (GridView) this.getView().findViewById(R.id.gridview);
		
		int gridItemWidth = (int)(photoGrid.getWidth()-this.context_.getResources().getDimension(R.dimen.select_horiz_spacing))/3;
		photoGrid.setColumnWidth(gridItemWidth);

		this.photoAdapter_ = new PhotoSelectAdapter(this.context_, this.fragmentHelper_, this.cartManager_.getPendingImages(), gridItemWidth);
		photoGrid.setAdapter(this.photoAdapter_);
	}
	
	public class SelectBackButtonHandler implements BackButtonPressInterrupter {
		@Override
		public boolean interruptBackButton() {
			cartManager_.cleanUpPendingImages();
			return false;
		}
	}
	
	public class SelectNextButtonHandler implements NextButtonPressInterrupter {
		@Override
		public boolean interruptNextButton() {
			ArrayList<KPhoto> selectedPhotos = photoAdapter_.getSelectedPhotos();
			if (selectedPhotos.size() == 0) {
				return true;
			} else {
				for (KPhoto photo : selectedPhotos)
					cartManager_.processPartnerImage(photo);
			}
			return false;
		}
	}
}
