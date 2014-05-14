package com.kindredprints.android.sdk.fragments;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.adapters.ProductListAdapter;
import com.kindredprints.android.sdk.customviews.DeleteButtonView;
import com.kindredprints.android.sdk.customviews.KindredAlertDialog;
import com.kindredprints.android.sdk.customviews.SideArrow;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.CartObject;
import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.data.Size;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.cache.ImageManager.ImageManagerCallback;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CartPageFragment extends KindredFragment {
	private InterfacePrefHelper interfacePrefHelper_;
	private CartManager cartManager_;
	private ImageManager imageManager_;
	
	private CartObject currObject_;
	
	private int currIndex_;
	private CartPageUpdateListener callback_;
	private Context context_;
	
	private boolean frontSideUp_;
	
	private ProgressBar progBar_;
	private ImageView imgWarning_;
	private SideArrow cmdLeft_;
	private SideArrow cmdRight_;
	private DeleteButtonView cmdDelete_;
	private Button cmdFlip_;
	private ImageView imgPreview_;
	private TextView txtImageCount_;
	private ListView lvProducts_;
	private ProductListAdapter productListAdapter_;
	
	private ImageManagerCallback imageSetCallback_;
	
	public CartPageFragment() { }
	
	public void init(Context context, KindredFragmentHelper fragmentHelper) {
		getStaticHelpers(context);
	}
	
	private void getStaticHelpers(Context context) {
		if (context != null) {
			this.context_ = context;
			this.cartManager_ = CartManager.getInstance(context);
			this.imageManager_ = ImageManager.getInstance(context);	
		} else {
			this.cartManager_ = CartManager.getInstance(getActivity());
			this.imageManager_ = ImageManager.getInstance(getActivity());
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = (ViewGroup) inflater.inflate(R.layout.fragment_cart_page, container, false);
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(getActivity());
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		getStaticHelpers(null);
				
		this.progBar_ = (ProgressBar) view.findViewById(R.id.progressBar);
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
		this.cmdFlip_ = (Button) view.findViewById(R.id.cmdFlip);
		this.imgWarning_ = (ImageView) view.findViewById(R.id.imgWarning);
		this.cmdDelete_ = (DeleteButtonView) view.findViewById(R.id.cmdDelete);
		this.imgPreview_ = (ImageView) view.findViewById(R.id.imgPreview);
		this.txtImageCount_ = (TextView) view.findViewById(R.id.txtCount);
		this.lvProducts_ = (ListView) view.findViewById(R.id.lvAvailableProducts);
		this.lvProducts_.setBackgroundColor(Color.TRANSPARENT);
		this.lvProducts_.setAdapter(this.productListAdapter_);
		setImageVisible(false);

		this.frontSideUp_ = true;
		this.cmdFlip_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				frontSideUp_ = !frontSideUp_;
				loadAppropriateImage();
			}
		});
		
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
		if (this.productListAdapter_ != null) this.productListAdapter_.notifyDataSetChanged();
		loadAppropriateImage();
		adjustDisplay();
	}
	
	private void adjustDisplay() {
		if (this.currObject_.getImage().isTwosided()) {
			this.cmdFlip_.setVisibility(View.VISIBLE);
		} else {
			this.cmdFlip_.setVisibility(View.INVISIBLE);
		}
		if (this.productListAdapter_ != null && this.productListAdapter_.needShowWarning()) {
			this.imgWarning_.setVisibility(View.VISIBLE);
		} else {
			this.imgWarning_.setVisibility(View.INVISIBLE);
		}
	}
	
	private void setImageVisible(boolean visible) {
		if (visible) {
			this.txtImageCount_.setVisibility(View.VISIBLE);
			this.cmdDelete_.setVisibility(View.VISIBLE);
			this.cmdLeft_.setVisibility(View.VISIBLE);
			this.cmdRight_.setVisibility(View.VISIBLE);
			this.lvProducts_.setVisibility(View.VISIBLE);
			this.progBar_.setVisibility(View.INVISIBLE);
			adjustDisplay();
		} else {
			this.cmdFlip_.setVisibility(View.INVISIBLE);
			this.imgWarning_.setVisibility(View.INVISIBLE);
			this.txtImageCount_.setVisibility(View.INVISIBLE);
			this.cmdDelete_.setVisibility(View.INVISIBLE);
			this.cmdLeft_.setVisibility(View.INVISIBLE);
			this.cmdRight_.setVisibility(View.INVISIBLE);
			this.progBar_.setVisibility(View.VISIBLE);
			this.lvProducts_.setVisibility(View.INVISIBLE);
		}
	}
	
	private void loadAppropriateImage() {
		PartnerImage image = this.currObject_.getImage();
		if (!this.frontSideUp_ && image.isTwosided()) {
			image = image.getBackSideImage();
		}
		
		this.imageSetCallback_ = new ImageManagerCallback() {
			@Override
			public void imageAssigned() {
				setImageVisible(true);
			}
		};

		float imgWidth = this.getView().getWidth()-2*this.context_.getResources().getDimensionPixelSize(R.dimen.cart_page_image_side_padding);
		if (this.currObject_.getPrintProducts().size() > 0) {
			this.imageManager_.setImageAsync(this.imgPreview_, image, this.currObject_.getPrintProducts().get(0), new Size(imgWidth, imgWidth), this.imageSetCallback_);
		} 
	}
	
	private void initInterface() {
		if (this.getView() == null || getActivity() == null) 
			return;
		
		this.currObject_ = this.cartManager_.getOrderForIndex(this.currIndex_);
		this.txtImageCount_.setText("Picture " + String.valueOf(currIndex_+1) + " of " + String.valueOf(this.cartManager_.countOfOrders()));

		loadAppropriateImage();
		
		this.productListAdapter_ = new ProductListAdapter(getActivity(), this.currObject_);
		this.productListAdapter_.setCartPageUpdateListener(new CartPageUpdateListener() {
			@Override
			public void userDeletedPageAtIndex(int index) { }
			@Override
			public void userChangedOrderTotalBy(int deltaTotal) {
				if (callback_ != null) callback_.userChangedOrderTotalBy(deltaTotal);
			}
			@Override
			public void userClickedGoPrevPage() { }
			@Override
			public void userClickedGoNextPage() { }
		});
		this.lvProducts_.setAdapter(this.productListAdapter_);
		adjustDisplay();
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
