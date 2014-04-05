package com.kindredprints.android.sdk.data;

public class Size {
	private float width;
	private float height;
	public Size(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	public Size copy() {
		Size cSize = new Size(this.width, this.height);
		return cSize;
	}
	
	public float getWidth() {
		return width;
	}
	public void setWidth(float width) {
		this.width = width;
	}
	public float getHeight() {
		return height;
	}
	public void setHeight(float height) {
		this.height = height;
	}
}
