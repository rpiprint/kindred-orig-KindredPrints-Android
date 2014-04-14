package com.kindredprints.android.sdk.data;

public interface CartUpdatedCallback {
	public void ordersHaveAllBeenUpdated();
	public void orderCountHasBeenUpdated();
	public void orderHasBeenUpdatedWithSize(PartnerImage obj);
	public void orderHasBeenServerInit(PartnerImage obj);
	public void orderHasBeenUploaded(PartnerImage obj);
}
