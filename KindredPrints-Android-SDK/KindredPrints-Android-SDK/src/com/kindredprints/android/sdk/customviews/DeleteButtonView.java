package com.kindredprints.android.sdk.customviews;

import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

public class DeleteButtonView extends Button {

	private InterfacePrefHelper interfacePrefHelper_;
	private Paint strokePaint_;
	private Paint fillPaint_;
	private Paint fillPressedPaint_;
	private Paint textPaint_;
	
	private static final float INTERNAL_PADDING = 0.3f;

	public DeleteButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		this.setBackgroundColor(Color.TRANSPARENT);
		initPaints(context);
	}
	
	private void initPaints(Context context) {
		this.fillPressedPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.fillPressedPaint_.setColor(this.interfacePrefHelper_.getNavColor());
		this.fillPressedPaint_.setStyle(Paint.Style.FILL);
		
		this.fillPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.fillPaint_.setColor(this.interfacePrefHelper_.getTextColor());
		this.fillPaint_.setStyle(Paint.Style.FILL);
		
		this.strokePaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.strokePaint_.setColor(this.interfacePrefHelper_.getNavColor());
		this.strokePaint_.setStyle(Paint.Style.STROKE);
		this.strokePaint_.setStrokeWidth(1.0f);
		
		this.textPaint_= new Paint(Paint.ANTI_ALIAS_FLAG);
		this.textPaint_.setColor(this.interfacePrefHelper_.getBackgroundColor());
		this.textPaint_.setTextSize(context.getResources().getDimension(R.dimen.cart_page_text_size));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float py = this.getHeight()/2.0f;
        float px = this.getWidth()/2.0f;
        
        canvas.drawCircle(px, py, px-2, this.fillPaint_);
        canvas.drawCircle(px, py, px-2, this.strokePaint_);
        canvas.drawLine(this.getWidth()*INTERNAL_PADDING, this.getHeight()*INTERNAL_PADDING, this.getWidth()*(1-INTERNAL_PADDING), this.getHeight()*(1-INTERNAL_PADDING), this.textPaint_);
        canvas.drawLine(this.getWidth()*INTERNAL_PADDING, this.getHeight()*(1-INTERNAL_PADDING), this.getWidth()*(1-INTERNAL_PADDING), this.getHeight()*INTERNAL_PADDING, this.textPaint_);
        
		if (isPressed()) {
			canvas.drawCircle(px, py, px-2, this.fillPressedPaint_);
		}
	}

}
