package com.kindredprints.android.sdk.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.CartUpdatedCallback;
import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class IntroPageFlipperFragment extends KindredFragment {
	private ViewPager pageFlipper_;
	private IntroPageAdapter introDataAdapter_;
	private ArrayList<String> pageUrls_;
	private int currIndex_;
	
	private CartManager cartManager_;
	private DevPrefHelper devPrefHelper_;
	
	private Context context_;
	
	private MixpanelAPI mixpanel_;
	
	private KindredFragmentHelper fragmentHelper_;

	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		context_ = activity.getApplicationContext();
		
		this.devPrefHelper_ = new DevPrefHelper(context_);
		this.pageUrls_ = this.devPrefHelper_.getIntroUrls();
		
		mixpanel_ = MixpanelAPI.getInstance(activity, activity.getResources().getString(R.string.mixpanel_token));
		mixpanel_.track("intro_page_view", null);

		this.cartManager_ = CartManager.getInstance(activity);
		this.cartManager_.setCartUpdatedCallback(new IntroUpdatedListener());
		
		this.currIndex_ = 0;
		fragmentHelper.setNextButtonDreamCatcher_(new IntroNextButtonHandler());
		fragmentHelper.setBackButtonDreamCatcher_(null);
		fragmentHelper.configNavBar();
		this.fragmentHelper_ = fragmentHelper;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_cart_view_pager, container, false);
		InterfacePrefHelper interfacePrefHelper = new InterfacePrefHelper(this.context_);
		view.setBackgroundColor(interfacePrefHelper.getBackgroundColor());

		this.pageFlipper_ = (ViewPager) view.findViewById(R.id.viewPager);
		this.introDataAdapter_ = new IntroPageAdapter(getActivity().getSupportFragmentManager());

		this.introDataAdapter_.notifyDataSetChanged();
		this.pageFlipper_.setAdapter(this.introDataAdapter_);
		this.pageFlipper_.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) { }

			@Override
			public void onPageSelected(int position) {
				currIndex_ = position;
			}
		});
		this.pageFlipper_.setCurrentItem(this.currIndex_);
		return view;
	}
	
	private class IntroNextButtonHandler implements NextButtonPressInterrupter {

		@Override
		public boolean interruptNextButton() {
			return false;
		}
	}
	
	private class IntroPageAdapter extends FragmentPagerAdapter {
		public IntroPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getItemPosition(Object o) {
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object o) {
			super.destroyItem(container, position, o);
			removeFragmentByPosition(position);
		}

		@Override
		public Fragment getItem(int position) {
			return createFragmentAt(position);
		}

		private Fragment createFragmentAt(int position) {
			IntroImageFragment pageFrag = new IntroImageFragment();
			pageFrag.init(context_, fragmentHelper_);
			pageFrag.setBackgroundImage(pageUrls_.get(position));
			return pageFrag;
		}

		public void removeFragmentByPosition(int pos) {
            String tag = "android:switcher:" + pageFlipper_.getId() + ":" + pos;
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.remove((IntroImageFragment)getActivity().getSupportFragmentManager().findFragmentByTag(tag));
            ft.commit();
		}
		@Override
		public int getCount() {
			return pageUrls_.size();
		}

	}


	private class IntroUpdatedListener implements CartUpdatedCallback {

		@Override
		public void introPagesHaveBeenUpdated(ArrayList<String> pageUrls) {
			if (devPrefHelper_ != null && introDataAdapter_ != null) {
				pageUrls_ = devPrefHelper_.getIntroUrls();
				introDataAdapter_.notifyDataSetChanged();
			}
		}
		@Override
		public void ordersHaveAllBeenUpdated() { }
		@Override
		public void orderCountHasBeenUpdated() { }
		@Override
		public void orderHasBeenUpdatedWithSize(PartnerImage obj, ArrayList<PrintProduct> fittedList) { }
		@Override
		public void orderHasBeenServerInit(PartnerImage obj) { }
		@Override
		public void orderHasBeenUploaded(PartnerImage obj) { }
	}
}
