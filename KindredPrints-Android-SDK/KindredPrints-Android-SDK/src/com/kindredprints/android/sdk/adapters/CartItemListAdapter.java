package com.kindredprints.android.sdk.adapters;

import java.util.ArrayList;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.customviews.KindredAlertDialog;
import com.kindredprints.android.sdk.customviews.PlusButtonView;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.data.PrintableImage;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CartItemListAdapter extends BaseAdapter {

	private Activity context_;
	private ImageManager imageManager_;
	private CartManager cartManager_;
	private InterfacePrefHelper interfacePrefHelper_;
	private DevPrefHelper devPrefHelper_;
	
	private PrintSelectedListener printClickCallback_;
	
	private ArrayList<PrintableImage> cartObjects_;
	
	public CartItemListAdapter(Activity context) {
		this.context_ = context;

		this.devPrefHelper_ = new DevPrefHelper(context);
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		this.cartManager_ = CartManager.getInstance(context);
		this.imageManager_ = ImageManager.getInstance(context);
		
		refreshCartItems();
	}
	
	public void refreshCartItems() {
		this.cartObjects_ = this.cartManager_.getSelectedOrderImages();
	}
	
	public boolean needShowWarning(PrintProduct product) {
		if (product.getDpi() < product.getWarnDPI()) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setPrintClickListener(PrintSelectedListener callback) {
		this.printClickCallback_ = callback;
	}
	
	@Override
	public int getCount() {
		return this.cartObjects_.size()+1;
	}

	@Override
	public Object getItem(int position) {
		return position;
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
			productView = inflater.inflate(R.layout.cart_item_list_view, null, true);		
		} else {
			productView = convertView;
		}		
		
		if (position < this.cartObjects_.size()) {
			productView = prepareProductView(productView, position);
		} else {
			productView = prepareAddRow(productView);
		}
		
		productView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (printClickCallback_ != null) printClickCallback_.printWasClicked(position);
			}
		});
		
		return productView;
	}
	
	private View prepareAddRow(View productView) {
		TextView txtHeaderTitle = (TextView) productView.findViewById(R.id.txtHeaderTitle);
		PlusButtonView plusButton = (PlusButtonView) productView.findViewById(R.id.cmdPlusQuantity);
		txtHeaderTitle.setVisibility(View.VISIBLE);
		plusButton.setVisibility(View.VISIBLE);
		ImageView imgProdPrev = (ImageView) productView.findViewById(R.id.imgProdPrev);
		ImageView imgWarning = (ImageView) productView.findViewById(R.id.imgWarning);
		TextView txtTitle = (TextView) productView.findViewById(R.id.txtTitle);
		TextView txtSubtitle = (TextView) productView.findViewById(R.id.txtSubtitle);
		imgProdPrev.setVisibility(View.INVISIBLE);
		imgWarning.setVisibility(View.INVISIBLE);
		txtTitle.setVisibility(View.INVISIBLE);
		txtSubtitle.setVisibility(View.INVISIBLE);
		
		txtHeaderTitle.setText(this.context_.getResources().getString(R.string.cart_add_more_image) + " " + this.devPrefHelper_.getPartnerName());
		txtHeaderTitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		return productView;
	}

	private View prepareProductView(View productView, final int position) {
		TextView txtHeaderTitle = (TextView) productView.findViewById(R.id.txtHeaderTitle);
		PlusButtonView plusButton = (PlusButtonView) productView.findViewById(R.id.cmdPlusQuantity);
		txtHeaderTitle.setVisibility(View.INVISIBLE);
		plusButton.setVisibility(View.INVISIBLE);
		ImageView imgProdPrev = (ImageView) productView.findViewById(R.id.imgProdPrev);
		ImageView imgWarning = (ImageView) productView.findViewById(R.id.imgWarning);
		TextView txtTitle = (TextView) productView.findViewById(R.id.txtTitle);
		TextView txtSubtitle = (TextView) productView.findViewById(R.id.txtSubtitle);
		imgProdPrev.setVisibility(View.VISIBLE);
		imgWarning.setVisibility(View.VISIBLE);
		txtTitle.setVisibility(View.VISIBLE);
		txtSubtitle.setVisibility(View.VISIBLE);
		
		PrintableImage printImage = this.cartObjects_.get(position);
		if (printImage.getPrintType().getDpi() < printImage.getPrintType().getWarnDPI()) {
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
		
		txtTitle.setText(printImage.getPrintType().getTitle());
		txtTitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		txtSubtitle.setText(String.valueOf(printImage.getPrintType().getQuantity()));
		txtSubtitle.setTextColor(this.interfacePrefHelper_.getTextColor());
		
		this.imageManager_.setImageAsync(imgProdPrev, printImage.getImage(), printImage.getPrintType(), printImage.getPrintType().getThumbSize(), null);
		
		return productView;
	}
	
	public interface PrintSelectedListener {
		public void printWasClicked(int index);
	}
}
