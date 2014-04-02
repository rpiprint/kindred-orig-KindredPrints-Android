package com.kindred.kindredprints_android_sdk.customviews;

import com.kindred.kindredprints_android_sdk.R;
import com.kindred.kindredprints_android_sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
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
		this.cmdMinus_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setQuantity(Math.max(0, getQuantity()-1));
			}
		});
		this.cmdPlus_ = (PlusButtonView) findViewById(R.id.cmdPlusQuantity);
		this.cmdPlus_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
