package com.kindredprints.android.sdk.data;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.json.JSONException;
import org.json.JSONObject;

import com.kindredprints.android.sdk.data.CartObject;
import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.KCustomPhoto;
import com.kindredprints.android.sdk.KLOCPhoto;
import com.kindredprints.android.sdk.KMEMPhoto;
import com.kindredprints.android.sdk.KPhoto;
import com.kindredprints.android.sdk.KURLPhoto;
import com.kindredprints.android.sdk.R;
import com.kindredprints.android.sdk.helpers.ImageUploadHelper;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class CartManager {	
	private ArrayList<KPhoto> pendingPhotos;
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
		this.pendingPhotos = new ArrayList<KPhoto>();
		this.ordersSema_ = new Semaphore(1);
		this.selOrdersSema_ = new Semaphore(1);
	}
	
	public void setCartUpdatedCallback(CartUpdatedCallback callback) {
		this.callback_ = callback;
	}
	
	public static CartManager getInstance(Context context) {
		if (manager_ == null) {
			manager_ = new CartManager(context.getApplicationContext());
		} 
		return manager_;
	}
	
	public void updateAllOrdersWithNewSizes() {
		/*try {
			this.ordersSema_.acquire();
			ArrayList<PrintProduct> allSizes = this.devPrefHelper_.getCurrentSizes();
			for (CartObject cartObj : this.orders) {
				cartObj.updateOrderSizes(allSizes);
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		Handler mainHandler = new Handler(Looper.getMainLooper());
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
	
	public void imageWasUpdatedWithSizes(PartnerImage image, final ArrayList<PrintProduct> fittedProducts) {
		try {
			/*this.ordersSema_.acquire();
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
					break;
				}
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();*/

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

		final PartnerImage fpImage = image;
		Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (callback_ != null) {	
					callback_.orderHasBeenUpdatedWithSize(fpImage, fittedProducts);
				}
			}
		});
	}
	public void imageWasServerInit(String localId, String pid) {
		PartnerImage pImage = null;
		try {
			/*this.ordersSema_.acquire();
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
					break;
				}
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();*/
			
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
					break;
				}
			}
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		final PartnerImage fpImage = pImage;
		Handler mainHandler = new Handler(Looper.getMainLooper());
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
			/*this.ordersSema_.acquire();
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
					break;
				}
			}
			this.userPrefHelper_.setCartOrders(this.orders);
			this.ordersSema_.release();*/
			
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
					break;
				}
			}
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		final PartnerImage fpImage = pImage;
		Handler mainHandler = new Handler(Looper.getMainLooper());
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
	/*
	public int countOfOrders() {
		return this.orders.size();
	}*/
	public int countOfSelectedOrders() {
		return this.selectedOrders.size();
	}

	public int hasImageInCart(CartObject obj) {
		int present = -1;
		try {
			this.selOrdersSema_.acquire();
			for (int i = 0; i < this.selectedOrders.size(); i++) {
				PrintableImage cartObj = this.selectedOrders.get(i);
				if (cartObj.getImage().getId().equals(obj.getImage().getId()) || cartObj.getImage().getPartnerId().equals(obj.getImage().getPartnerId())) {
					Log.i("KindredSDK", "found object with id = " + obj.getImage().getId());
					present = i;
					break;
				}
			}
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return present;
	}
	
	/*
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
			
			Handler mainHandler = new Handler(Looper.getMainLooper());
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
	}*/
	
	public void deleteOrderImageAtIndex(int index) {
		try {
			this.selOrdersSema_.acquire();
				
			this.selectedOrders.remove(i);
			
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
		
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void addManyPartnerImages(ArrayList<KPhoto> photos) {
		this.pendingPhotos.addAll(photos);
	}
	
	public void addPartnerImage(KPhoto photo) {
		this.pendingPhotos.add(photo);
	}
	
	/*public void processPartnerImage(KPhoto photo) {
		PartnerImage pImage = new PartnerImage(photo);
		CartObject cartObj = new CartObject();		
		cartObj.setImage(pImage);
		if (addOrderImage(cartObj)) {
			cacheIncomingImage(pImage, photo);
		}
	}*/
	
	public void cacheIncomingImage(PartnerImage pImage, KPhoto photo) {
		ImageManager imManager = ImageManager.getInstance(context_);
		if (photo instanceof KMEMPhoto) {
			imManager.cacheOrigImageFromMemory(pImage, ((KMEMPhoto)photo).getBm());
		} else if (photo instanceof KLOCPhoto) {
			imManager.cacheOrigImageFromFile(pImage, ((KLOCPhoto)photo).getFilename());
		} else if (photo instanceof KURLPhoto){
			imManager.startPrefetchingOrigImageToCache(pImage);
		} else {
			String frontPreviewUrl = this.devPrefHelper_.getCustomPreviewImageUrl((KCustomPhoto)photo, true);
			String backPreviewUrl = this.devPrefHelper_.getCustomPreviewImageUrl((KCustomPhoto)photo, false);
					
			pImage.setPrevUrl(frontPreviewUrl);
			pImage.setUrl(frontPreviewUrl);
			PartnerImage backsideImage = new PartnerImage(photo);
			backsideImage.setPrevUrl(backPreviewUrl);
			backsideImage.setUrl(backPreviewUrl);
			pImage.setBackSideImage(backsideImage);
			imManager.startPrefetchingOrigImageToCache(pImage);
			imManager.startPrefetchingOrigImageToCache(pImage.getBackSideImage());
		}
	}
	
	/*
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
	}*/
	
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
	
	public int addOrderToSelected(CartObject cartObj, PrintProduct product) {
		this.ordersSema_.acquire();
		this.selOrdersSema_.acquire();
		if (product.getQuantity() > 0) {
			for (PrintableImage pImage : this.selectedOrders) {
				if (pImage.getImage().getId().equalsIgnoreCase(cartObj.getImage().getId()) 
						&& pImage.getPrintType().getId().equals(product.getId())) {
					newPImage = pImage.copy();
					if (pImage.getPrintType().getQuantity() != product.getQuantity()) {
						newPImage.setServerLineItemInit(false);
					} 
					break;
				}
			}
			
			if (product.getDpi() < product.getWarnDPI()) {
				lowDPI = true;
			}
			
			newPImage.setImage(cartObj.getImage().copy());
			newPImage.setPrintType(product.copy());

			this.selectedOrders.add(newPImage);
		}
		this.selOrdersSema_.release();
		this.ordersSema_.release();
		this.devPrefHelper_.setNeedUpdateOrderId(true);
	}

	public ArrayList<CartObject> getOrderImages() {
		return this.orders;
	}
	public ArrayList<PrintableImage> getSelectedOrderImages() {
		return this.selectedOrders;
	}
	public ArrayList<KPhoto> getPendingImages() {
		return this.pendingPhotos;
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

	public void cleanUpPendingImages() {
		this.pendingPhotos = new ArrayList<KPhoto>();
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
			this.selOrdersSema_.acquire();
			for (int i = 0; i<this.selectedOrders.size(); i++) {
				PrintableImage image = this.selectedOrders.get(i);
				if (image == null) {
					this.selectedOrders.remove(i);
					i = i - 1;
					needSave = true;
					continue;
				}
				
				orderTotal = orderTotal + image.getPrintType().getPrice()*image.getPrintType().getQuantity();
				
				if (needSave) {
					this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
				}
			}
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		return orderTotal;
	}
	
}
