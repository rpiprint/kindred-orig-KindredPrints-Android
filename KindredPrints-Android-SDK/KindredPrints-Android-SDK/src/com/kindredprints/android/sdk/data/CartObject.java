package com.kindredprints.android.sdk.data;

import java.util.ArrayList;

import com.kindredprints.android.sdk.helpers.ImageEditor;

public class CartObject {

	private PartnerImage image;
	private ArrayList<PrintProduct> printProducts;
	private boolean printProductsInit;
	
	public CartObject() {
		
	}
	
	public CartObject copy() {
		CartObject obj = new CartObject();
		obj.setImage(this.getImage().copy());
		ArrayList<PrintProduct> listCopy = new ArrayList<PrintProduct>();
		for (PrintProduct pOrig : this.getPrintProducts()) {
			listCopy.add(pOrig.copy());
		}
		obj.setPrintProducts(listCopy);
		obj.setPrintProductsInit(this.isPrintProductsInit());
		return obj;
	}

	public void updateOrderSizes(ArrayList<PrintProduct> allSizes) {
		if (this.printProducts == null) {
			this.printProducts = new ArrayList<PrintProduct>();
		}
		
		ArrayList<PrintProduct> reducedList = new ArrayList<PrintProduct>();
		ArrayList<PrintProduct> finalList = new ArrayList<PrintProduct>();
		if (this.getImage().isTwosided()) {
			reducedList.addAll(ImageEditor.getAllowablePrintableSizesForImageSize(new Size(this.image.getWidth(), this.image.getHeight()), allSizes, ImageEditor.FILTER_DOUBLE));
		} else {
			reducedList.addAll(ImageEditor.getAllowablePrintableSizesForImageSize(new Size(this.image.getWidth(), this.image.getHeight()), allSizes, ImageEditor.NO_FILTER));
		}
		
		for (PrintProduct product : reducedList) {
			boolean exists = false;
			
			for (PrintProduct existProduct : this.printProducts) {
				if (existProduct.getId().equalsIgnoreCase(product.getId())) {
					finalList.add(existProduct);
					exists = true;
					break;
				}
			}
			
			if (!exists) finalList.add(product.copy());
		}
		
		this.setPrintProducts(finalList);
	}
	
	public PartnerImage getImage() {
		return image;
	}

	public void setImage(PartnerImage image) {
		this.image = image;
	}

	public ArrayList<PrintProduct> getPrintProducts() {
		if (printProducts == null) {
			printProducts = new ArrayList<PrintProduct>();
		}
		return printProducts;
	}

	public void setPrintProducts(ArrayList<PrintProduct> printProducts) {
		this.printProducts = printProducts;
	}

	public boolean isPrintProductsInit() {
		return printProductsInit;
	}

	public void setPrintProductsInit(boolean printProductsInit) {
		this.printProductsInit = printProductsInit;
	}
}
