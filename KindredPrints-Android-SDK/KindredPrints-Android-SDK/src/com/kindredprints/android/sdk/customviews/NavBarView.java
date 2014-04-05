package com.kindredprints.android.sdk.customviews;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NavBarView extends RelativeLayout {
	private static final int UP_TRANSITION_TIME = 200;
	private static final int DOWN_TRANSITION_TIME = 200;
	
	private int viewHeight;
	private boolean hidden;
	
	private SideArrow cmdBack_;
	private Button cmdNext_;
	private TextView txtTitle_;
	
	private InterfacePrefHelper interfacePrefHelper_;
	
	private NavBarClickCallback callback_;
	
	public NavBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_nav_bar, this, true);
		
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		
		this.txtTitle_ = (TextView) findViewById(R.id.txtTitle);
		this.cmdBack_ = (SideArrow) findViewById(R.id.cmdBack);
		this.cmdBack_.setDirection(SideArrow.LEFT_ARROW);
		
		this.cmdNext_ = (Button) findViewById(R.id.cmdNext);
		this.cmdNext_.setBackgroundColor(Color.TRANSPARENT);
		this.cmdNext_.setTextColor(this.interfacePrefHelper_.getHighlightColor());
		this.setBackgroundColor(this.interfacePrefHelper_.getNavColor());
		this.txtTitle_.setTextColor(this.interfacePrefHelper_.getTextColor());
		this.txtTitle_.setGravity(Gravity.CENTER_VERTICAL);
		
		this.cmdBack_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (callback_ != null) callback_.onBackClick();
			}
		});
		this.cmdNext_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (callback_ != null) callback_.onNextClick();
			}
		});
	}
	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		this.viewHeight = yNew;
		this.hidden = false;
	}
	
	public void setNextButtonEnabled(boolean enabled) {
		this.cmdNext_.setEnabled(enabled);
		if (enabled) {
			this.cmdNext_.setTextColor(this.interfacePrefHelper_.getHighlightColor());
		} else {
			this.cmdNext_.setTextColor(Color.GRAY);
		}
	}
	
	public void triggerNextButton() {
		this.cmdNext_.performClick();
	}
	
	public void triggerBackButton() {
		this.cmdBack_.performClick();
	}
	
	public void setButtonClickListener(NavBarClickCallback callback) {
		this.callback_ = callback;
	}
	
	public void hide() {
		if (!this.hidden) {
			TranslateAnimation ani = new TranslateAnimation(0, 0, 0, -viewHeight);
		    ani.setDuration(DOWN_TRANSITION_TIME);
		    ani.setFillAfter(true);
		    this.startAnimation(ani);
		    this.hidden = true;
		}
	}
	public void show() {
		if (this.hidden) {
			TranslateAnimation ani = new TranslateAnimation(0, 0, 0, viewHeight);
		    ani.setDuration(UP_TRANSITION_TIME);
		    ani.setFillAfter(true);
		    this.startAnimation(ani);
		    this.hidden = false;
		}
	}
	
	public void setNextTitle(String title) {
		this.cmdNext_.setText(title);
	}
	public void setNavTitle(String title) {
		this.txtTitle_.setText(title);
	}
	
	
	
}
