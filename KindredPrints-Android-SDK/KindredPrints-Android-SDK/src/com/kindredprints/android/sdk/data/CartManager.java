package com.kindredprints.android.sdk.data;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.KCustomPhoto;
import com.kindredprints.android.sdk.KLOCPhoto;
import com.kindredprints.android.sdk.KMEMPhoto;
import com.kindredprints.android.sdk.KPhoto;
import com.kindredprints.android.sdk.KURLPhoto;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


public class CartManager {	
	private ArrayList<KPhoto> pendingPhotos;
	private ArrayList<PrintableImage> selectedOrders;
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
		this.selectedOrders = this.userPrefHelper_.getSelectedOrders();
		this.pendingPhotos = new ArrayList<KPhoto>();
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
	
	public void updateIntroImageUrls(final ArrayList<String> pageUrls) {
		Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				callback_.introPagesHaveBeenUpdated(pageUrls);
			}
		});
	}
	
	public void updateAllOrdersWithNewSizes() {
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
			this.selOrdersSema_.acquire();
			for (PrintableImage printImage : this.selectedOrders) {
				if (printImage.getImage().getId().equalsIgnoreCase(image.getId()) && product.getId().equalsIgnoreCase(printImage.getPrintType().getId())) {			
					printImage.getPrintType().setQuantity(product.getQuantity());
				}
			}
			this.devPrefHelper_.setNeedUpdateOrderId(true);
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			this.selOrdersSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void imageWasUpdatedWithSizes(PartnerImage image, final ArrayList<PrintProduct> fittedProducts) {
		try {
			this.selOrdersSema_.acquire();
			for (int i = 0; i < this.selectedOrders.size(); i++) {
				PrintableImage sImage = this.selectedOrders.get(i);
				if (sImage.getImage().getId().equalsIgnoreCase(image.getId()) && fittedProducts.size() > 0) {
					if (!sImage.getPrintType().getId().equalsIgnoreCase(fittedProducts.get(0).getId())) {
						sImage.setPrintType(fittedProducts.get(0));
					}
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

	public int countOfSelectedOrders() {
		return this.selectedOrders.size();
	}

	public int hasPartnerIdInCart(String partnerId) {
		int present = -1;
		try {
			this.selOrdersSema_.acquire();
			for (int i = 0; i < this.selectedOrders.size(); i++) {
				PrintableImage cartObj = this.selectedOrders.get(i);
				if (cartObj.getImage().getPartnerId().equals(partnerId)) {
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
	
	public int hasImageInCart(PrintableImage obj) {
		int present = -1;
		try {
			this.selOrdersSema_.acquire();
			for (int i = 0; i < this.selectedOrders.size(); i++) {
				PrintableImage cartObj = this.selectedOrders.get(i);
				if (cartObj.getImage().getId().equals(obj.getImage().getId()) || cartObj.getImage().getPartnerId().equals(obj.getImage().getPartnerId())) {
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
	
	public void deleteSelectedOrderImageForId(String localid) {
		try {
			this.selOrdersSema_.acquire();
			Log.i("KindredSDK", "deleting image at " + localid);
			for (int j=0; j<this.selectedOrders.size(); j++) {
				PrintableImage image = this.selectedOrders.get(j);
				if (image.getImage().getId().equalsIgnoreCase(localid)) {
					ImageManager.getInstance(context_).deleteAllImagesFromCache(image.getImage(), image.getPrintType());

					this.selectedOrders.remove(j);
					j = j - 1;
				}
			}
			this.userPrefHelper_.setSelectedOrders(this.selectedOrders);
			
	
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
	}
	
	public void addManyPartnerImages(ArrayList<KPhoto> photos) {
		this.pendingPhotos.addAll(photos);
	}
	
	public void addPartnerImage(KPhoto photo) {
		this.pendingPhotos.add(photo);
	}
	
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
	
	public int addOrderToSelected(PrintableImage pImage, PrintProduct product) {
		int found = -1;
		try {
			this.selOrdersSema_.acquire();
			if (product.getQuantity() > 0) {
				for (int i = 0; i < this.selectedOrders.size(); i++) {
					PrintableImage printImage = this.selectedOrders.get(i);
					if (printImage.getImage().getId().equalsIgnoreCase(pImage.getImage().getId()) 
							&& printImage.getPrintType().getId().equals(product.getId())) {
						found = i;
						if (printImage.getPrintType().getQuantity() != product.getQuantity()) {
							printImage.setServerLineItemInit(false);
							printImage.getPrintType().setQuantity(product.getQuantity());
						} 
						break;
					}
				}
				
				if (found < 0) {
					PrintableImage newPrintImage = pImage.copy();
					newPrintImage.setPrintType(product.copy());
					this.selectedOrders.add(newPrintImage);
				}
			}
			this.selOrdersSema_.release();
			this.devPrefHelper_.setNeedUpdateOrderId(true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		return found;
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
		this.selectedOrders = new ArrayList<PrintableImage>();
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
