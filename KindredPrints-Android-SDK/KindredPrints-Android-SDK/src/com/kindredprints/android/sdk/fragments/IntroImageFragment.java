package com.kindredprints.android.sdk.fragments;

import com.kindredprints.android.sdk.KURLPhoto;
import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.data.Size;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.cache.ImageManager.ImageManagerCallback;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class IntroImageFragment extends KindredFragment {	
	private Context context_;
	private ImageManager imManager_;
	private ImageView imgBackground_;
	private TextView txtTitle_;
	private TextView txtSubtitle_;
	private ProgressBar progressBar_;
	private String imageUrl_;
	private String title_;
	private int pageIndex_;
	private boolean drawn_;
	
	private ImageManagerCallback imageSetCallback_;
	
	public IntroImageFragment() { }

	public void init(Context context, KindredFragmentHelper fragmentHelper) {
		this.context_ = context;
		this.imManager_ = ImageManager.getInstance(context);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = (ViewGroup) inflater.inflate(R.layout.fragment_intro_page, container, false);

		this.imgBackground_ = (ImageView) view.findViewById(R.id.imgBackground);
		imgBackground_.setImageBitmap(null);
		
		this.txtTitle_ = (TextView) view.findViewById(R.id.txtTitle);		
		this.txtSubtitle_ = (TextView) view.findViewById(R.id.txtSubtitle);
		this.progressBar_ = (ProgressBar) view.findViewById(R.id.progressBar);

		setImageVisible(false);
		
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
	
	public void setBackgroundImage(String url, String text, int index) {
		this.imageUrl_ = url;
		this.title_ = text;
		this.pageIndex_ = index;
		if (this.drawn_) {
			loadImage();
		}
	}
	
	private void setImageVisible(boolean visible) {
		if (visible) {
			this.txtTitle_.setVisibility(View.VISIBLE);
			this.txtSubtitle_.setVisibility(View.VISIBLE);
			this.imgBackground_.setVisibility(View.VISIBLE);
			this.progressBar_.setVisibility(View.INVISIBLE);
		} else {
			this.txtTitle_.setVisibility(View.INVISIBLE);
			this.txtSubtitle_.setVisibility(View.INVISIBLE);
			this.imgBackground_.setVisibility(View.INVISIBLE);
			this.progressBar_.setVisibility(View.VISIBLE);
		}
	}
	
	private void loadImage() {
		this.drawn_ = true;
		if (this.imageUrl_ != null) {
			this.txtTitle_.setText(this.title_);
			this.txtSubtitle_.setText("Page " + this.pageIndex_);
			
			String[] sections = this.imageUrl_.split("/");
			String pid = sections[sections.length-1];
			
			this.imageSetCallback_ = new ImageManagerCallback() {
				@Override
				public void imageAssigned(Size size) {
					MixpanelAPI.getInstance(context_, context_.getResources().getString(R.string.mixpanel_token)).track("intro_page_image_load", null);
					setImageVisible(true);
				}
			};
			
			this.imManager_.setImageAsync(this.imgBackground_, new KURLPhoto(this.imageUrl_), pid, new Size(this.imgBackground_.getWidth(), this.imgBackground_.getHeight()), this.imageSetCallback_);
		}
	}
}
