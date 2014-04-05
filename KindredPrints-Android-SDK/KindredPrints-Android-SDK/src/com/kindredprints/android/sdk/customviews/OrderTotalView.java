package com.kindredprints.android.sdk.customviews;

import java.text.DecimalFormat;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OrderTotalView extends RelativeLayout {
	private static final int UP_TRANSITION_TIME = 200;
	private static final int DOWN_TRANSITION_TIME = 200;
	
	private InterfacePrefHelper interfacePrefHelper_;
	private int viewHeight;
	private boolean hidden;
	
	private View viewTotalBackground_;
	private TextView txtTitle_;
	private TextView txtOrderTotal_;
	
	private int orderTotal;
	
	public OrderTotalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_order_total, this, true);
		
		this.orderTotal = 0;
	    
	    this.viewTotalBackground_ = (View) findViewById(R.id.viewBackground);
	    this.txtTitle_ = (TextView) findViewById(R.id.txtTotalTitle);
	    this.txtOrderTotal_ = (TextView) findViewById(R.id.txtTotal);
	    this.setBackgroundColor(context.getResources().getColor(R.color.color_order_total));
	    this.txtTitle_.setTextColor(this.interfacePrefHelper_.getTextColor());
	    this.txtOrderTotal_.setTextColor(this.interfacePrefHelper_.getTextColor());
	    this.viewTotalBackground_.setBackgroundColor(context.getResources().getColor(R.color.color_order_total_label));
	}
	
	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		this.viewHeight = yNew;
	    this.hidden = false;
	    hide();
	}

	public int getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(int orderTotal) {
		this.orderTotal = orderTotal;
		DecimalFormat moneyFormat = null;
		if (orderTotal % 100 == 0) {
			moneyFormat = new DecimalFormat("$0");
		} else {
			moneyFormat = new DecimalFormat("$0.00");
		}
		float orderTotalF = (float)orderTotal/100.0f;
		this.txtOrderTotal_.setText(moneyFormat.format(orderTotalF));
		
		if (this.orderTotal > 0) {
			show();
		} else {
			hide();
		}
	}
	
	private void hide() {
		if (!this.hidden) {
			TranslateAnimation ani = new TranslateAnimation(0, 0, 0, this.viewHeight);
		    ani.setDuration(DOWN_TRANSITION_TIME);
		    ani.setFillAfter(true);
		    this.startAnimation(ani);
		    this.hidden = true;
		}
		
	}
	private void show() {
		if (this.hidden) {
			TranslateAnimation ani = new TranslateAnimation(0, 0, this.viewHeight, 0);
		    ani.setDuration(UP_TRANSITION_TIME);
		    ani.setFillAfter(true);
		    this.startAnimation(ani);
		    this.hidden = false;
		}	}

}
