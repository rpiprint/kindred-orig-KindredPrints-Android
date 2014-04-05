package com.kindredprints.android.sdk.fragments;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.adapters.ProductListAdapter;
import com.kindredprints.android.sdk.customviews.DeleteButtonView;
import com.kindredprints.android.sdk.customviews.KindredAlertDialog;
import com.kindredprints.android.sdk.customviews.SideArrow;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.CartObject;
import com.kindredprints.android.sdk.data.Size;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CartPageFragment extends KindredFragment {
	private InterfacePrefHelper interfacePrefHelper_;
	private CartManager cartManager_;
	private ImageManager imageManager_;
	
	private CartObject currObject_;
	
	private int currIndex_;
	private CartPageUpdateListener callback_;
	
	private ImageView imgWarning_;
	private SideArrow cmdLeft_;
	private SideArrow cmdRight_;
	private DeleteButtonView cmdDelete_;
	private ImageView imgPreview_;
	private TextView txtImageCount_;
	private ListView lvProducts_;
	private ProductListAdapter productListAdapter_;
	
	public CartPageFragment() {
		this.cartManager_ = CartManager.getInstance(getActivity());
		this.imageManager_ = ImageManager.getInstance(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = (ViewGroup) inflater.inflate(R.layout.fragment_cart_page, container, false);
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(getActivity());
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		this.cmdLeft_ = (SideArrow) view.findViewById(R.id.cmdLeft);
		this.cmdRight_ = (SideArrow) view.findViewById(R.id.cmdRight);
		this.cmdLeft_.setDirection(SideArrow.LEFT_ARROW);
		this.cmdRight_.setDirection(SideArrow.RIGHT_ARROW);
		this.cmdLeft_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (callback_ != null) callback_.userClickedGoPrevPage();
			}
		});
		this.cmdRight_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (callback_ != null) callback_.userClickedGoNextPage();
			}
		});
		if (currIndex_ == 0) {
			this.cmdLeft_.setVisibility(View.INVISIBLE);
		}
		if (currIndex_ == this.cartManager_.countOfOrders()-1) {
			this.cmdRight_.setVisibility(View.INVISIBLE);
		}
		this.imgWarning_ = (ImageView) view.findViewById(R.id.imgWarning);
		this.cmdDelete_ = (DeleteButtonView) view.findViewById(R.id.cmdDelete);
		this.imgPreview_ = (ImageView) view.findViewById(R.id.imgPreview);
		this.txtImageCount_ = (TextView) view.findViewById(R.id.txtCount);
		this.lvProducts_ = (ListView) view.findViewById(R.id.lvAvailableProducts);
		this.lvProducts_.setBackgroundColor(Color.TRANSPARENT);
		this.lvProducts_.setAdapter(this.productListAdapter_);
		
		this.txtImageCount_.setTextColor(this.interfacePrefHelper_.getTextColor());
				
		this.cmdDelete_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				cleanUp();
				imageManager_.deleteAllImagesFromCache(currObject_);
				if (callback_ != null) callback_.userDeletedPageAtIndex(currIndex_);
			}
		});
		this.imgWarning_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				KindredAlertDialog dialog = new KindredAlertDialog(getActivity(), false);
				dialog.show();
			}
		});
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
	
	public void setCartPageUpdateListener(CartPageUpdateListener listener) {
		this.callback_ = listener;
	}
	public void setCurrIndex(int index) {
		this.currIndex_ = index;
	}
	
	public String getUniqueId() {
		if (this.currObject_ != null)
			return this.currObject_.getImage().getId();
		else
			return "";
	}
	
	public void refreshProductList() {
		this.currObject_ = this.cartManager_.getOrderForIndex(this.currIndex_);
		this.txtImageCount_.setText("Picture " + String.valueOf(currIndex_+1) + " of " + String.valueOf(this.cartManager_.countOfOrders()));
		this.productListAdapter_.notifyDataSetChanged();
		if (this.productListAdapter_.needShowWarning()) {
			this.imgWarning_.setVisibility(View.VISIBLE);
		} else {
			this.imgWarning_.setVisibility(View.INVISIBLE);
		}
	}
	
	private void initInterface() {
		if (this.getView() == null || getActivity() == null) 
			return;
		
		this.currObject_ = this.cartManager_.getOrderForIndex(this.currIndex_);
		this.txtImageCount_.setText("Picture " + String.valueOf(currIndex_+1) + " of " + String.valueOf(this.cartManager_.countOfOrders()));
		
		float imgWidth = this.getView().getWidth()-2*getActivity().getResources().getDimensionPixelSize(R.dimen.cart_page_image_side_padding);
		if (this.currObject_.getPrintProducts().size() > 0) {
			this.imageManager_.setImageAsync(this.imgPreview_, this.currObject_.getImage(), this.currObject_.getPrintProducts().get(0), new Size(imgWidth, imgWidth));
		} else {
			this.imageManager_.setImageAsync(this.imgPreview_, this.currObject_.getImage(), null, new Size(imgWidth, imgWidth));
		}
		this.productListAdapter_ = new ProductListAdapter(getActivity(), this.currObject_);
		this.productListAdapter_.setCartPageUpdateListener(new CartPageUpdateListener() {
			@Override
			public void userDeletedPageAtIndex(int index) { }
			@Override
			public void userChangedOrderTotalBy(int deltaTotal) {
				callback_.userChangedOrderTotalBy(deltaTotal);
			}
			@Override
			public void userClickedGoPrevPage() { }
			@Override
			public void userClickedGoNextPage() { }
		});
		this.lvProducts_.setAdapter(this.productListAdapter_);
		if (this.productListAdapter_.needShowWarning()) {
			this.imgWarning_.setVisibility(View.VISIBLE);
		} else {
			this.imgWarning_.setVisibility(View.INVISIBLE);
		}
	}
	
	public void cleanUp() {
		this.imgPreview_.setImageBitmap(null);
		this.lvProducts_.setAdapter(null);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		cleanUp();
	}
	
	public interface CartPageUpdateListener {
		public void userDeletedPageAtIndex(int index);
		public void userChangedOrderTotalBy(int deltaTotal);
		public void userClickedGoPrevPage();
		public void userClickedGoNextPage();
	}
}
