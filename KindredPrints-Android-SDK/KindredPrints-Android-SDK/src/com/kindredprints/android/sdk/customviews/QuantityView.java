package com.kindredprints.android.sdk.customviews;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class QuantityView extends RelativeLayout {
	private QuantityChangedListener callback_;
	
	private PlusButtonView cmdPlus_;
	private MinusButtonView cmdMinus_;
	private TextView txtQuantity_;
	
	private int quantity;
	
	public QuantityView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_quantity_selection, this, true);
		
		InterfacePrefHelper interfacePrefHelper = new InterfacePrefHelper(context);
		
		this.setBackgroundColor(Color.TRANSPARENT);
		this.cmdMinus_ = (MinusButtonView) findViewById(R.id.cmdMinusQuantity);
		this.cmdMinus_.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				cmdMinus_.invalidate();
				return false;
			}
			
		});
		this.cmdMinus_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				cmdMinus_.invalidate();
				setQuantity(Math.max(0, getQuantity()-1));
			}
		});
		this.cmdPlus_ = (PlusButtonView) findViewById(R.id.cmdPlusQuantity);
		this.cmdPlus_.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				cmdPlus_.invalidate();
				return false;
			}
		});
		this.cmdPlus_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cmdPlus_.invalidate();
				setQuantity(getQuantity()+1);
			}
		});
		this.txtQuantity_ = (TextView) findViewById(R.id.txtQuantity);
		this.txtQuantity_.setTextColor(interfacePrefHelper.getTextColor());
		this.txtQuantity_.setGravity(Gravity.CENTER);
		setQuantity(0);
	}
	
	public void setQuantityChangedListener(QuantityChangedListener callback) {
		this.callback_ = callback;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
		if (quantity == 0) {
			this.cmdMinus_.setShowEnabled(false);
		} else {
			this.cmdMinus_.setShowEnabled(true);
		}
		this.txtQuantity_.setText(String.valueOf(quantity));
		
		if (this.callback_ != null) this.callback_.userChangedQuantity(quantity);
	}
	
	
	public interface QuantityChangedListener {
		public void userChangedQuantity(int quantity);
	}

}
