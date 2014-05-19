package com.kindredprints.android.sdk.customviews;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

public class PlusButtonView extends Button {
	private InterfacePrefHelper interfacePrefHelper_;
	private Paint strokePaint_;
	private Paint fillPaint_;
	
	private static final float INTERNAL_PADDING = 0.25f;
	
	public PlusButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		this.setBackgroundColor(Color.TRANSPARENT);
		initPaints(context);
	}

	private void initPaints(Context context) {
		this.strokePaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.strokePaint_.setColor(this.interfacePrefHelper_.getTextColor());
		this.strokePaint_.setStyle(Paint.Style.STROKE);
		this.strokePaint_.setStrokeWidth(context.getResources().getDimension(R.dimen.cart_page_plusminus_stroke));
		
		this.fillPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.fillPaint_.setColor(this.interfacePrefHelper_.getTextColor());
		this.fillPaint_.setStyle(Paint.Style.FILL);
	}

	public void updatePaints(int color) {
		this.strokePaint_.setColor(color);
		this.fillPaint_.setColor(color);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float py = this.getHeight()/2.0f;
        float px = this.getWidth()/2.0f;
        
        canvas.drawLine(this.getWidth()*INTERNAL_PADDING, py, this.getWidth()*(1-INTERNAL_PADDING), py, this.strokePaint_);
        canvas.drawLine(px, this.getHeight()*INTERNAL_PADDING, px, this.getHeight()*(1-INTERNAL_PADDING), this.strokePaint_);
        
        canvas.drawCircle(px, py, px-2, this.strokePaint_);
        
		if (isPressed()) {
			canvas.drawCircle(px, py, px-2, fillPaint_);
		}
	}
}
