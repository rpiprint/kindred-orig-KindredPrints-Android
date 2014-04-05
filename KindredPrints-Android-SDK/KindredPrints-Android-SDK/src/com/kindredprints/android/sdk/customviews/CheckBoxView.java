package com.kindredprints.android.sdk.customviews;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

public class CheckBoxView extends Button {
	private InterfacePrefHelper interfacePrefHelper_;
	private Paint fakeTransPaint_;
	private Paint strokePaint_;
	private Paint fillPaint_;
	
	private boolean selectedState_;
	
	private static final float START_X = 0.4f;
	private static final float START_Y = 0.7f;
	private static final float PERCENTAGE_OF_SIDE = 0.55f;
	private static final float PERCENTAGE_OF_LONG_SIDE = 0.35f;

	public CheckBoxView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		this.setBackgroundColor(Color.TRANSPARENT);
		this.selectedState_ = false;
		initPaints(context);
	}

	private void initPaints(Context context) {
		this.fakeTransPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.fakeTransPaint_.setColor(this.interfacePrefHelper_.getBackgroundColor());
		this.fakeTransPaint_.setStyle(Paint.Style.STROKE);
		this.fakeTransPaint_.setStrokeWidth(context.getResources().getDimension(R.dimen.cart_page_plusminus_stroke));
		
		this.strokePaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.strokePaint_.setColor(this.interfacePrefHelper_.getTextColor());
		this.strokePaint_.setStyle(Paint.Style.STROKE);
		this.strokePaint_.setStrokeWidth(context.getResources().getDimension(R.dimen.cart_page_plusminus_stroke));
		
		this.fillPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.fillPaint_.setColor(this.interfacePrefHelper_.getTextColor());
		this.fillPaint_.setStyle(Paint.Style.FILL);
	}

	public void setChecked(boolean checked) {
		this.selectedState_ = checked;
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float py = this.getHeight()/2.0f;
        float px = this.getWidth()/2.0f;
        
        
        canvas.drawCircle(px, py, px-2, this.strokePaint_);
        
		if (this.selectedState_) {
			canvas.drawCircle(px, py, px-2, fillPaint_);
			
			float startX = this.getWidth()*START_X;
			float startY = this.getHeight()*START_Y;
			float longSide = this.getWidth()*PERCENTAGE_OF_SIDE;
			float shortSide = longSide*PERCENTAGE_OF_LONG_SIDE;
			float cos45 = (float)(Math.sqrt(2.0)/2.0);
			
			canvas.drawLine(startX, startY, startX-shortSide*cos45, startY-shortSide*cos45, this.fakeTransPaint_);
	        canvas.drawLine(startX, startY, startX+longSide*cos45, startY-longSide*cos45, this.fakeTransPaint_);
		}
	}
}
