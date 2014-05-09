package com.kindredprints.android.sdk.adapters;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.data.PrintableImage;
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

public class CartItemListAdapter extends BaseAdapter {

	private Activity context_;
	private ImageManager imageManager_;
	private CartManager cartManager_;
	private InterfacePrefHelper interfacePrefHelper_;
	
	private MixpanelAPI mixpanel_;
	
	private PrintSelectedListener printClickCallback_;
	
	private ArrayList<PrintableImage> cartObjects_;
	
	public CartItemListAdapter(Activity context) {
		this.context_ = context;
		this.mixpanel_ = MixpanelAPI.getInstance(context, context.getResources().getString(R.string.mixpanel_token));

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
		return this.cartObjects_.size();
	}

	@Override
	public Object getItem(int position) {
		return this.cartObjects_.get(position);
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
		
		return productView;
	}

	public interface PrintSelectedListener {
		public void printWasClicked(int index);
	}
}
