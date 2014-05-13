package com.kindredprints.android.sdk.fragments;

import com.kindredprints.android.sdk.KPhoto;
import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.customviews.KindredAlertDialog;
import com.kindredprints.android.sdk.customviews.QuantityView;
import com.kindredprints.android.sdk.customviews.QuantityView.QuantityChangedListener;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.CartUpdatedCallback;
import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.data.PrintableImage;

import com.kindredprints.android.sdk.data.Size;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.BackButtonPressInterrupter;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper.NextButtonPressInterrupter;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.cache.ImageManager.ImageManagerCallback;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartPreviewFragment extends KindredFragment {
	private InterfacePrefHelper interfacePrefHelper_;
	private CartManager cartManager_;
	private ImageManager imageManager_;
	
	private Context context_;
	
	private PrintableImage currObject_;
	private PrintProduct currProduct_;
	private KindredFragmentHelper fragmentHelper_;
	
	private boolean frontSideUp_;
	
	private ProgressBar progBar_;
	private QuantityView quantityView_;
	private Button cmdAddToCart_;
	private ImageView imgWarning_;
	private Button cmdFlip_;
	private ImageView imgPreview_;
	private TextView txtTitle_;
	private TextView txtSubtitle_;
	
	private ImageManagerCallback imageSetCallback_;
	
	private boolean quantityChanged_;
	
	public CartPreviewFragment() { }
	
	public void initFragment(KindredFragmentHelper fragmentHelper, Activity activity) {
		this.context_ = activity.getApplicationContext();
		this.cartManager_ = CartManager.getInstance(this.context_);
		this.imageManager_ = ImageManager.getInstance(this.context_);
		this.fragmentHelper_ = fragmentHelper;
		this.fragmentHelper_.setNextButtonDreamCatcher_(new NextButtonHandler());
		this.fragmentHelper_.setBackButtonDreamCatcher_(new BackButtonHandler());
		
		this.cartManager_.setCartUpdatedCallback(new CartUpdatedCallback() {
			@Override
			public void ordersHaveAllBeenUpdated() { 
				if (currObject_ != null) {
					refreshProductList();
				}
			}
			@Override
			public void orderCountHasBeenUpdated() { }
			@Override
			public void orderHasBeenUpdatedWithSize(PartnerImage obj, ArrayList<PrintProduct> fittedProducts) { 
				if (currObject_ != null) {
					if (currObject_.getImage().getId().equals(obj.getId())) {
						if (fittedProducts.size() > 0) {
							currObject_.setPrintType(fittedProducts.get(0));
						}
						refreshProductList();
					}
				}
			}

			@Override
			public void orderHasBeenServerInit(PartnerImage obj) { }

			@Override
			public void orderHasBeenUploaded(PartnerImage obj) { }
		});
		this.fragmentHelper_.setNextButtonCartType(true);
		Bundle bun = this.getArguments();
		int index = 0;
		if (bun != null && bun.containsKey("cart_index")) {
			index = bun.getInt("cart_index");
			this.currObject_ = this.cartManager_.getSelectedOrderForIndex(index);
			this.fragmentHelper_.setNextButtonVisible(false);
		} else {
			if (bun != null && bun.containsKey("index")) {
				index = bun.getInt("index");
			}
			KPhoto incPhoto = this.cartManager_.getPendingImages().get(index);
			PartnerImage pImage = new PartnerImage(incPhoto);
			this.currObject_ = new PrintableImage();
			this.currObject_.setImage(pImage);
			int existIndex = this.cartManager_.hasImageInCart(this.currObject_);
			if (existIndex >= 0) {
				this.currObject_ = this.cartManager_.getSelectedOrderForIndex(existIndex);
			}
			this.cartManager_.cacheIncomingImage(pImage, incPhoto);
			this.fragmentHelper_.setNextButtonVisible(true);
		}
		if (this.currObject_.getPrintType() != null) {
			this.currProduct_ = this.currObject_.getPrintType();
		}
	}
	
	public class BackButtonHandler implements BackButtonPressInterrupter {
		@Override
		public boolean interruptBackButton() {
			
			return false;
		}
	}
	
	public class NextButtonHandler implements NextButtonPressInterrupter {
		@Override
		public boolean interruptNextButton() {
			cartManager_.cleanUpPendingImages();
			return false;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = (ViewGroup) inflater.inflate(R.layout.fragment_cart_preview, container, false);
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(getActivity());
		view.setBackgroundColor(this.interfacePrefHelper_.getBackgroundColor());
		
		this.quantityChanged_ = false;
		
		this.progBar_ = (ProgressBar) view.findViewById(R.id.progressBar);
		this.cmdAddToCart_ = (Button) view.findViewById(R.id.cmdAddUpdateCart);
		this.cmdAddToCart_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.quantityView_ = (QuantityView) view.findViewById(R.id.viewQuantity);
		if (this.currProduct_ != null)
			this.quantityView_.setQuantity(this.currProduct_.getQuantity());
		else
			this.quantityView_.setQuantity(0);
		this.cmdFlip_ = (Button) view.findViewById(R.id.cmdFlip);
		this.imgWarning_ = (ImageView) view.findViewById(R.id.imgWarning);
		this.imgPreview_ = (ImageView) view.findViewById(R.id.imgPreview);
		this.txtTitle_ = (TextView) view.findViewById(R.id.txtTitle);
		this.txtSubtitle_ = (TextView) view.findViewById(R.id.txtSubtitle);
		this.txtTitle_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.txtSubtitle_.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		setImageVisible(false);
		
		this.frontSideUp_ = true;
		this.cmdFlip_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				frontSideUp_ = !frontSideUp_;
				loadAppropriateImage();
			}
		});
		
		this.imgWarning_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				KindredAlertDialog dialog = new KindredAlertDialog(getActivity(), false);
				dialog.show();
			}
		});
		
		this.quantityView_.setQuantityChangedListener(new QuantityChangedListener() {
			@Override
			public void userChangedQuantity(int quantity) {
				quantityChanged_ = true;
				currProduct_.setQuantity(quantity);
				adjustButtonState();
			}
		});
		
		this.cmdAddToCart_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (cartManager_.hasImageInCart(currObject_) < 0) {
					cartManager_.addOrderToSelected(currObject_, currProduct_);
				} else if (currProduct_.getQuantity() == 0) {
					cartManager_.deleteSelectedOrderImageForId(currObject_.getImage().getId());
					return;
				} 
				cartManager_.imageWasUpdatedWithQuantities(currObject_.getImage(), currProduct_);
				quantityChanged_ = false;
				adjustButtonState();
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
	
	public String getUniqueId() {
		if (this.currObject_ != null)
			return this.currObject_.getImage().getId();
		else
			return "";
	}
	
	public void refreshProductList() {
		if (this.currObject_ != null && this.currObject_.getPrintType() != null) {
			this.currProduct_ = this.currObject_.getPrintType();
			if (this.quantityView_ != null) this.quantityView_.setQuantity(this.currProduct_.getQuantity());
			if (this.txtTitle_ != null) this.txtTitle_.setText(this.currProduct_.getTitle());
			DecimalFormat moneyFormat = new DecimalFormat("$0.00 each");
			float eachTotalF = (float)this.currProduct_.getPrice()/100.0f;
			if (this.txtSubtitle_ != null) this.txtSubtitle_.setText(moneyFormat.format(eachTotalF));
			if (this.cartManager_.hasImageInCart(this.currObject_) >= 0) {
				this.quantityChanged_ = false;
			}
		}
		loadAppropriateImage();
		adjustDisplay();
		adjustButtonState();
	}
	
	private void adjustButtonState() {
		if (this.quantityChanged_) {
			this.cmdAddToCart_.setEnabled(true);
			this.cmdAddToCart_.setBackgroundResource(R.drawable.cmd_rounded_blue_filled_button);
			if (this.cartManager_.hasImageInCart(this.currObject_) >= 0) {
				if (this.currProduct_.getQuantity() == 0) {
					this.cmdAddToCart_.setText(this.context_.getResources().getString(R.string.cart_remove_from_cart));
				} else {
					this.cmdAddToCart_.setText(this.context_.getResources().getString(R.string.cart_update_in_cart));
				}
			} else {
				if (this.currProduct_.getQuantity() == 0) {
					this.cmdAddToCart_.setEnabled(false);
					this.cmdAddToCart_.setBackgroundResource(R.drawable.cmd_rounded_button);
				} else {
					this.cmdAddToCart_.setText(this.context_.getResources().getString(R.string.cart_add_to_cart));	
				}
			}
		} else {
			this.cmdAddToCart_.setEnabled(false);
			this.cmdAddToCart_.setBackgroundResource(R.drawable.cmd_rounded_button);
		}
	}
	
	private void adjustDisplay() {
		if (this.currObject_ != null && this.currProduct_ != null) {
			if (this.currObject_.getImage().isTwosided()) {
				this.cmdFlip_.setVisibility(View.VISIBLE);
			} else {
				this.cmdFlip_.setVisibility(View.INVISIBLE);
			}
			if (this.currProduct_ != null && (this.currProduct_.getDpi() < this.currProduct_.getWarnDPI())) {
				this.imgWarning_.setVisibility(View.VISIBLE);
			} else {
				this.imgWarning_.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	private void setImageVisible(boolean visible) {
		if (visible) {
			this.progBar_.setVisibility(View.INVISIBLE);
			this.quantityView_.setVisibility(View.VISIBLE);
			this.cmdAddToCart_.setVisibility(View.VISIBLE);
			adjustDisplay();
		} else {
			this.cmdFlip_.setVisibility(View.INVISIBLE);
			this.imgWarning_.setVisibility(View.INVISIBLE);
			this.progBar_.setVisibility(View.VISIBLE);
			this.quantityView_.setVisibility(View.INVISIBLE);
			this.cmdAddToCart_.setVisibility(View.INVISIBLE);
		}
	}
	
	private void loadAppropriateImage() {
		if (this.currObject_ != null) {
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
	
			float padding = this.context_.getResources().getDimensionPixelSize(R.dimen.cart_page_image_side_padding);
			float imgWidth = this.getView().getWidth();//-2*padding;
			float imgHeight = this.getView().getHeight()-3*padding-this.cmdAddToCart_.getHeight();
			if (this.currProduct_ != null) {
				this.imageManager_.setImageAsync(this.imgPreview_, image, this.currProduct_, new Size(imgWidth, imgHeight), this.imageSetCallback_);
			}
		}
	}
	
	private void initInterface() {
		if (this.getView() == null || getActivity() == null) 
			return;
		loadAppropriateImage();
		adjustDisplay();
		refreshProductList();
	}
}
