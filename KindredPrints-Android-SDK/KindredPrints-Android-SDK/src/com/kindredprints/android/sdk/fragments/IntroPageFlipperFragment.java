package com.kindredprints.android.sdk.fragments;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.CartUpdatedCallback;
import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class IntroPageFlipperFragment extends KindredFragment {
	private ViewPager pageFlipper_;
	private IntroPageAdapter introDataAdapter_;
	private Button cmdNext_;
	private ArrayList<String> pageUrls_;
	private ArrayList<String> introTitles_;
	private int currIndex_;
	
	private InterfacePrefHelper interfacePrefHelper_;
	
	private CartManager cartManager_;
	private ImageManager imManager_;
	private DevPrefHelper devPrefHelper_;
	
	private Context context_;
	private Timer pageFlipTimer_;
	
	private MixpanelAPI mixpanel_;
	
	private boolean scrollIdle_;
	
	private KindredFragmentHelper fragmentHelper_;

	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		context_ = activity.getApplicationContext();
		
		this.imManager_ = ImageManager.getInstance(context_);
		this.devPrefHelper_ = new DevPrefHelper(context_);
		this.interfacePrefHelper_ = new InterfacePrefHelper(context_);
		this.pageUrls_ = this.devPrefHelper_.getIntroUrls();
		this.introTitles_ = new ArrayList<String>();
		this.introTitles_.add(activity.getResources().getString(R.string.intro_page_one));
		this.introTitles_.add(activity.getResources().getString(R.string.intro_page_two));
		this.introTitles_.add(activity.getResources().getString(R.string.intro_page_three));
		this.introTitles_.add(activity.getResources().getString(R.string.intro_page_four));
		
		mixpanel_ = MixpanelAPI.getInstance(activity, activity.getResources().getString(R.string.mixpanel_token));
		mixpanel_.track("intro_page_view", null);

		this.cartManager_ = CartManager.getInstance(activity);
		this.cartManager_.setCartUpdatedCallback(new IntroUpdatedListener());
		
		this.scrollIdle_ = true;
		this.currIndex_ = 0;
		fragmentHelper.setNextButtonDreamCatcher_(new IntroNextButtonHandler());
		fragmentHelper.setBackButtonDreamCatcher_(null);
		fragmentHelper.configNavBar();
		this.fragmentHelper_ = fragmentHelper;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_intro_page_flipper, container, false);
		InterfacePrefHelper interfacePrefHelper = new InterfacePrefHelper(this.context_);
		view.setBackgroundColor(interfacePrefHelper.getBackgroundColor());

		this.pageFlipper_ = (ViewPager) view.findViewById(R.id.viewPager);
		this.introDataAdapter_ = new IntroPageAdapter(getActivity().getSupportFragmentManager());

		this.cmdNext_ = (Button) view.findViewById(R.id.cmdStart);
		this.cmdNext_.setTextColor(this.interfacePrefHelper_.getHighlightTextColor());
		this.cmdNext_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MixpanelAPI.getInstance(context_, context_.getResources().getString(R.string.mixpanel_token)).track("intro_page_start_click", null);
				fragmentHelper_.triggerNextButton();
			}
		});
		
		this.introDataAdapter_.notifyDataSetChanged();
		this.pageFlipper_.setAdapter(this.introDataAdapter_);
		this.pageFlipper_.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int scrollState) {
				if (scrollState == ViewPager.SCROLL_STATE_DRAGGING || scrollState == ViewPager.SCROLL_STATE_SETTLING) {
					scrollIdle_ = false;
				} else {
					scrollIdle_ = true;
				}
			} 
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) { }

			@Override
			public void onPageSelected(int position) {
				currIndex_ = position;
			}
		});
		this.pageFlipper_.setCurrentItem(this.currIndex_);
		
		startFlips();
		
		return view;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (this.pageFlipTimer_ != null)
			this.pageFlipTimer_.cancel();
	}
	
	private void startFlips() {
		this.pageFlipTimer_ = new Timer();
		this.pageFlipTimer_.schedule(new TimerTask() {
			@Override
			public void run() {
				if (scrollIdle_) {
					Handler mainHandler = new Handler(getActivity().getMainLooper());
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							int count = 4;
							if (pageUrls_.size() > 0)
								count = pageUrls_.size();
							currIndex_ = (currIndex_+1)%count;
							pageFlipper_.setCurrentItem(currIndex_, true);
						}
					});
				}
			}
		}, 0, 3000);
	}
	
	private class IntroNextButtonHandler implements NextButtonPressInterrupter {

		@Override
		public boolean interruptNextButton() {
			devPrefHelper_.setSeenIntroStatus();
			for (String url : pageUrls_) {
				String[] sections = url.split("/");
				String pid = sections[sections.length-1];
				
				imManager_.deleteFromCache(pid);
			}
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
			JSONObject introPage = new JSONObject();
			try {
				introPage.put("page_index", position);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			MixpanelAPI.getInstance(context_, context_.getResources().getString(R.string.mixpanel_token)).track("intro_page_view", introPage);
			pageFrag.setBackgroundImage(pageUrls_.get(position), introTitles_.get(position%introTitles_.size()));
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
