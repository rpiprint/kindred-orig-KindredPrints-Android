package com.kindred.kindredprints_android_sdk.fragments;

import com.kindred.kindredprints_android_sdk.R;
import com.kindred.kindredprints_android_sdk.customviews.KindredAlertDialog;
import com.kindred.kindredprints_android_sdk.customviews.OrderTotalView;
import com.kindred.kindredprints_android_sdk.data.CartManager;
import com.kindred.kindredprints_android_sdk.data.CartObject;
import com.kindred.kindredprints_android_sdk.data.CartUpdatedCallback;
import com.kindred.kindredprints_android_sdk.data.PartnerImage;
import com.kindred.kindredprints_android_sdk.data.PrintProduct;
import com.kindred.kindredprints_android_sdk.fragments.CartPageFragment.CartPageUpdateListener;
import com.kindred.kindredprints_android_sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindred.kindredprints_android_sdk.helpers.prefs.InterfacePrefHelper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CartViewPagerFragment extends KindredFragment {
	public static final String NO_IMAGES_SELECTED = "Please select select at least 1 image to continue";
	
	private ViewPager cartFlipper_;
	private CartPageAdapter cartDataAdapter_;
	private int currIndex_;
	
	private KindredFragmentHelper fragmentHelper_;
	
	private TextView txtEmptyCart_;
	private OrderTotalView orderTotalView_;
	
	private CartManager cartManager_;
	
	private boolean continueCheck_;
	private boolean isInit = false;
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		this.cartManager_ = CartManager.getInstance(activity);
		this.cartManager_.setCartUpdatedCallback(new CartObjectsUpdatedListener());
		
		this.currIndex_ = 0;
		fragmentHelper.setNextButtonDreamCatcher_(new CartNextButtonHandler());
		fragmentHelper.setBackButtonDreamCatcher_(null);
		fragmentHelper.configNavBar();
		this.fragmentHelper_ = fragmentHelper;
		this.continueCheck_ = false;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.fragment_cart_view_pager, container, false);
		InterfacePrefHelper interfacePrefHelper = new InterfacePrefHelper(getActivity());
		view.setBackgroundColor(interfacePrefHelper.getBackgroundColor());
		this.orderTotalView_ = (OrderTotalView) view.findViewById(R.id.orderTotal);
		this.txtEmptyCart_ = (TextView) view.findViewById(R.id.txtEmptyCart);
		this.txtEmptyCart_.setTextColor(interfacePrefHelper.getTextColor());
		this.cartFlipper_ = (ViewPager) view.findViewById(R.id.viewPager);
				
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (!this.isInit) {
			this.isInit = true;
			showAppropriateView();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		this.isInit = false;
	}
	
	private void showAppropriateView() {
		if (this.cartManager_.countOfOrders() > 0) {
			addToOrderTotal(this.cartManager_.getOrderTotal());
						
			this.txtEmptyCart_.setVisibility(TextView.INVISIBLE);

			this.cartDataAdapter_ = new CartPageAdapter(getActivity().getSupportFragmentManager());
			
			this.cartDataAdapter_.notifyDataSetChanged();
			this.cartFlipper_.setAdapter(this.cartDataAdapter_);
			this.cartFlipper_.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageScrollStateChanged(int arg0) {}
				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) { }

				@Override
				public void onPageSelected(int position) {
					currIndex_ = position;
				}
			});
			this.cartFlipper_.setCurrentItem(this.currIndex_);
		} else {
			this.txtEmptyCart_.setVisibility(TextView.VISIBLE);
		}
	}
	
	private void addToOrderTotal(int deltaTotal) {
		this.orderTotalView_.setOrderTotal(this.orderTotalView_.getOrderTotal()+deltaTotal);
	}
	
	public void cleanUp() {
		if (cartFlipper_ != null) this.cartFlipper_.setAdapter(null);
		CartManager.getInstance(getActivity()).setCartUpdatedCallback(null);
		KindredFragmentHelper.getInstance().setNextButtonDreamCatcher_(null);
		KindredFragmentHelper.getInstance().setBackButtonDreamCatcher_(null);
	}
	
	private class CartNextButtonHandler implements NextButtonPressInterrupter {

		@Override
		public boolean interruptNextButton() {
			if (cartManager_.generateSelectedOrdersFromBaseOrders() && !continueCheck_) {
				KindredAlertDialog kad = new KindredAlertDialog(getActivity(), true);
				kad.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						continueCheck_ = true;
						fragmentHelper_.moveNextFragment();
					}
				});
				kad.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface arg0) {
						continueCheck_ = true;
					}
				});
				kad.show();
				return true;
			}
			
			if (cartManager_.countOfSelectedOrders() == 0) {
				KindredAlertDialog kad = new KindredAlertDialog(getActivity(), NO_IMAGES_SELECTED, false);
				kad.show();
				return true;
			}
			
			cleanUp();
			return false;
		}
		
	}
	
	private class CartPageAdapter extends FragmentPagerAdapter {
		public CartPageAdapter(FragmentManager fm) {
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
			CartPageFragment pageFrag = new CartPageFragment();
			pageFrag.setCurrIndex(position);
			pageFrag.setCartPageUpdateListener(new CartPageUpdateListener() {
				@Override
				public void userDeletedPageAtIndex(int index) {
					CartObject delOrder = cartManager_.getOrderForIndex(index);
					int deltaOrder = 0;
					for (PrintProduct product : delOrder.getPrintProducts()) {
						deltaOrder = deltaOrder - product.getQuantity() * product.getPrice();
					}
					addToOrderTotal(deltaOrder);
					cartFlipper_.setAdapter(null);
					cartManager_.deleteOrderImageAtIndex(index);
					cartDataAdapter_.notifyDataSetChanged();
					cartFlipper_.setAdapter(cartDataAdapter_);

					if (currIndex_ == getCount()) {
						currIndex_ = currIndex_ - 1;
					} 
					if (currIndex_ >= 0)
					cartFlipper_.setCurrentItem(currIndex_, true);
					else {
						txtEmptyCart_.setVisibility(TextView.VISIBLE);
					}
				}
				@Override
				public void userChangedOrderTotalBy(int deltaTotal) {
					addToOrderTotal(deltaTotal);
				}
				@Override
				public void userClickedGoPrevPage() {
					if (currIndex_ > 0) {
						cartFlipper_.setCurrentItem(currIndex_-1, true);
					}
				}
				@Override
				public void userClickedGoNextPage() {
					if (currIndex_ < getCount()-1) {
						cartFlipper_.setCurrentItem(currIndex_+1, true);
					}
				}
			});
			return pageFrag;
		}
		
		public void removeFragmentByPosition(int pos) {
            String tag = "android:switcher:" + cartFlipper_.getId() + ":" + pos;
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.remove((CartPageFragment)getActivity().getSupportFragmentManager().findFragmentByTag(tag));
            ft.commit();
		}
		public CartPageFragment getFragmentByPosition(int pos) {
            String tag = "android:switcher:" + cartFlipper_.getId() + ":" + pos;
            return (CartPageFragment)getActivity().getSupportFragmentManager().findFragmentByTag(tag);
		}
		
		@Override
		public int getCount() {
			return cartManager_.countOfOrders();
		}
		
	}
	
	
	private class CartObjectsUpdatedListener implements CartUpdatedCallback {

		@Override
		public void ordersHaveAllBeenUpdated() {
			if (cartDataAdapter_ != null) {
				Log.i("KindredSDK","orders have ALL been updated with size");
				for (int i = Math.max(currIndex_-1, 0); i < Math.min(currIndex_+2, cartDataAdapter_.getCount()); i++) {
					CartPageFragment f = cartDataAdapter_.getFragmentByPosition(i);
					if (f != null) {
						Log.i("KindredSDK","calling refresh on index " + i);

						f.refreshProductList();
					}
				}
			}
		}

		@Override
		public void orderHasBeenUpdatedWithSize(PartnerImage obj) {
			if (cartDataAdapter_ != null) {
				if (obj == null)
					return;
				for (int i = 0; i < cartManager_.countOfOrders(); i++) {
					CartObject cObj = cartManager_.getOrderForIndex(i);
					if (cObj.getImage().getId().equalsIgnoreCase(obj.getId())) {
						if (i < currIndex_+2 && i > currIndex_-2) {
							Log.i("KindredSDK","calling refresh on index " + i);

							CartPageFragment f = cartDataAdapter_.getFragmentByPosition(i);
							if (f != null) {
								f.refreshProductList();
							}
						}
						break;
					}
				}
			}
		}

		@Override
		public void orderHasBeenServerInit(PartnerImage obj) { }
		@Override
		public void orderHasBeenUploaded(PartnerImage obj) { }
	}
}
