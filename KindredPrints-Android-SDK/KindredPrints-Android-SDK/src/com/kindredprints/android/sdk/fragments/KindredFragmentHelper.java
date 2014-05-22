package com.kindredprints.android.sdk.fragments;

import java.util.LinkedList;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.customviews.NavBarView;
import com.kindredprints.android.sdk.customviews.NetworkProgressBar;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.UserObject;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class KindredFragmentHelper {
	public static final String FRAG_INTRO = "kp_fragment_intro";
	public static final String FRAG_SELECT = "kp_fragment_select";
	public static final String FRAG_PREVIEW = "kp_fragment_preview";
	public static final String FRAG_CART = "kp_fragment_cart";
	public static final String FRAG_LOGIN = "kp_fragment_login";
	public static final String FRAG_SHIPPING = "kp_fragment_shipping";
	public static final String FRAG_SHIPPING_EDIT = "kp_fragment_shipping_edit";
	public static final String FRAG_ORDER_SUMMARY = "kp_order_summary";
	public static final String FRAG_ORDER_CARD_EDIT = "kp_order_card_edit";
	public static final String FRAG_ORDER_FINISHED = "kp_order_finished";
	
	
	private DevPrefHelper devPrefHelper_;
	private UserPrefHelper userPrefHelper_;
	private String currFragHash_;
	private FragmentManager fManager_;
	private LinkedList<String> backStack_;
	private NavBarView navBarView_;
	private Resources resources_;
	private Activity activity_;
	
	private CartManager cartManager_;
	
	private NetworkProgressBar progBar_;
	
	private NextButtonPressInterrupter nextInterrupted_;
	private BackButtonPressInterrupter backInterrupted_;
	
	private static KindredFragmentHelper fragmentHelper_;
	
	public KindredFragmentHelper(Activity activity) {
		this.backStack_ = new LinkedList<String>();
		this.activity_ = activity;
		this.cartManager_ = CartManager.getInstance(activity);
	}
	
	public static KindredFragmentHelper getInstance() {
		return fragmentHelper_;
	}
	
	public static KindredFragmentHelper getInstance(FragmentManager fManager, NavBarView navBarView, Activity activity) {
		if (fragmentHelper_ == null) {
			fragmentHelper_ = new KindredFragmentHelper(activity);
		}
		fragmentHelper_.navBarView_ = navBarView;
		fragmentHelper_.fManager_ = fManager;
		fragmentHelper_.resources_ = activity.getResources();
		return fragmentHelper_;
	}
	
	public void updateActivity(Activity activity) {
		this.activity_ = activity;
		this.userPrefHelper_ = new UserPrefHelper(activity);
		this.devPrefHelper_ = new DevPrefHelper(activity);
		this.progBar_ = new NetworkProgressBar(activity);
	}
	
	public void setNextButtonDreamCatcher_(NextButtonPressInterrupter interrupter_) {
		this.nextInterrupted_ = interrupter_;
	}
	
	public void setBackButtonDreamCatcher_(BackButtonPressInterrupter interrupter_) {
		this.backInterrupted_ = interrupter_;
	}
	
	public void setNextButtonEnabled(boolean enabled) {
		this.navBarView_.setNextButtonEnabled(enabled);
	}
	
	public void setNextButtonVisible(boolean visible) {
		this.navBarView_.setNextButtonVisible(visible);
	}
	
	public void setNextButtonCartType(boolean cart) {
		if (cart) {
			this.navBarView_.setNextButtonType(NavBarView.TYPE_CART_BUTTON, this.cartManager_.countOfSelectedOrders());
		} else {
			this.navBarView_.setNextButtonType(NavBarView.TYPE_NEXT_BUTTON, this.cartManager_.countOfSelectedOrders());
		}
	}
	
	public void triggerNextButton() {
		this.navBarView_.triggerNextButton();
	}
	
	public void triggerBackButton() {
		this.navBarView_.triggerBackButton();
	}
	
	public void goBackAndExit() {
		this.backStack_.clear();
		triggerBackButton();
	}
	
	public void showProgressBarWithMessage(String message) {
		this.progBar_.show(message, 0.5f);
	}
	
	public void updateProgressBarWithProgress(float progress) {
		this.progBar_.change_progress(progress);
	}
	
	public void updateProgressBarWithMessage(String message) {
		this.progBar_.change_message(message);
	}
	
	public void hideProgressBar() {
		this.progBar_.hide();
	}
	
	public void initRootFragment() {
		this.backStack_.clear();
		KindredFragment f = null;
		if (this.devPrefHelper_.getSeenIntroStatus()) {
			int numPending = this.cartManager_.getPendingImages().size();
			if (numPending > 0) {
				if (numPending > 1) {
					this.currFragHash_ = FRAG_SELECT;
				} else {
					Log.i("KindredSDK", "init root fragment to preview screen");
					this.currFragHash_ = FRAG_PREVIEW;
				}
			} else {
				this.currFragHash_ = FRAG_CART;
			}
		} else {
			this.currFragHash_ = FRAG_INTRO;
		}
		f = fragForHash(this.currFragHash_);
		f.initFragment(this, this.activity_);
		moveRightFragment(f);
	}
	
	public void configNavBar() {
		configNavBarForHash(this.currFragHash_);
	}
	
	public boolean moveToFragment(String hash) {
		return moveToFragmentWithBundle(hash, new Bundle());
	}
	
	public boolean moveToFragmentWithBundle(String hash, Bundle bun) {
		KindredFragment f = fragForHash(hash);
		if (f != null) {
			this.backStack_.add(this.currFragHash_);
			this.currFragHash_ = hash;
			f.setArguments(bun);
			f.initFragment(this, this.activity_);
			moveRightFragment(f);
			return true;
		}
		return false;
	}
	
	public boolean replaceCurrentFragmentWithFragmentAndBundle(String hash, Bundle bun) {
		KindredFragment f = fragForHash(hash);
		if (f != null) {
			f.setArguments(bun);
			f.initFragment(this, this.activity_);
			this.currFragHash_ = hash;
			moveRightFragment(f);
			return true;
		}
		return false;
	}
	
	public boolean replaceCurrentFragmentWithFragment(String hash) {
		return replaceCurrentFragmentWithFragmentAndBundle(hash, new Bundle());
	}
	
	public boolean moveNextFragmentWithBundle(Bundle bun) {
		if (this.nextInterrupted_ != null) {
			if (this.nextInterrupted_.interruptNextButton()) {
				return true;
			}
		} 
		
		String nextFrag = null;
		if (this.currFragHash_.equals(FRAG_CART)) {			
			if (this.userPrefHelper_.getUserObject().getId().equals(UserObject.USER_VALUE_NONE)) {
				nextFrag = FRAG_LOGIN;
			} else {
				nextFrag = FRAG_ORDER_SUMMARY;
			}
		} else if (this.currFragHash_.equals(FRAG_LOGIN)) {
			nextFrag = FRAG_ORDER_SUMMARY;
		} else if (this.currFragHash_.equals(FRAG_SHIPPING_EDIT)) {
			return moveLastFragmentWithBundle(bun);
		} else if (this.currFragHash_.equals(FRAG_SHIPPING)) {
			return moveLastFragmentWithBundle(bun);
		} else if (this.currFragHash_.equals(FRAG_ORDER_SUMMARY)) {
			nextFrag = FRAG_ORDER_FINISHED;
		} else if (this.currFragHash_.equals(FRAG_ORDER_CARD_EDIT)) { 
			return moveLastFragmentWithBundle(bun);
		} else if (this.currFragHash_.equals(FRAG_SELECT)) {
			this.backStack_.clear();
			return replaceCurrentFragmentWithFragmentAndBundle(FRAG_CART, bun);
		} else if (this.currFragHash_.equals(FRAG_PREVIEW)) {
			this.backStack_.clear();
			return replaceCurrentFragmentWithFragmentAndBundle(FRAG_CART, bun);
		} else if (this.currFragHash_.equals(FRAG_INTRO)) {
			initRootFragment();
			return true;
		}
		if (nextFrag != null) {
			KindredFragment f = fragForHash(nextFrag);
			if (f != null) {
				f.setArguments(bun);
				f.initFragment(this, this.activity_);
				this.backStack_.add(this.currFragHash_);
				this.currFragHash_ = nextFrag;
				moveRightFragment(f);
				return true;
			}
		}
		return false;
	}
	
	public boolean moveNextFragment() {
		return moveNextFragmentWithBundle(new Bundle());
	}
	
	public boolean moveLastFragmentWithBundle(Bundle bun) {
		if (this.backInterrupted_ != null) {
			if (this.backInterrupted_.interruptBackButton()) {
				return true;
			}
		}
		
		if (!this.backStack_.isEmpty()) {
			this.currFragHash_ = this.backStack_.removeLast();
			if (this.currFragHash_.equals(FRAG_LOGIN) && !this.userPrefHelper_.getUserObject().getId().equals(UserObject.USER_VALUE_NONE)) {
				this.currFragHash_ = this.backStack_.remove();
			}
			KindredFragment f = fragForHash(this.currFragHash_);
			f.initFragment(this, this.activity_);
			f.setArguments(bun);
			moveLeftFragment(f);
			return true;
		} else {
			KindredFragment f = fragForHash(this.currFragHash_);
			FragmentTransaction ft = this.fManager_.beginTransaction();
			ft.remove(f);
			ft.commit();
			this.cartManager_.cleanUpPendingImages();
		}
		return false;
	}
	
	public boolean moveLastFragment() {
		return moveLastFragmentWithBundle(new Bundle());
	}
	
	private void moveRightFragment(KindredFragment f) {
		setNextButtonEnabled(true);
		FragmentTransaction ft = this.fManager_.beginTransaction();
		ft.setCustomAnimations(R.anim.anim_slide_in, R.anim.anim_slide_out);
		ft.replace(R.id.fragmentHolder, f, this.currFragHash_);
		ft.commit();
	}
	
	private void moveLeftFragment(KindredFragment f) {
		setNextButtonEnabled(true);
		FragmentTransaction ft = this.fManager_.beginTransaction();
		ft.setCustomAnimations(R.anim.anim_slide_in_back, R.anim.anim_slide_out_back);
		ft.replace(R.id.fragmentHolder, f, this.currFragHash_);
		ft.commit();
	}
	
	private KindredFragment fragForHash(String hash) {
		KindredFragment f = null;
		if (hash.equals(FRAG_CART)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_CART);
			if (f == null) {
				return new CartItemListFragment();
			}
		} else if (hash.equals(FRAG_LOGIN)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_LOGIN);
			if (f == null) {
				return new LoginViewFragment();
			}
		} else if (hash.equals(FRAG_SHIPPING)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_SHIPPING);
			if (f == null) {
				return new ShippingListFragment();
			}
		} else if (hash.equals(FRAG_SHIPPING_EDIT)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_SHIPPING_EDIT);
			if (f == null) {
				return new ShippingEditFragment();
			}
		} else if (hash.equals(FRAG_ORDER_SUMMARY)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_ORDER_SUMMARY);
			if (f == null) {
				return new OrderSummaryFragment();
			}
		} else if (hash.equals(FRAG_ORDER_CARD_EDIT)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_ORDER_CARD_EDIT);
			if (f == null) {
				return new CardEditFragment();
			}
		} else if (hash.equals(FRAG_ORDER_FINISHED)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_ORDER_FINISHED);
			if (f == null) {
				return new OrderCompleteFragment();
			}
		} else if (hash.equals(FRAG_SELECT)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_SELECT);
			if (f == null) {
				return new PhotoSelectFragment();
			}
		} else if (hash.equals(FRAG_PREVIEW)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_PREVIEW);
			if (f == null) {
				return new CartPreviewFragment();
			}
		} else if (hash.equals(FRAG_INTRO)) {
			f = (KindredFragment) this.fManager_.findFragmentByTag(FRAG_INTRO);
			if (f == null) {
				return new IntroPageFlipperFragment();
			}
		}
		return f;
	}
	
	public void configNavBarForHash(String hash) {
		Log.i("KindredSDK", "config nav bar for hash " + hash);
		if (hash.equals(FRAG_CART)) {
			this.navBarView_.setNextButtonType(NavBarView.TYPE_NEXT_BUTTON, this.cartManager_.countOfSelectedOrders());
			this.navBarView_.setNextTitle(this.resources_.getString(R.string.nav_next_title_cart));
		} else if (hash.equals(FRAG_LOGIN)) {
			this.navBarView_.setNextTitle(this.resources_.getString(R.string.nav_next_title_login));
		} else if (hash.equals(FRAG_LOGIN+String.valueOf(LoginViewFragment.STATE_NEED_PASSWORD))) {
			this.navBarView_.setNextTitle(this.resources_.getString(R.string.nav_next_title_login));
		} else if (hash.equals(FRAG_LOGIN+String.valueOf(LoginViewFragment.STATE_WRONG_PASSWORD))) {
			this.navBarView_.setNextTitle(this.resources_.getString(R.string.nav_next_title_login_reset));
		} else if (hash.equals(FRAG_SHIPPING) || hash.equals(FRAG_ORDER_CARD_EDIT)) {
			this.navBarView_.setNextTitle(this.resources_.getString(R.string.nav_next_title_shipping));
		} else if (hash.equals(FRAG_SHIPPING_EDIT)) {
			this.navBarView_.setNextTitle(this.resources_.getString(R.string.nav_next_title_edit_shipping));
		} else if (hash.equals(FRAG_ORDER_SUMMARY)) {
			this.navBarView_.setNextTitle(this.resources_.getString(R.string.nav_next_title_purchase));
		} else if (hash.equals(FRAG_ORDER_FINISHED)) {
			this.navBarView_.setNextTitle("");
		} else if (hash.equals(FRAG_SELECT)) {
			this.navBarView_.setNextButtonType(NavBarView.TYPE_CART_BUTTON, this.cartManager_.countOfSelectedOrders());
		} else if (hash.equals(FRAG_PREVIEW)) {
			this.navBarView_.setNextButtonType(NavBarView.TYPE_CART_BUTTON, this.cartManager_.countOfSelectedOrders());
		} else if (hash.equals(FRAG_INTRO)) {
			this.navBarView_.setNextButtonType(NavBarView.TYPE_NEXT_BUTTON, this.cartManager_.countOfSelectedOrders());
			this.navBarView_.setNextTitle(this.resources_.getString(R.string.nav_next_title_intro));
		}
	}
	
	public interface NextButtonPressInterrupter {
		public boolean interruptNextButton();
	}
	
	public interface BackButtonPressInterrupter {
		public boolean interruptBackButton();
	}
}
