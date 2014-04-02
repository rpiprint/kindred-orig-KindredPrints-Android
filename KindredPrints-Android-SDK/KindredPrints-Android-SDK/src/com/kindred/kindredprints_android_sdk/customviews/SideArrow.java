package com.kindred.kindredprints_android_sdk.customviews;

import com.kindred.kindredprints_android_sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

public class SideArrow extends Button {
	public static final int LEFT_ARROW = 1;
	public static final int RIGHT_ARROW = -1;
	
	private static final float LINE_THICKNESS = 0.08f;
	private static final float DIAMETER_PERCENT = 0.4f;
	
	private InterfacePrefHelper interfacePrefHelper_;

	private Paint strokePaint_;

	private int direction_;

	public SideArrow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setBackgroundColor(Color.TRANSPARENT);
		this.direction_ = LEFT_ARROW;
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);

		this.strokePaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.strokePaint_.setColor(this.interfacePrefHelper_.getTextColor());
		this.strokePaint_.setStyle(Paint.Style.STROKE);
	}
	
	public void setDirection(int direction) {
		this.direction_ = direction;		
		this.invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float py = this.getHeight()/2.0f;
        float px = this.getWidth()/2.0f;
        
        float stroke = LINE_THICKNESS*this.getWidth();
		this.strokePaint_.setStrokeWidth(stroke);
        
        float side = this.getWidth()*DIAMETER_PERCENT;
        float halfSide = ((float)Math.sqrt(2))*side/2.0f;
        float strokePadding = ((float)Math.sqrt(2))*stroke/4.0f;
        
        canvas.drawLine(px+this.direction_*halfSide/2, py-halfSide, px-this.direction_*halfSide/2+strokePadding, py+strokePadding, this.strokePaint_);
        canvas.drawLine(px-this.direction_*halfSide/2, py, px+this.direction_*halfSide/2, py+halfSide, this.strokePaint_);
	}
}
