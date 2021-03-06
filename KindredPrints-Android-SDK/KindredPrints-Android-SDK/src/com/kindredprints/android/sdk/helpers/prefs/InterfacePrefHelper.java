package com.kindredprints.android.sdk.helpers.prefs;

import com.kindredprints.android.sdk.data.Size;

import android.content.Context;
import android.graphics.Color;

public class InterfacePrefHelper extends PrefHelper {
	private static final String KEY_TEXT_COLOR = "kp_text_color";
	private static final String KEY_HIGHLIGHT_TEXT_COLOR = "kp_highlight_text_color";
	private static final String KEY_HIGHLIGHT_COLOR = "kp_highlight_color";
	private static final String KEY_NAV_COLOR = "kp_nav_color";
	private static final String KEY_BACKGROUND_COLOR = "kp_background_color";
	
	private static final float PREVIEW_PERC_WIDTH = 0.80f;
	private static final float PREVIEW_PERC_HEIGHT = 0.4f;
	private static final float THUMB_PERC = 0.25f;
	private static final String KEY_BORDER_COLOR = "kp_border_color";
	private static final String KEY_BORDER_ENABLED = "kp_border_enabled";

	
	private PrefHelper prefHelper_;
	
	public InterfacePrefHelper(Context context) {
		this.prefHelper_ = getInstance(context); 
	}
	
	public float getThumbMaxSize() {
		return THUMB_PERC*this.prefHelper_.getScreenSize().getWidth();
	}
	public float getPreviewMaxSize() {
		return Math.min(PREVIEW_PERC_WIDTH*this.prefHelper_.getScreenSize().getWidth(), PREVIEW_PERC_HEIGHT*this.prefHelper_.getScreenSize().getHeight());
	}
	
	public float getBorderSize(float borderPerc) {
		if (this.prefHelper_.getBool(KEY_BORDER_ENABLED)) {
			return borderPerc;
		} else {
			return 0.0f;
		}
	}
	
	public float getBorderWidth(float borderPerc, Size picSize) {
		if (this.prefHelper_.getBool(KEY_BORDER_ENABLED)) {
			if (picSize.getHeight() > picSize.getWidth()) {
				return borderPerc * picSize.getHeight();
			} else {
				return borderPerc * picSize.getWidth();
			}
		} else {
			return 0.0f;
		}
	}
	
	public int getBorderColor() {
		String storedHexColor = this.prefHelper_.getString(KEY_BORDER_COLOR);
		if (storedHexColor.equals(PrefHelper.NO_STRING_VALUE)) {
			storedHexColor = "#FFFFFF";
		}
		return Color.parseColor(storedHexColor);
	}
	
	public void setBorderColor(int border) {
		String borderC = String.format("#%06X", (0xFFFFFF & border));
		this.prefHelper_.setString(KEY_BORDER_COLOR, borderC);
	}
	public int getHighlightColor() {
		String storedHexColor = this.prefHelper_.getString(KEY_HIGHLIGHT_COLOR);
		if (storedHexColor.equals(PrefHelper.NO_STRING_VALUE)) {
			storedHexColor = "#52CCEF";
		}
		return Color.parseColor(storedHexColor);
	}
	
	public void setHighlightColor(int border) {
		String borderC = String.format("#%06X", (0xFFFFFF & border));
		this.prefHelper_.setString(KEY_HIGHLIGHT_COLOR, borderC);
	}
	public void enableBorder() {
		this.prefHelper_.setBool(KEY_BORDER_ENABLED, true);
	}
	public void disableBorder() {
		this.prefHelper_.setBool(KEY_BORDER_ENABLED, false);
	}
	
	public int getHighlightTextColor() {
		String storedHexColor = this.prefHelper_.getString(KEY_HIGHLIGHT_TEXT_COLOR);
		if (storedHexColor.equals(PrefHelper.NO_STRING_VALUE)) {
			storedHexColor = "#FFFFFF";
		}
		return Color.parseColor(storedHexColor);
	}
	public void setHightlightTextColor(int border) {
		String borderC = String.format("#%06X", (0xFFFFFF & border));
		this.prefHelper_.setString(KEY_HIGHLIGHT_TEXT_COLOR, borderC);
	}
	public int getTextColor() {
		String storedHexColor = this.prefHelper_.getString(KEY_TEXT_COLOR);
		if (storedHexColor.equals(PrefHelper.NO_STRING_VALUE)) {
			storedHexColor = "#666666";
		}
		return Color.parseColor(storedHexColor);
	}
	
	public void setTextColor(int border) {
		String borderC = String.format("#%06X", (0xFFFFFF & border));
		this.prefHelper_.setString(KEY_TEXT_COLOR, borderC);
	}
	
	public int getNavColor() {
		String storedHexColor = this.prefHelper_.getString(KEY_NAV_COLOR);
		if (storedHexColor.equals(PrefHelper.NO_STRING_VALUE)) {
			storedHexColor = "#DDDDDD";
		}
		return Color.parseColor(storedHexColor);
	}
	
	public void setNavColor(int border) {
		String borderC = String.format("#%06X", (0xFFFFFF & border));
		this.prefHelper_.setString(KEY_NAV_COLOR, borderC);
	}
	
	public int getBackgroundColor() {
		String storedHexColor = this.prefHelper_.getString(KEY_BACKGROUND_COLOR);
		if (storedHexColor.equals(PrefHelper.NO_STRING_VALUE)) {
			storedHexColor = "#FFFFFF";
		}
		return Color.parseColor(storedHexColor);
	}
	
	public void setBackgroundColor(int border) {
		String borderC = String.format("#%06X", (0xFFFFFF & border));
		this.prefHelper_.setString(KEY_BACKGROUND_COLOR, borderC);
	}
	
}
