package com.kindredprints.android.sdk.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.kindredprints.android.sdk.data.CartManager;
import com.kindredprints.android.sdk.data.PartnerImage;
import com.kindredprints.android.sdk.data.PrintableImage;
import com.kindredprints.android.sdk.data.UserObject;
import com.kindredprints.android.sdk.helpers.cache.FileCache;
import com.kindredprints.android.sdk.helpers.cache.ImageManager;
import com.kindredprints.android.sdk.helpers.prefs.InterfacePrefHelper;
import com.kindredprints.android.sdk.helpers.prefs.UserPrefHelper;
import com.kindredprints.android.sdk.remote.KindredRemoteInterface;
import com.kindredprints.android.sdk.remote.NetworkCallback;

public class ImageUploadHelper {
	private ImageUploadCallback callback_;
	private UserPrefHelper userPrefHelper_;
	private InterfacePrefHelper interfacePrefHeper_;
	
	private int currUploadCount_;
	private ArrayList<String> finishedPile_;
	private ArrayList<String> uploadQueue_;
	private ArrayList<String> inprogressList_;
	private HashMap<String, Object> processingBin_;
	private HashMap<String, JSONObject> imageUploadMap_;
	
	private Semaphore finishedSema_;
	private Semaphore processingSema_;
	private Semaphore printablesSema_;
	
	private ArrayList<String> pendingPrintables_;
	private HashMap<String, ArrayList<PrintableImage>> printableMap_;
	
	private UserObject currUser_;
	private KindredRemoteInterface kRemoteInt_;
	private CartManager cartManager_;
	private ImageManager imgManager_;
	
	private static final int MAX_UPLOADS = 2;
	
	private static ImageUploadHelper uploadHelper_;
	
	public ImageUploadHelper() { }
	public ImageUploadHelper(Context context) {
		this.interfacePrefHeper_ = new InterfacePrefHelper(context);
		this.userPrefHelper_ = new UserPrefHelper(context);
		this.currUploadCount_ = 0;
		this.finishedPile_ = new ArrayList<String>();
		this.uploadQueue_ = new ArrayList<String>();
		this.inprogressList_ = new ArrayList<String>();
		this.processingBin_ = new HashMap<String, Object>();
		this.imageUploadMap_ = new HashMap<String, JSONObject>();
		
		this.finishedSema_ = new Semaphore(1);
		this.processingSema_ = new Semaphore(1);
		this.printablesSema_ = new Semaphore(1);
		
		this.pendingPrintables_ = new ArrayList<String>();
		this.printableMap_ = new HashMap<String, ArrayList<PrintableImage>>();
		
		this.kRemoteInt_ = new KindredRemoteInterface(context);
		this.kRemoteInt_.setNetworkCallbackListener(new ImageSyncCallback());
		this.cartManager_ = CartManager.getInstance(context);
		this.imgManager_ = ImageManager.getInstance(context);
	}
	
	public static ImageUploadHelper getInstance(Context context) {
		if (uploadHelper_ == null) {
			uploadHelper_ = new ImageUploadHelper(context);
		}
		return uploadHelper_;
	}
	
	public void setUploadCallback(ImageUploadCallback callback) {
		this.callback_ = callback;
	}
	
	public void imageReadyForUpload(PartnerImage image) {
		this.currUser_ = this.userPrefHelper_.getUserObject();
		if (this.currUser_.getId() != null && !this.currUser_.getId().equalsIgnoreCase(UserObject.USER_VALUE_NONE)) {
			processImageForServerSync(image);
		}
	}
	
	public void validateAllOrdersInit() {
		this.currUser_ = this.userPrefHelper_.getUserObject();
		if (this.currUser_.getId() != null && !this.currUser_.getId().equalsIgnoreCase(UserObject.USER_VALUE_NONE)) {
			ArrayList<PrintableImage> orders = this.cartManager_.getSelectedOrderImages();
			this.currUploadCount_ = 0;
			for (PrintableImage pImage : orders) {
				processImageForServerSync(pImage.getImage());
				if (!pImage.getImage().isServerInit()) {
					addPrintableImageToList(pImage);
				} else {
					processPrintableImageForServerSync(pImage);
				}
			}
		}
	}
	
	private String getPrintableTag(PrintableImage image) {
		return image.getImage().getId() + "-" +image.getPrintType().getId();
	}
	
	private void processPrintableImageForServerSync(PrintableImage image) {
		if (!image.isServerInit() || !image.isServerLineItemInit()) {
			if (addImageToQueue(getPrintableTag(image), image)) {
				if (this.currUploadCount_ < MAX_UPLOADS) {
					this.currUploadCount_ = this.currUploadCount_ + 1;
					processNextImage();
				} 
			} 
		} else {
			addToFinishedPile(getPrintableTag(image));
			callBackAsAppropriate();
		}
	}
	
	private void processImageForServerSync(PartnerImage image) {
		if (!image.isServerInit() || !image.isUploadComplete()) {
			if (addImageToQueue(image.getId(), image)) {
				if (this.currUploadCount_ < MAX_UPLOADS) {
					this.currUploadCount_ = this.currUploadCount_ + 1;
					processNextImage();
				}
			} 
		} else {
			addToFinishedPile(image.getId());
			callBackAsAppropriate();
		 }
	}
	
	private void addPrintableImageToUploadQueueIfExists(String localId, String serverId) {
		try {
			this.printablesSema_.acquire();
			for (int i = 0; i < this.pendingPrintables_.size(); i++) {
				String pid = this.pendingPrintables_.get(i);
				if (pid.equalsIgnoreCase(localId)) {	
					ArrayList<PrintableImage> pImages = this.printableMap_.remove(localId);
					for (PrintableImage pImage : pImages) { 
						pImage.getImage().setServerId(serverId);
						pImage.getImage().setServerInit(true);
						processPrintableImageForServerSync(pImage);
					}
					this.pendingPrintables_.remove(i);	
					this.printablesSema_.release();
					return;
				}
			}
			this.printablesSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void addPrintableImageToList(PrintableImage image) {
		try {
			this.printablesSema_.acquire();
			for (String pid : this.pendingPrintables_) {
				if (pid.equalsIgnoreCase(image.getImage().getId())) {
					ArrayList<PrintableImage> pImages = this.printableMap_.get(image.getImage().getId());
					pImages.add(image);
					this.printableMap_.put(image.getImage().getId(), pImages);
					this.printablesSema_.release();
					return;
				}
			}
			ArrayList<PrintableImage> pImages = new ArrayList<PrintableImage>();
			pImages.add(image);
			this.pendingPrintables_.add(image.getImage().getId());
			this.printableMap_.put(image.getImage().getId(), pImages);
			this.printablesSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private boolean addImageToQueue(String id, Object image) {
		try {
			this.processingSema_.acquire();
			for (String pid : this.uploadQueue_) {
				if (pid.equalsIgnoreCase(id)) {
					this.processingSema_.release();
					return false;
				}
			}
			
			for (String pid : this.inprogressList_) {
				if (pid.equalsIgnoreCase(id)) {
					this.processingSema_.release();
					return false;
				}
			}
			this.processingBin_.put(id, image);
			this.uploadQueue_.add(id);
			this.processingSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private void addToFinishedPile(String picId) {
		try {
			this.finishedSema_.acquire();
			for (String pid : this.finishedPile_) {
				if (pid.equalsIgnoreCase(picId)) {
					this.finishedSema_.release();
					return;
				}
			}
			this.finishedPile_.add(picId);
			this.finishedSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void removeStringFromProcessing(String pid) {
		try {
			this.processingSema_.acquire();
			for (int i=0; i < this.inprogressList_.size(); i++) {
				if (this.inprogressList_.get(i).equals(pid)) {
					this.inprogressList_.remove(i);
					break;
				}
			}
			this.processingSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void callBackAsAppropriate() {
		if (this.callback_ != null) {
			float totalImages = this.uploadQueue_.size() + this.inprogressList_.size() + this.finishedPile_.size();
			float totalFinished = this.finishedPile_.size();
			this.callback_.uploadFinishedWithOverallProgress(totalFinished/totalImages);
			if (totalImages == totalFinished)
				this.callback_.uploadsHaveCompleted();
		}
	}
	
	private void processNextImage() {
		try {
			this.processingSema_.acquire();
			this.currUploadCount_ = this.currUploadCount_ - 1;
			if (this.uploadQueue_.size() > 0) {
				this.currUploadCount_ = this.currUploadCount_ + 1;

				final String pid = this.uploadQueue_.get(0);
				this.inprogressList_.add(pid);
				this.uploadQueue_.remove(0);
				
				this.processingSema_.release();
				new Thread(new Runnable() {
					@Override
					public void run() {
						Object obj = processingBin_.get(pid);
						if (obj instanceof PartnerImage) {
							PartnerImage image = (PartnerImage) obj;
							if (!image.isServerInit()) {
								if (!image.isTwosided()) {
									initImageOnServer(image);
								} else {
									initImageOnFauxServer(image);
								}
							} else {
								uploadImageFromMemory(image);
							}
						} else if (obj instanceof PrintableImage) {
							PrintableImage image = (PrintableImage) obj;
							if (image.isServerInit()) {
								initOrUpdateLineItemObjectOnServer(image);
							} else {
								if (!image.getImage().isTwosided()) {
									initPrintableImageOnServer(image);
								} else {
									initCustomPrintableImageOnServer(image);
								}
							}
						}
					}
				}).start();
			} else {
				this.processingSema_.release();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void initImageOnFauxServer(PartnerImage image) {
		addPrintableImageToUploadQueueIfExists(image.getId(), image.getId());
		cartManager_.imageWasServerInit(image.getId(), image.getId());
		cartManager_.imageFinishedUploading(image.getId());
		finishedPile_.add(image.getId());
		processingBin_.remove(image.getId());
		removeStringFromProcessing(image.getId());
		processNextImage();
	}
	
	private void initImageOnServer(PartnerImage image) {
		JSONObject postObj = new JSONObject();
		JSONObject postMetaObj = new JSONObject();
		HashMap<String, Integer> imageMeta = imgManager_.getImageMetaDetails(image);
		
		try {
			postObj.put("user_id", currUser_.getId());
			
			if (imageMeta.containsKey(FileCache.IMAGE_META_WIDTH)) {
				postMetaObj.put("width", imageMeta.get(FileCache.IMAGE_META_WIDTH));
			}
			if (imageMeta.containsKey(FileCache.IMAGE_META_HEIGHT)) {
				postMetaObj.put("height", imageMeta.get(FileCache.IMAGE_META_HEIGHT));
			}
			if (imageMeta.containsKey(FileCache.IMAGE_META_ORIENT)) {
				postMetaObj.put("orient", imageMeta.get(FileCache.IMAGE_META_ORIENT));
			}
			
			if (postMetaObj.length() > 0) {
				postObj.put("source_metadata", postMetaObj);
			}
			
			if (image.getType().equals(PartnerImage.LOCAL_IMAGE_URL)) {
				postObj.put("file_size", imageMeta.get(FileCache.IMAGE_META_FSIZE));
				postObj.put("source", "phone");
				
				this.kRemoteInt_.createImage(postObj, image.getId());
			} else if (image.getType().equals(PartnerImage.REMOTE_IMAGE_URL)) {
				postObj.put("remote_url", image.getUrl());
				postObj.put("source", "remote");
				this.kRemoteInt_.createURLImage(postObj, image.getId());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}			
	}
	
	private void initCustomPrintableImageOnServer(PrintableImage image) {
		JSONObject postObj = new JSONObject();
		JSONObject postOpsObj = new JSONObject();
		JSONObject postCustomImgObj = new JSONObject();

		try {
			postCustomImgObj.put("type", image.getImage().getType());
			postCustomImgObj.put("data", image.getImage().getPartnerData());
			postOpsObj.put("custom", postCustomImgObj);
			postObj.put("user_id", currUser_.getId());
			postObj.put("operations", postOpsObj);
			postObj.put("type", "custom");
			Log.i("KindredSDK", "creating custom pi = " + postObj.toString());
			this.kRemoteInt_.createPrintableImage(postObj, getPrintableTag(image));
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	private void initPrintableImageOnServer(PrintableImage image) {
		JSONObject postObj = new JSONObject();
		JSONObject postOpsObj = new JSONObject();
		JSONObject postSourceImgObj = new JSONObject();
		
		try {
			postSourceImgObj.put("id", image.getImage().getServerId());
			postOpsObj.put("source", postSourceImgObj);
			postOpsObj.put("border", this.interfacePrefHeper_.getBorderSize(image.getPrintType().getBorderPerc()));
			postObj.put("user_id", currUser_.getId());
			postObj.put("operations", postOpsObj);
			postObj.put("type", image.getPrintType().getType());
			
			this.kRemoteInt_.createPrintableImage(postObj, getPrintableTag(image));
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	private void initOrUpdateLineItemObjectOnServer(PrintableImage image) {
		JSONObject postObj = new JSONObject();
		try {
			if (image.getServerLineItemId().equals(PrintableImage.NO_SERVER_INIT)) {
				postObj.put("user_id", currUser_.getId());
				postObj.put("printableimage_id", image.getServerId());
				postObj.put("quantity", image.getPrintType().getQuantity());
				postObj.put("price_id", image.getPrintType().getId());
				this.kRemoteInt_.createLineItem(postObj, getPrintableTag(image));
			} else {
				postObj.put("id", image.getServerLineItemId());
				postObj.put("quantity", image.getPrintType().getQuantity());
				this.kRemoteInt_.updateLineItem(postObj, image.getServerLineItemId(), getPrintableTag(image));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	private void uploadImageFromMemory(PartnerImage image) {
		JSONObject post = new JSONObject();
		try {
			if (this.imageUploadMap_.containsKey(image.getId())) {
				String filename = this.imgManager_.getFullFilename(image);
				post = this.imageUploadMap_.get(image.getId());
				this.kRemoteInt_.uploadImage(post, filename, image.getId());
			} else {
				post.put("id", image.getServerId());
				this.kRemoteInt_.checkStatusOfImage(post, image.getServerId(), image.getId());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public class ImageSyncCallback implements NetworkCallback {
		@Override
		public void finished(JSONObject serverResponse) {
			if (serverResponse != null) {
				try {
					int status = serverResponse.getInt(KindredRemoteInterface.KEY_SERVER_CALL_STATUS_CODE);
					String requestTag = serverResponse.getString(KindredRemoteInterface.KEY_SERVER_CALL_TAG);
					String identTag = serverResponse.getString(KindredRemoteInterface.KEY_SERVER_CALL_IDENT);
					
					if (requestTag.equals(KindredRemoteInterface.REQ_TAG_CREATE_IMAGE) || requestTag.equals(KindredRemoteInterface.REQ_TAG_CHECK_IMAGE_STATUS)) {
						if (status == 200) {
							String sId = serverResponse.getString("id");
							cartManager_.imageWasServerInit(identTag, sId);
							addPrintableImageToUploadQueueIfExists(identTag, sId);
							PartnerImage image = (PartnerImage) processingBin_.get(identTag);
							image.setServerId(sId);
							image.setServerInit(true);
							processingBin_.put(identTag, image);
							if (serverResponse.has("upload")) {
								imageUploadMap_.put(identTag, serverResponse.getJSONObject("upload"));
								uploadQueue_.add(identTag);
							} else {
								if (!serverResponse.getString("upload_status").equals("succeeded")) {
									uploadQueue_.add(identTag);
								}
							}
						}
						removeStringFromProcessing(identTag);
						processNextImage();
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_CREATE_URL_IMAGE)) {
						if (status == 200) {
							String sId = serverResponse.getString("id");
							addPrintableImageToUploadQueueIfExists(identTag, sId);
							cartManager_.imageWasServerInit(identTag, sId);
							cartManager_.imageFinishedUploading(identTag);
							finishedPile_.add(identTag);
							processingBin_.remove(identTag);
						} else {
							uploadQueue_.add(identTag);
						}
						removeStringFromProcessing(identTag);
						processNextImage();
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_UPLOAD_IMAGE)) {
						if (status == 200) {
							cartManager_.imageFinishedUploading(identTag);
							finishedPile_.add(identTag);
							processingBin_.remove(identTag);
							
							removeStringFromProcessing(identTag);
							processNextImage();
						} else {
							PartnerImage image = (PartnerImage) processingBin_.get(identTag);
							uploadImageFromMemory(image);
						}
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_CREATE_PRINTABLE_IMAGE)) {
						if (status == 200) {
							String sId = serverResponse.getString("id");
							cartManager_.selectedPrintableImageWasServerInit(identTag, sId);
							PrintableImage pImage = (PrintableImage) processingBin_.get(identTag);
							pImage.setServerId(sId);
							pImage.setServerInit(true);
							processingBin_.put(identTag, pImage);
						}
						uploadQueue_.add(identTag);
						removeStringFromProcessing(identTag);
						processNextImage();
					} else if (requestTag.equals(KindredRemoteInterface.REQ_TAG_CREATE_LINE_ITEM) || requestTag.equals(KindredRemoteInterface.REQ_TAG_UPDATE_LINE_ITEM)) {
						if (status == 200) {
							String sId = serverResponse.getString("id");
							cartManager_.selectedPrintableImageWasLineItemInit(identTag, sId);
							
							finishedPile_.add(identTag);
							processingBin_.remove(identTag);
						} else {
							uploadQueue_.add(identTag);
						}
						removeStringFromProcessing(identTag);
						processNextImage();
					}
					callBackAsAppropriate();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
			
	public interface ImageUploadCallback {
		public void uploadsHaveCompleted();
		public void uploadFinishedWithOverallProgress(float progress);
	}
}
