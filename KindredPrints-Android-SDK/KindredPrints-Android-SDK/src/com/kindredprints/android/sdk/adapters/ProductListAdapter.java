package com.kindredprints.android.sdk.adapters;

import java.text.DecimalFormat;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.customviews.KindredAlertDialog;
import com.kindredprints.android.sdk.customviews.QuantityView;
import com.kindredprints.android.sdk.customviews.QuantityView.QuantityChangedListener;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.CartObject;
import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.fragments.CartPageFragment.CartPageUpdateListener;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductListAdapter extends BaseAdapter {

	private Activity context_;
	private CartObject currObject_;
	private ImageManager imageManager_;
	private CartManager cartManager_;
	private InterfacePrefHelper interfacePrefHelper_;
	
	private MixpanelAPI mixpanel_;
	
	private CartPageUpdateListener callback_;
	private PrintSelectedListener printClickCallback_;
	
	public ProductListAdapter(Activity context, CartObject currentObject) {
		this.context_ = context;
		this.mixpanel_ = MixpanelAPI.getInstance(context, context.getResources().getString(R.string.mixpanel_token));

		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		this.currObject_ = currentObject;
		this.cartManager_ = CartManager.getInstance(context);
		this.imageManager_ = ImageManager.getInstance(context);
	}
	
	public boolean needShowWarning() {
		for (PrintProduct product : this.currObject_.getPrintProducts()) {
			if (product.getDpi() < product.getWarnDPI()) {
				return true;
			}
		}
		return false;
	}
	
	public void setCartPageUpdateListener(CartPageUpdateListener callback) {
		this.callback_ = callback;
	}
	
	public void setPrintClickListener(PrintSelectedListener callback) {
		this.printClickCallback_ = callback;
	}
	
	@Override
	public int getCount() {
		return this.currObject_.getPrintProducts().size();
	}

	@Override
	public Object getItem(int position) {
		return this.currObject_.getPrintProducts().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View productView = null;		
		if (convertView == null) {
			LayoutInflater inflater = this.context_.getLayoutInflater();
			productView = inflater.inflate(R.layout.product_list_view, null, true);		
		} else {
			productView = convertView;
		}		
		
		productView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (printClickCallback_ != null) printClickCallback_.printWasClicked(position);
			}
		});
		
		ImageView imgWarning = (ImageView) productView.findViewById(R.id.imgWarning);
		QuantityView quantView = (QuantityView) productView.findViewById(R.id.viewQuantity);
		TextView txtTitle = (TextView) productView.findViewById(R.id.txtTitle);
		TextView txtSubtitle = (TextView) productView.findViewById(R.id.txtSubtitle);
		ImageView imgPreview = (ImageView) productView.findViewById(R.id.imgProdPrev);
		
		PrintProduct product = this.currObject_.getPrintProducts().get(position);
		
		if (product.getDpi() < product.getWarnDPI()) {
			imgWarning.setVisibility(View.VISIBLE);
			imgWarning.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					KindredAlertDialog dialog = new KindredAlertDialog(context_, false);
					dialog.show();
				}
			});
		} else {
			imgWarning.setVisibility(View.INVISIBLE);
		}
		
		txtTitle.setText(product.getTitle());
		txtTitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		DecimalFormat moneyFormat = new DecimalFormat("+ $0.00");
		float eachTotalF = (float)product.getPrice()/100.0f;
		txtSubtitle.setText(moneyFormat.format(eachTotalF));
		txtSubtitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		imgPreview.getLayoutParams().width = (int) product.getThumbSize().getWidth();
		imgPreview.getLayoutParams().height = (int) product.getThumbSize().getHeight();
		this.imageManager_.setImageAsync(imgPreview, this.currObject_.getImage(), product, product.getThumbSize(), null);
		
		quantView.setQuantityChangedListener(new QuantityChangedListener() {
			@Override
			public void userChangedQuantity(int quantity) {
				mixpanel_.track("cart_changed_quantities", null);
				int pastPrice = currObject_.getPrintProducts().get(position).getQuantity()*currObject_.getPrintProducts().get(position).getPrice();
				int newPrice = quantity*currObject_.getPrintProducts().get(position).getPrice();
				currObject_.getPrintProducts().get(position).setQuantity(quantity);
				cartManager_.imageWasUpdatedWithQuantities(currObject_.getImage(), currObject_.getPrintProducts().get(position));
				callback_.userChangedOrderTotalBy(newPrice-pastPrice);
			}
		});
		quantView.setQuantity(currObject_.getPrintProducts().get(position).getQuantity());
		return productView;
	}

	public interface PrintSelectedListener {
		public void printWasClicked(int index);
	}
}
