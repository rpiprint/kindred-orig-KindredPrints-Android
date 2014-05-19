package com.kindredprints.android.sdk.fragments;

import com.kindredprints.android.sdk.KURLPhoto;
import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.data.Size;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class IntroImageFragment extends KindredFragment {

	private ImageManager imManager_;
	private ImageView imgBackground_;
	private String imageUrl_;
	private boolean drawn_;
	
	public IntroImageFragment() { }

	public void init(Context context, KindredFragmentHelper fragmentHelper) {
		this.imManager_ = ImageManager.getInstance(context);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = (ViewGroup) inflater.inflate(R.layout.fragment_intro_page, container, false);

		this.imgBackground_ = (ImageView) view.findViewById(R.id.imgBackground);
		imgBackground_.setImageBitmap(null);
		
		this.drawn_ = false;
		view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				loadImage();
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
					view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
		
		return view;
	}
	
	public void setBackgroundImage(String url) {
		this.imageUrl_ = url;
		if (this.drawn_) {
			loadImage();
		}
	}
	
	private void loadImage() {
		this.drawn_ = true;
		if (this.imageUrl_ != null) {
			String[] sections = this.imageUrl_.split("/");
			String pid = sections[sections.length-1];
			this.imManager_.setImageAsync(this.imgBackground_, new KURLPhoto(this.imageUrl_), pid, new Size(this.imgBackground_.getWidth(), this.imgBackground_.getHeight()));
		}
	}
}
