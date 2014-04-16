package com.kindredprints.android.sdk.data;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import com.kindredprints.android.sdk.helpers.ImageUploadHelper;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class CartManager {
	private ArrayList<CartObject> orders;
	private ArrayList<PrintableImage> selectedOrders;
	private Semaphore ordersSema_;
	private Semaphore selOrdersSema_;
	
	private Context context_;
	
	private CartUpdatedCallback callback_;
	private UserPrefHelper userPrefHelper_;
	private DevPrefHelper devPrefHelper_;
	private static CartManager manager_;
	
	public CartManager() { }
	public CartManager(Context context) {
		this.context_ = context;
		this.userPrefHelper_ = new UserPrefHelper(context);
		this.devPrefHelper_ = new DevPrefHelper(context);
		this.orders = this.userPrefHelper_.getCartOrders();
		this.selectedOrders = this.userPrefHelper_.getSelectedOrders();
		this.ordersSema_ = new Semaphore(1);
		this.selOrdersSema_ = new Semaphore(1);
	}
	
	public void setCartUpdatedCallback(CartUpdatedCallback callback) {
		this.callback_ = callback;
	}
	
	public static CartManager getInstance(Context context) {
		if (manager_ == null) {
			manager_ = new CartManager(context);
		} 
		return manager_;
	}
	
	
	public void updateAllOrdersWithNewSizes() {
		try {
			this.ordersSema_.acquire();
			ArrayList<PrintProduct> allSizes = this.devPrefHelper_.getCurrentSizes();
			for (CartObject cartObj : this.orders) {
				cartObj.updateOrderSizes(allSizes);
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Handler mainHandler = new Handler(context_.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (callback_ != null) {
					callback_.ordersHaveAllBeenUpdated();
				}
			}
		});
	}
	
	public void imageWasUpdatedWithQuantities(PartnerImage image, PrintProduct product) {
		try {
			this.ordersSema_.acquire();
			for (CartObject cartObj : this.orders) {
				if (cartObj.getImage().getId().equalsIgnoreCase(image.getId())) {
					for (PrintProduct pProduct : cartObj.getPrintProducts()) {
						if (pProduct.getId().equals(product.getId())) {
							pProduct.setQuantity(product.getQuantity());
						}
					}
				}
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void imageWasUpdatedWithSizes(PartnerImage image, ArrayList<PrintProduct> fittedProducts) {
		PartnerImage pImage = null;
		try {
			this.ordersSema_.acquire();
			for (CartObject cartObj : this.orders) {
				boolean update = false;
				pImage = cartObj.getImage();
				if (pImage.getId().equalsIgnoreCase(image.getId())) {
					update = true;
				} else if (pImage.isTwosided() && pImage.getBackSideImage().getId().equalsIgnoreCase(image.getId())) {
					update = true;
					pImage = pImage.getBackSideImage();
				}
				if (update) {
					pImage.setHeight(image.getHeight());
					pImage.setWidth(image.getWidth());
					pImage.setLocalCached(image.isLocalCached());
					pImage.setThumbLocalCached(image.isThumbLocalCached());
					cartObj.setPrintProducts(fittedProducts);
					cartObj.setPrintProductsInit(true);
				}
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();

			this.selOrdersSema_.acquire();
			for (int i = 0; i < this.selectedOrders.size(); i++) {
				PrintableImage sImage = this.selectedOrders.get(i);
				if (sImage.getImage().getId().equalsIgnoreCase(image.getId())) {
					this.selectedOrders.remove(i);
					i = i - 1;
				}
			}
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		final PartnerImage fpImage = pImage;
		Handler mainHandler = new Handler(context_.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (callback_ != null) {	
					callback_.orderHasBeenUpdatedWithSize(fpImage);
				}
			}
		});
	}
	public void imageWasServerInit(String localId, String pid) {
		PartnerImage pImage = null;
		try {
			this.ordersSema_.acquire();
			for (CartObject cartObj : this.orders) {
				boolean update = false;
				pImage = cartObj.getImage();
				if (pImage.getId().equalsIgnoreCase(localId)) {
					update = true;
				} else if (pImage.isTwosided() && pImage.getBackSideImage().getId().equalsIgnoreCase(localId)) {
					update = true;
					pImage = pImage.getBackSideImage();
				}
				if (update) {
					pImage.setServerId(pid);
					pImage.setServerInit(true);
				}
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();
			
			this.selOrdersSema_.acquire();
			for (PrintableImage sImage : this.selectedOrders) {
				boolean update = false;
				pImage = sImage.getImage();
				if (pImage.getId().equalsIgnoreCase(localId)) {
					update = true;
				} else if (pImage.isTwosided() && pImage.getBackSideImage().getId().equalsIgnoreCase(localId)) {
					update = true;
					pImage = pImage.getBackSideImage();
				}
				if (update) {
					sImage.getImage().setServerId(pid);
					sImage.getImage().setServerInit(true);
				}
			}
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		final PartnerImage fpImage = pImage;
		Handler mainHandler = new Handler(context_.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (callback_ != null) {
					callback_.orderHasBeenServerInit(fpImage);
				}
			}
		});
	}
	public void imageFinishedUploading(String localId) {
		PartnerImage pImage = null;
		try {
			this.ordersSema_.acquire();
			for (CartObject cartObj : this.orders) {
				boolean update = false;
				pImage = cartObj.getImage();
				if (cartObj.getImage().getId().equalsIgnoreCase(localId)) {
					update = true;
				} else if (pImage.isTwosided() && pImage.getBackSideImage().getId().equalsIgnoreCase(localId)) {
					update = true;
					pImage = pImage.getBackSideImage();
				}
				if (update) {
					pImage.setUploadComplete(true);
				}
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();
			
			this.selOrdersSema_.acquire();
			for (PrintableImage sImage : this.selectedOrders) {
				boolean update = false;
				pImage = sImage.getImage();
				if (pImage.getId().equalsIgnoreCase(localId)) {
					update = true;
				} else if (pImage.isTwosided() && pImage.getBackSideImage().getId().equalsIgnoreCase(localId)) {
					update = true;
					pImage = pImage.getBackSideImage();
				}
				if (update) {
					pImage.setUploadComplete(true);
				}
			}
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		final PartnerImage fpImage = pImage;
		Handler mainHandler = new Handler(context_.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (callback_ != null) {
					callback_.orderHasBeenUploaded(fpImage);
				}
			}
		});
	}
	
	public void selectedPrintableImageWasServerInit(String localId, String pid) {
		try {
			this.selOrdersSema_.acquire();
			for (int i = 0; i < this.selectedOrders.size(); i++) {
		        PrintableImage pImage = this.selectedOrders.get(i);
		        if ((pImage.getImage().getId() + "-" + pImage.getPrintType().getId()).equalsIgnoreCase(localId)) {
		        	pImage.setServerId(pid);
		        	pImage.setServerInit(true);
		        }
		    }
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void selectedPrintableImageWasLineItemInit(String localId, String pid) {
		try {
			this.selOrdersSema_.acquire();
			for (int i = 0; i < this.selectedOrders.size(); i++) {
		        PrintableImage pImage = this.selectedOrders.get(i);
		        if ((pImage.getImage().getId() + "-" + pImage.getPrintType().getId()).equalsIgnoreCase(localId)) {
		        	this.selectedOrders.get(i).setServerLineItemId(pid);
		        	this.selectedOrders.get(i).setServerLineItemInit(true);
		        }
		    }
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int countOfOrders() {
		return this.orders.size();
	}
	public int countOfSelectedOrders() {
		return this.selectedOrders.size();
	}

	public void deleteOrderImageForId(String localid) {
		try {
			this.ordersSema_.acquire();
			this.selOrdersSema_.acquire();
			for (int i = 0; i < this.orders.size(); i++) {
				CartObject cartObj = this.orders.get(i);
				
				if (cartObj.getImage().getId().equals(localid)) {
					for (int j=0; j<this.selectedOrders.size(); j++) {
						PrintableImage image = this.selectedOrders.get(j);
						if (image.getImage().getId().equalsIgnoreCase(cartObj.getImage().getId())) {
							this.selectedOrders.remove(j);
							j = j - 1;
						}
					}
					this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
					
					this.orders.remove(i);
					this.userPrefHelper_.setCartOrders(this.orders);
					
					ImageManager.getInstance(context_).deleteAllImagesFromCache(cartObj);
					break;
				}
			}
			this.ordersSema_.release();
			this.selOrdersSema_.release();
			
			Handler mainHandler = new Handler(context_.getMainLooper());
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					if (callback_ != null) {
						callback_.orderCountHasBeenUpdated();
						callback_.ordersHaveAllBeenUpdated();
					}
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteOrderImageAtIndex(int index) {
		try {
			this.ordersSema_.acquire();
			this.selOrdersSema_.acquire();
			if (index >= 0 && index < this.orders.size()) {
				CartObject cartObj = this.orders.get(index);
				for (int i=0; i<this.selectedOrders.size(); i++) {
					PrintableImage image = this.selectedOrders.get(i);
					if (image.getImage().getId().equalsIgnoreCase(cartObj.getImage().getId())) {
						this.selectedOrders.remove(i);
						i = i - 1;
					}
				}
				this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
				
				this.orders.remove(index);
				this.userPrefHelper_.setCartOrders(this.orders);
			}
			this.ordersSema_.release();
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public boolean addOrderImage(CartObject order) {
		try {
			this.ordersSema_.acquire();
			for (CartObject prevOrder : this.orders) {
				if (prevOrder.getImage().getId().equalsIgnoreCase(order.getImage().getId())) {
					Log.i("KindredSDK", "Warning: duplicate id detected - no image added");
					this.ordersSema_.release();
					return false;
				} else if (prevOrder.getImage().getPartnerId().equalsIgnoreCase(order.getImage().getPartnerId())) {
					Log.i("KindredSDK", "Warning: duplicate id detected - no image added. ID = " + order.getImage().getPartnerId());
					this.ordersSema_.release();
					return false;
				}
			}
			this.orders.add(order);
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	public CartObject getOrderForIndex(int index) {
		if (index < 0 || index >= this.orders.size())
			return null;
		CartObject order = null;
		try {
			this.ordersSema_.acquire();
			order = this.orders.get(index);
			this.ordersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return order; 
	}
	public PrintableImage getSelectedOrderForIndex(int index) {
		if (index < 0 || index >= this.selectedOrders.size())
			return null;
		PrintableImage pImage = null;
		try {
			this.selOrdersSema_.acquire();
			pImage = this.selectedOrders.get(index);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return pImage;
	}
	public PrintableImage getSelectedOrderForId(String pid) {
		try {
			this.selOrdersSema_.acquire();
			for (PrintableImage im : this.selectedOrders) {
				if (im.getServerId().equalsIgnoreCase(pid)) {
					this.selOrdersSema_.release();
					return im;
				}
			}
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public boolean generateSelectedOrdersFromBaseOrders() {
		int countOfNewOrders = 0;
		int countOfUnchanged = 0;
		boolean lowDPI = false;
		ArrayList<PrintableImage> prevSelection = new ArrayList<PrintableImage>();
		try {
			this.ordersSema_.acquire();
			this.selOrdersSema_.acquire();
			for (PrintableImage im : this.selectedOrders) {
				prevSelection.add(im.copy());
			}
			
			this.selectedOrders.clear();
			for (CartObject cartObj : this.orders) {
				for (PrintProduct product : cartObj.getPrintProducts()) {
					if (product.getQuantity() > 0) {
						PrintableImage newPImage = new PrintableImage();
						for (PrintableImage pImage : prevSelection) {
							if (pImage.getImage().getId().equalsIgnoreCase(cartObj.getImage().getId()) 
									&& pImage.getPrintType().getId().equals(product.getId())) {
								newPImage = pImage.copy();
								if (pImage.getPrintType().getQuantity() != product.getQuantity()) {
									newPImage.setServerLineItemInit(false);
								} else {
									countOfUnchanged = countOfUnchanged + 1;
								}
								break;
							}
						}
						
						if (product.getDpi() < product.getWarnDPI()) {
							lowDPI = true;
						}
						
						newPImage.setImage(cartObj.getImage().copy());
						newPImage.setPrintType(product.copy());
	
						countOfNewOrders = countOfNewOrders + 1;
						this.selectedOrders.add(newPImage);
					}
				}
			}
			
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			
			this.selOrdersSema_.release();
			this.ordersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		if (countOfNewOrders != countOfUnchanged) {
			this.devPrefHelper_.setNeedUpdateOrderId(true);
		}
		
		ImageUploadHelper.getInstance(this.context_).validateAllOrdersInit();
		
		return lowDPI;
	}

	public ArrayList<CartObject> getOrderImages() {
		return this.orders;
	}
	public ArrayList<PrintableImage> getSelectedOrderImages() {
		return this.selectedOrders;
	}
	
	public void setSelectedOrderImages(ArrayList<PrintableImage> selectedOrderImages) {
		try {
			this.selOrdersSema_.acquire();
			this.selectedOrders = selectedOrderImages;
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	public void cleanUpCart() {
		this.orders = new ArrayList<CartObject>();
		this.selectedOrders = new ArrayList<PrintableImage>();
		this.userPrefHelper_.setCartOrders(this.orders);
		this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
	}

	public int getOrderTotal() {
		int orderTotal = 0;
		boolean needSave = false;
		try {
			this.ordersSema_.acquire();
			for (int i = 0; i<this.orders.size(); i++) {
				CartObject image = this.orders.get(i);
				if (image == null) {
					this.orders.remove(i);
					i = i - 1;
					needSave = true;
					continue;
				}
				for (PrintProduct product : image.getPrintProducts()) {
					orderTotal = orderTotal + product.getPrice()*product.getQuantity();
				}
				if (needSave) {
					this.userPrefHelper_.setCartOrders(this.orders);
				}
			}
			this.ordersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		return orderTotal;
	}
	
}
