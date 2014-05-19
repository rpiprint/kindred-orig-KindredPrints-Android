package com.kindredprints.android.sdk.data;

import java.util.ArrayList;

public interface CartUpdatedCallback {
	public void ordersHaveAllBeenUpdated();
	public void orderCountHasBeenUpdated();
	public void orderHasBeenUpdatedWithSize(PartnerImage obj, ArrayList<PrintProduct> fittedList);
	public void orderHasBeenServerInit(PartnerImage obj);
	public void orderHasBeenUploaded(PartnerImage obj);
	public void introPagesHaveBeenUpdated(ArrayList<String> pageUrls);
}
