package com.kindred.kindredprints_android_sdk.data;

public class LineItem {
	public final static String LINE_ITEM_NO_VALUE = "no_value";
	
	public final static String ORDER_PRODUCT_LINE_TYPE = "lineitem";
	public final static String ORDER_SUBTOTAL_LINE_TYPE = "subtotal";
	public final static String ORDER_SHIPPING_LINE_TYPE = "shipping";
	public final static String ORDER_CREDITS_LINE_TYPE = "credits";
	public final static String ORDER_COUPON_LINE_TYPE = "couponedit";
	public final static String ORDER_COUPON_APPLIED_LINE_TYPE = "coupon";
	public final static String ORDER_TOTAL_LINE_TYPE = "total";

	private String liType;
	private String liName;
	private String liAmount;
	private int liQuantity;
	private String liAddressId;
	private String liShipMethod;
	private String liCouponId;
	
	public LineItem() {
		this.liType = LINE_ITEM_NO_VALUE;
		this.liName = LINE_ITEM_NO_VALUE;
		this.liAmount = LINE_ITEM_NO_VALUE;
		this.liQuantity = 0;
		this.liAddressId = LINE_ITEM_NO_VALUE;
		this.liShipMethod = LINE_ITEM_NO_VALUE;
		this.liCouponId = LINE_ITEM_NO_VALUE;
	}

	public String getLiType() {
		return liType;
	}
	public void setLiType(String liType) {
		this.liType = liType;
	}
	public String getLiName() {
		return liName;
	}
	public void setLiName(String liName) {
		this.liName = liName;
	}
	public String getLiShipMethod() {
		return liShipMethod;
	}
	public void setLiShipMethod(String liShipMethod) {
		this.liShipMethod = liShipMethod;
	}
	public String getLiAmount() {
		return liAmount;
	}
	public void setLiAmount(String liAmount) {
		this.liAmount = liAmount;
	}
	public int getLiQuantity() {
		return liQuantity;
	}
	public void setLiQuantity(int liQuantity) {
		this.liQuantity = liQuantity;
	}
	public String getLiAddressId() {
		return liAddressId;
	}
	public void setLiAddressId(String liAddressId) {
		this.liAddressId = liAddressId;
	}
	public String getLiCouponId() {
		return liCouponId;
	}
	public void setLiCouponId(String liCouponId) {
		this.liCouponId = liCouponId;
	}
}
