package com.kindredprints.android.sdk.adapters;

import java.util.ArrayList;

import com.kindredprints.android.sdk.KPhoto;
import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.Size;
import com.kindredprints.android.sdk.fragments.KindredFragmentHelper;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class PhotoSelectAdapter extends BaseAdapter {
	private static final float PERCENT_CHECK = 0.25f;
	private Activity context_;
	private KindredFragmentHelper fragmentHelper_;
	
	private CartManager cartManager_;
	private ImageManager imageManager_;
	
	private ArrayList<KPhoto> selectedPhotos_;
	private ArrayList<KPhoto> allPhotos_;
	
	private int lastIndexLoaded_;

	private int gridItemWidth_;
	private int checkItemWidth_;
	
	public PhotoSelectAdapter(Activity context, KindredFragmentHelper fragmentHelper, ArrayList<KPhoto> pendingPhotos, int gridItemWidth) {
		this.fragmentHelper_ = fragmentHelper;
		this.context_ = context;
		this.allPhotos_ = pendingPhotos;
		this.selectedPhotos_ = new ArrayList<KPhoto>();
		this.imageManager_ = ImageManager.getInstance(context);
		this.cartManager_ = CartManager.getInstance(context);
		
		this.gridItemWidth_ = gridItemWidth;
		this.checkItemWidth_ = (int)(this.gridItemWidth_*PERCENT_CHECK);
		this.lastIndexLoaded_ = -1;
	}
	
	public ArrayList<KPhoto> getSelectedPhotos() {
		return this.selectedPhotos_;
	}
	
	@Override
	public int getCount() {
		return this.allPhotos_.size();
	}

	@Override
	public Object getItem(int position) {
		return this.allPhotos_.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private void setChecked(ImageView imgChecked, boolean checked) {
		if (checked) {
			 imgChecked.setImageDrawable(this.context_.getResources().getDrawable(R.drawable.select_checked));
		} else {
			 imgChecked.setImageDrawable(null);
		}
	}

	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View squareView;		
		if (convertView == null) {
			LayoutInflater inflater = this.context_.getLayoutInflater();
			squareView = inflater.inflate(R.layout.pic_view, null, true);		
		} else {
			squareView = convertView;
		}		
		if (this.lastIndexLoaded_ == position)
			return squareView;
		else
			this.lastIndexLoaded_ = position;
			
		final ImageView imgChecked = (ImageView) squareView.findViewById(R.id.imgChecked);
		if (this.cartManager_.hasPartnerIdInCart(allPhotos_.get(position).getId()) >= 0) {
			setChecked(imgChecked, true);
		} else {
			setChecked(imgChecked, false);
		}
		
		ImageView imgThumb = (ImageView) squareView.findViewById(R.id.imgThumb);
		imgThumb.getLayoutParams().width = this.gridItemWidth_;
		imgThumb.getLayoutParams().height = this.gridItemWidth_;
		imgChecked.getLayoutParams().width = this.checkItemWidth_;
		imgChecked.getLayoutParams().height = this.checkItemWidth_;
		imgThumb.setImageDrawable(null);
		imgThumb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Bundle bun = new Bundle();
				bun.putInt("index", position);
				fragmentHelper_.moveToFragmentWithBundle(KindredFragmentHelper.FRAG_PREVIEW, bun);
			}
		}); 
		this.imageManager_.setImageAsync(imgThumb, this.allPhotos_.get(position), String.valueOf(position), new Size(this.gridItemWidth_, this.gridItemWidth_), null);
		return squareView;
	}

}
