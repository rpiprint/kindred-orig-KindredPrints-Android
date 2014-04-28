package com.kindredprints.android.sdk.helpers.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.CartObject;
import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.data.PrintProduct;
import com.kindredprints.android.sdk.data.Size;
import com.kindredprints.android.sdk.helpers.ImageEditor;
import com.kindredprints.android.sdk.helpers.ImageUploadHelper;
import com.kindredprints.android.sdk.helpers.prefs.DevPrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class ImageManager {	
	private static final String PREFIX_THUMB = "thumb_";
	private static final String PREFIX_PREVIEW = "preview_";
	private static final String PREFIX_FULL = "full_";
	
	private static final int MAX_DOWNLOADS = 3;
	
	private Context context_;
	
	private FileCache fCache_;
	private ImageCache imCache_;
	private CartManager cartManager_;
	
	private Semaphore processingSema_;
	private Semaphore waitingviewSema_;
	
	private int currOrigDownloads_;
	private ArrayList<String> waitingToDownloadQueue_;
	private ArrayList<String> downloadingQueue_;
	private HashMap<String, PartnerImage> imageDetails_;
	private HashMap<String, ImageView> waitingViews_;
	private HashMap<String, ImageManagerCallback> waitingCallbacks_;
	private DevPrefHelper devPrefHelper_;
	private InterfacePrefHelper interfacePrefHelper_;
		
	private static ImageManager imManager_;
	
	public ImageManager() { }
	public ImageManager(Context context) {
		this.context_ = context;
		this.fCache_ = FileCache.getInstance(context);
		this.imCache_ = ImageCache.getInstance(context);
		this.cartManager_ = CartManager.getInstance(context);
		this.waitingToDownloadQueue_ = new ArrayList<String>();
		this.downloadingQueue_ = new ArrayList<String>();
		this.imageDetails_ = new HashMap<String, PartnerImage>();
		this.waitingViews_ = new HashMap<String, ImageView>();
		this.waitingCallbacks_ = new HashMap<String, ImageManagerCallback>();
		this.devPrefHelper_ = new DevPrefHelper(context);
		this.interfacePrefHelper_ = new InterfacePrefHelper(context);
		
		this.processingSema_ = new Semaphore(1);
		this.waitingviewSema_ = new Semaphore(1);
		
		this.currOrigDownloads_ = 0;
	}
	
	public static ImageManager getInstance(Context context) {
		if (imManager_ == null) {
			imManager_ = new ImageManager(context);
		}
		return imManager_;
	}

	public static String getOrigName(String ident) {
		return PREFIX_FULL + ident;
	}
	public static String getPreviewName(String ident) {
		return PREFIX_PREVIEW + ident;
	}
	public static String getThumbName(String ident) {
		return PREFIX_THUMB + ident;
	}
	
	
	public HashMap<String, Integer> getImageMetaDetails(PartnerImage image) {	
		return this.fCache_.getImageMetaDetails(getOrigName(image.getId()));
	}
	
	public String getFullFilename(PartnerImage image) {
		return this.fCache_.getFullFilename(getOrigName(image.getId()));
	}

	public void startPrefetchingOrigImageToCache(PartnerImage image) {
		String origId = getOrigName(image.getId());
		Log.i("KindredSDK", "starting to prefetch " + origId);
		if (!isOrigImageInProcess(origId) && !isOrigWaitingForProcess(origId) && !this.fCache_.hasImageForKey(origId)) {
			this.imageDetails_.put(origId, image);
			this.waitingToDownloadQueue_.add(origId);
			if (this.currOrigDownloads_ < MAX_DOWNLOADS) {
				this.currOrigDownloads_ = this.currOrigDownloads_ + 1;
				new Thread(new Runnable() {
					@Override
					public void run() {
						startNextOrigDownload();
					}
				}).start();
			}
		}
	}
	
	public boolean cacheOrigImageFromUrl(String key, String url) {
		return this.fCache_.addImageFromUrl(url, key);
	}
	
	public Bitmap getImageFromFileSystem(String key, Size imgSize) {
		return this.fCache_.getImageForKey(key, imgSize);
	}
	
	public void cacheOrigImageFromFile(PartnerImage image, String sourceFn) {
		String origId = getOrigName(image.getId());
		this.downloadingQueue_.add(origId);

		this.fCache_.addImageFromFile(sourceFn, origId);
		processImageInStorage(image, null);
	}
	
	public void cacheOrigImageFromMemory(PartnerImage image, Bitmap imgData) {
		String origId = getOrigName(image.getId());
		this.downloadingQueue_.add(origId);

		this.fCache_.addImage(imgData, origId);
		processImageInStorage(image, null);
	}
	
	public void deleteAllImagesFromCache(CartObject cartObj) {
		String origId = getOrigName(cartObj.getImage().getId());
		String prevId = getPreviewName(cartObj.getImage().getId());
		
		deleteFromCache(origId);
		deleteFromCache(prevId);
		
		if (cartObj.getImage().isTwosided()) {
			origId = getOrigName(cartObj.getImage().getBackSideImage().getId());
			prevId = getPreviewName(cartObj.getImage().getBackSideImage().getId());
			
			deleteFromCache(origId);
			deleteFromCache(prevId);
		}
		
		for (PrintProduct product : cartObj.getPrintProducts()) {
			String thumbId = product.getId() + "_" + getThumbName(cartObj.getImage().getId());
			String printPrevId = product.getId() + "_" + getPreviewName(cartObj.getImage().getId());
			
			deleteFromCache(thumbId);
			deleteFromCache(printPrevId);
			
			if (cartObj.getImage().isTwosided()) {
				thumbId = product.getId() + "_" + getThumbName(cartObj.getImage().getBackSideImage().getId());
				printPrevId = product.getId() + "_" + getPreviewName(cartObj.getImage().getBackSideImage().getId());
				
				deleteFromCache(thumbId);
				deleteFromCache(printPrevId);
			}
		}
	}
	
	private void deleteFromCache(String id) {
		this.imCache_.removeImage(id);
		this.fCache_.deleteImageForKey(id);
	}
	
	private void startNextOrigDownload() {
	    this.currOrigDownloads_ = this.currOrigDownloads_ - 1;
	    if (this.waitingToDownloadQueue_.size() > 0) {
	        this.currOrigDownloads_ = this.currOrigDownloads_ + 1 ;
			try {
		        this.processingSema_.acquire();
		        final String ident = this.waitingToDownloadQueue_.remove(this.waitingToDownloadQueue_.size()-1);
		        Log.i("KindredSDK", "Starting download on " + ident);
		        this.downloadingQueue_.add(ident);
		        this.processingSema_.release();
		        
		        Handler mainHandler = new Handler(context_.getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
				        AsyncPictureGetter picGetter = new AsyncPictureGetter();
				        picGetter.execute(ident);
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	}

	// add images to their respective caches
	private void processImageInStorage(PartnerImage image, PrintProduct size) {
		String origId = getOrigName(image.getId());
		HashMap<String, Integer> metaDetails = this.fCache_.getImageMetaDetails(origId);
		Size origSize = new Size(metaDetails.get(FileCache.IMAGE_META_WIDTH), metaDetails.get(FileCache.IMAGE_META_HEIGHT));
		float imageAspectRatio = origSize.getWidth()/origSize.getHeight();
		ArrayList<PrintProduct> sizesToCrop = new ArrayList<PrintProduct>();
		if (size == null) {
			if (image.isTwosided()) {
				sizesToCrop.addAll(ImageEditor.getAllowablePrintableSizesForImageSize(origSize, this.devPrefHelper_.getCurrentSizes(), ImageEditor.FILTER_DOUBLE));
			} else {
				sizesToCrop.addAll(ImageEditor.getAllowablePrintableSizesForImageSize(origSize, this.devPrefHelper_.getCurrentSizes(), ImageEditor.NO_FILTER));
			}
			float scaleFactor = 1.0f;
			if (origSize.getWidth() > origSize.getHeight()) {
				scaleFactor = this.interfacePrefHelper_.getPreviewMaxSize()/origSize.getWidth();
			} else {
				scaleFactor = this.interfacePrefHelper_.getPreviewMaxSize()/origSize.getHeight();
			}
			size = new PrintProduct();
			size.setPreviewSize(new Size(origSize.getWidth()*scaleFactor, origSize.getHeight()*scaleFactor));
		} else {
			sizesToCrop.add(size);
		}
		
		if (sizesToCrop.size() > 0) {
			Size maxSize = sizesToCrop.get(0).getTrimmed();
			for (PrintProduct prod : sizesToCrop) {
				Size sProd = prod.getTrimmed();
				if (sProd.getWidth() > maxSize.getWidth() || sProd.getHeight() > maxSize.getHeight()) {
					maxSize = sProd;
				}
			}
			
			for (PrintProduct sProd : sizesToCrop) {				
				float thumbSizeWidth = sProd.getThumbSize().getWidth()*sProd.getTrimmed().getWidth()/maxSize.getWidth();
				float thumbSizeHeight = sProd.getThumbSize().getHeight()*sProd.getTrimmed().getHeight()/maxSize.getHeight();
				Size cropSize = new Size(thumbSizeWidth, thumbSizeHeight);
				
				Bitmap thumbRaw = this.fCache_.getImageForKey(origId, cropSize);
				if (imageAspectRatio < 1) {
					cropSize = new Size(thumbSizeHeight, thumbSizeWidth);
				}
				sProd.setThumbSize(cropSize);

				Bitmap thumb = ImageEditor.format_image(
						thumbRaw, 
						metaDetails.get(FileCache.IMAGE_META_ORIENT), 
						image.getCropOffset(), cropSize, 
						this.interfacePrefHelper_.getBorderWidth(sProd.getBorderPerc(), cropSize), 
						this.interfacePrefHelper_.getBorderColor());
				String thumbUid = sProd.getId() + "_" + getThumbName(image.getId());
				
				this.fCache_.addImage(thumb, thumbUid);
				this.imCache_.addImage(thumb, null, thumbUid);
				
				float prevSizeWidth = sProd.getPreviewSize().getWidth()*sProd.getTrimmed().getWidth()/maxSize.getWidth();
				float prevSizeHeight = sProd.getPreviewSize().getHeight()*sProd.getTrimmed().getHeight()/maxSize.getHeight();
				cropSize = new Size(prevSizeWidth, prevSizeHeight);
				Bitmap prevRaw = this.fCache_.getImageForKey(origId, cropSize);
				if (imageAspectRatio < 1) {
					cropSize = new Size(prevSizeHeight, prevSizeWidth);
				}
				sProd.setPreviewSize(cropSize);

				Bitmap preview = ImageEditor.format_image(
						prevRaw,
						metaDetails.get(FileCache.IMAGE_META_ORIENT), 
						image.getCropOffset(), 
						cropSize,
						this.interfacePrefHelper_.getBorderWidth(sProd.getBorderPerc(), cropSize), 
						this.interfacePrefHelper_.getBorderColor());
				String prevUid = sProd.getId() + "_" + getPreviewName(image.getId());
				
				if (image.isThumbLocalCached() || image.getUrl().equals(image.getPrevUrl())) {
					if (origSize.getWidth() > origSize.getHeight()) {
						sProd.setDpi(origSize.getWidth()/sProd.getTrimmed().getWidth());
					} else {
						sProd.setDpi(origSize.getHeight()/sProd.getTrimmed().getHeight());
					}
				}
				
				this.fCache_.addImage(preview, prevUid);
				this.imCache_.addImage(preview, null, prevUid);
				
				assignAnyWaitingViewsForId(thumbUid);
				assignAnyWaitingViewsForId(prevUid);
			}
		}
		
		String prevId = getPreviewName(image.getId());

		Bitmap preview = this.fCache_.getImageForKey(origId, size.getPreviewSize());
		this.fCache_.addImage(preview, prevId);
		this.imCache_.addImage(preview, null, prevId);
		
		image.setWidth(metaDetails.get(FileCache.IMAGE_META_WIDTH));
		image.setHeight(metaDetails.get(FileCache.IMAGE_META_HEIGHT));
		
		if (image.getType().equals(PartnerImage.REMOTE_IMAGE_URL)) {
			if (!image.isThumbLocalCached() && !image.getPrevUrl().equals(image.getUrl())) {
				image.setThumbLocalCached(true);
			} else {
				image.setThumbLocalCached(true);
				image.setLocalCached(true);
			}
		} else {
			image.setLocalCached(true);
			image.setThumbLocalCached(true);
		}
		
		
		this.cartManager_.imageWasUpdatedWithSizes(image, sizesToCrop);
		ImageUploadHelper.getInstance(this.context_).imageReadyForUpload(image);
		
		assignAnyWaitingViewsForId(prevId);
		try {
			this.processingSema_.acquire();
			this.downloadingQueue_.remove(origId);
			this.processingSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!image.isLocalCached() && !isOrigImageInProcess(getOrigName(image.getId()))) {
			startPrefetchingOrigImageToCache(image);
		}
	}

	// returns yes if the image download is in process or if the file cache contains the full image
	private boolean isOrigImageInProcess(String uniqueId) {
		try {
			this.processingSema_.acquire();
		    for (int i = 0; i < this.downloadingQueue_.size(); i++) {
		    	if (this.downloadingQueue_.get(i).equalsIgnoreCase(uniqueId)) {
		    		this.processingSema_.release();
		    		return true;
		    	}
		    }
		    this.processingSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    return false;
	}
	
	private boolean isOrigWaitingForProcess(String uniqueId) {
		try {
			this.processingSema_.acquire();
			for (int i = 0; i < this.waitingToDownloadQueue_.size(); i++) {
		    	if (this.waitingToDownloadQueue_.get(i).equalsIgnoreCase(uniqueId)) {
		    		this.processingSema_.release();
		    		return true;
		    	}
		    }	    
			this.processingSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	// scan through the waiting views, looking if there are any that need images
	private void assignAnyWaitingViewsForId(final String uniqueId) {
		try {
			this.waitingviewSema_.acquire();
		    final ImageView view = this.waitingViews_.get(uniqueId);
		    final ImageManagerCallback callback = this.waitingCallbacks_.get(uniqueId);
		    if (view != null) {
		    	Handler mainHandler = new Handler(context_.getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
				        view.setImageBitmap(imCache_.getImageForKey(uniqueId, view));
				        if (callback != null) callback.imageAssigned();
					}
				});
				this.waitingCallbacks_.remove(uniqueId);
		        this.waitingViews_.remove(uniqueId);
		    }
			this.waitingviewSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setImageAsync(final ImageView view, final PartnerImage image, PrintProduct product, final Size displaySize, final ImageManagerCallback callback) {
		String uid = null;
		if (displaySize.getHeight() <= this.interfacePrefHelper_.getThumbMaxSize() && displaySize.getWidth() <= this.interfacePrefHelper_.getThumbMaxSize()) {
			uid = product.getId() + "_" + getThumbName(image.getId());
		} else if (product != null) {
			uid = product.getId() + "_" + getPreviewName(image.getId());
		} else {
			uid = getPreviewName(image.getId());
		}
				
		final String fUid = uid;
		if (this.imCache_.hasImage(uid)) {
			Bitmap bm = this.imCache_.getImageForKey(uid, view);
			view.setImageBitmap(bm);
			if (callback != null) callback.imageAssigned();
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (fCache_.hasImageForKey(fUid)) {
						Handler mainHandler = new Handler(context_.getMainLooper());
						final Bitmap bm = fCache_.getImageForKey(fUid, displaySize);
						imCache_.addImage(bm, view, fUid);
						mainHandler.post(new Runnable() {
							@Override
							public void run() {
								view.setImageBitmap(bm);
							}
						});
						assignAnyWaitingViewsForId(fUid);
						
					} else {
						try {
							waitingviewSema_.acquire();
							if (callback != null) waitingCallbacks_.put(fUid, callback);
							waitingViews_.put(fUid, view);
							waitingviewSema_.release();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (!isOrigImageInProcess(getOrigName(image.getId())) && !isOrigWaitingForProcess(getOrigName(image.getId()))) {
							startPrefetchingOrigImageToCache(image);
						}
					}
				}
			}).start();
			
		}
	}
	
	private class AsyncPictureGetter extends AsyncTask<String, Void, String> {
		private boolean success;
		@Override
		protected String doInBackground(String... params) {
			String ident = params[0];
			
			this.success = false;
			PartnerImage image = imageDetails_.get(ident);
			if (image.getType().equalsIgnoreCase(PartnerImage.LOCAL_IMAGE_URL)) {
				this.success = fCache_.addImageFromFile(image.getUrl(), ident);
			} else {
				String url = "";
				if (!image.isThumbLocalCached() && !image.getPrevUrl().equals(image.getUrl())) {
					url = image.getPrevUrl();
				} else {
					image.setThumbLocalCached(true);
					url = image.getUrl();
				}
				this.success = fCache_.addImageFromUrl(url, ident);
					
			}
			
			return ident;
		}
		
		@Override
		protected void onPostExecute(final String ident) {
			if (fCache_.hasImageForKey(ident) && success) {
				// succeeded
				new Thread(new Runnable() {
					@Override
					public void run() {	
						processImageInStorage(imageDetails_.get(ident), null);
						imageDetails_.remove(ident);
					}
				}).start();
			} else {
				cartManager_.deleteOrderImageForId(imageDetails_.get(ident).getId());
				try {
					processingSema_.acquire();
					downloadingQueue_.remove(ident);
					processingSema_.release();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				imageDetails_.remove(ident);
			}
			startNextOrigDownload();
			
		}
	}
	
	public interface ImageManagerCallback {
		public void imageAssigned();
	}
}
