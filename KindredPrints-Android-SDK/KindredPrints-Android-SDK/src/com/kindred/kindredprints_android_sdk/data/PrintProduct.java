package com.kindred.kindredprints_android_sdk.data;

public class PrintProduct {
	private String id;
	private String title;
	private String type;
	private String description;
	private int price;
	private float borderPerc;
	private Size trimmed;
	private int quantity;
	private Size thumbSize;
	private Size previewSize;
	private float minDPI;
	private float warnDPI;

	private float dpi;
	
	public PrintProduct() { 
		this.quantity = 1;
	}

	public PrintProduct copy() {
		PrintProduct pCopy = new PrintProduct();
		pCopy.setId(this.getId());
		pCopy.setType(this.getType());
		pCopy.setTitle(this.getTitle());
		pCopy.setDescription(this.getDescription());
		pCopy.setPrice(this.getPrice());
		pCopy.setBorderPerc(this.getBorderPerc());
		pCopy.setTrimmed(this.getTrimmed().copy());
		pCopy.setQuantity(this.getQuantity());
		pCopy.setThumbSize(this.getThumbSize().copy());
		pCopy.setPreviewSize(this.getPreviewSize().copy());
		pCopy.setWarnDPI(this.getWarnDPI());
		pCopy.setMinDPI(this.getMinDPI());
		pCopy.setDpi(this.getDpi());
		return pCopy;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public float getBorderPerc() {
		return borderPerc;
	}

	public void setBorderPerc(float borderPerc) {
		this.borderPerc = borderPerc;
	}

	public float getDpi() {
		return dpi;
	}

	public void setDpi(float dpi) {
		this.dpi = dpi;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public float getMinDPI() {
		return minDPI;
	}

	public void setMinDPI(float minDPI) {
		this.minDPI = minDPI;
	}

	public float getWarnDPI() {
		return warnDPI;
	}

	public void setWarnDPI(float warnDPI) {
		this.warnDPI = warnDPI;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public Size getTrimmed() {
		return trimmed;
	}

	public void setTrimmed(Size trimmed) {
		this.trimmed = trimmed;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Size getThumbSize() {
		return thumbSize;
	}

	public void setThumbSize(Size thumbSize) {
		this.thumbSize = thumbSize;
	}

	public Size getPreviewSize() {
		return previewSize;
	}

	public void setPreviewSize(Size previewSize) {
		this.previewSize = previewSize;
	}
}
