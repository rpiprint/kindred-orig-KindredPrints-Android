package com.kindredprints.android.sdk.data;

import java.util.UUID;

import com.kindredprints.android.sdk.KCustomPhoto;
import com.kindredprints.android.sdk.KLOCPhoto;
import com.kindredprints.android.sdk.KMEMPhoto;
import com.kindredprints.android.sdk.KPhoto;
import com.kindredprints.android.sdk.KURLPhoto;

public class PartnerImage {
	public static final String SERVER_ID_NONE = "no_server_id";
	public static final String LOCAL_IMAGE_URL = "local_image";
	public static final String REMOTE_IMAGE_URL = "remote_image";
	
	private String partnerId;
	private String partnerData;
	
	private String id;
	private String serverId;
	private String url;

	private String prevUrl;

	private String type;
		
	private float cropOffset;
	private float width;
	private float height;
	private boolean localCached;
	private boolean thumbLocalCached;
	private boolean serverInit;
	private boolean uploadComplete;
	
	private boolean twosided;
	private PartnerImage backSideImage;
	
	public PartnerImage() {
		initFields();
	}

	public PartnerImage(KPhoto photo) {
		initFields();
		if (photo instanceof KURLPhoto) {
			init((KURLPhoto)photo);
		} else if (photo instanceof KMEMPhoto) {
			init((KMEMPhoto)photo);
		} else if (photo instanceof KLOCPhoto) {
			init((KLOCPhoto)photo);
		} else if (photo instanceof KCustomPhoto) {
			init((KCustomPhoto)photo);
		}
	}
	
	private void init(KURLPhoto photo) {
		this.setPartnerId(photo.getId());
		this.setPartnerData(photo.getId());
		this.setPrevUrl(photo.getPrevUrl());
		this.setUrl(photo.getOrigUrl());
		this.setType(REMOTE_IMAGE_URL);
		this.setTwosided(false);
	}
	public void init(KMEMPhoto photo) {
		this.setPartnerId(photo.getId());
		this.setPartnerData(photo.getId());
		this.setType(LOCAL_IMAGE_URL);
		this.setTwosided(false);
	}
	public void init(KLOCPhoto photo) {
		this.setPartnerId(photo.getId());
		this.setPartnerData(photo.getId());
		this.setUrl(photo.getFilename());
		this.setType(LOCAL_IMAGE_URL);
		this.setTwosided(false);
	}
	public void init(KCustomPhoto photo) {
		this.setPartnerId(photo.getId());
		this.setPartnerData(photo.getAssociatedData());
		this.setUrl(photo.getAssociatedData());
		this.setType(photo.getCustomType());
		this.setTwosided(true);
	}
	
	private void initFields() {
		this.id = String.valueOf(UUID.randomUUID());
		this.partnerData = this.id;
		this.partnerId = this.id;
		this.serverId = SERVER_ID_NONE;
		this.prevUrl = LOCAL_IMAGE_URL;
		this.url = LOCAL_IMAGE_URL;
		this.type = LOCAL_IMAGE_URL;
		this.cropOffset = -1;
		this.width = 0;
		this.height = 0;
		this.localCached = false;
		this.thumbLocalCached = false;
		this.serverInit = false;
		this.uploadComplete = false;
		this.twosided = false;
	}
	
	public PartnerImage copy() {
		PartnerImage partImage = new PartnerImage();
		partImage.setId(this.getId());
		partImage.setServerId(this.getServerId());
		partImage.setPartnerId(this.getPartnerId());
		partImage.setPartnerData(this.getPartnerData());
		partImage.setPrevUrl(this.getPrevUrl());
		partImage.setUrl(this.getUrl());
		partImage.setType(this.getType());
		partImage.setCropOffset(this.getCropOffset());
		partImage.setWidth(this.getWidth());
		partImage.setHeight(this.getHeight());
		partImage.setLocalCached(this.isLocalCached());
		partImage.setThumbLocalCached(this.isThumbLocalCached());
		partImage.setServerInit(this.isServerInit());
		partImage.setUploadComplete(this.isUploadComplete());
		partImage.setBackSideImage(this.getBackSideImage());
		partImage.setTwosided(this.isTwosided());
		return partImage;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getPrevUrl() {
		return prevUrl;
	}

	public void setPrevUrl(String prevUrl) {
		this.prevUrl = prevUrl;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public float getCropOffset() {
		return cropOffset;
	}

	public void setCropOffset(float cropOffset) {
		this.cropOffset = cropOffset;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public boolean isThumbLocalCached() {
		return thumbLocalCached;
	}

	public void setThumbLocalCached(boolean thumbLocalCached) {
		this.thumbLocalCached = thumbLocalCached;
	}

	public boolean isLocalCached() {
		return localCached;
	}

	public void setLocalCached(boolean localCached) {
		this.localCached = localCached;
	}

	public boolean isServerInit() {
		return serverInit;
	}

	public void setServerInit(boolean serverInit) {
		this.serverInit = serverInit;
	}

	public boolean isUploadComplete() {
		return uploadComplete;
	}

	public void setUploadComplete(boolean uploadComplete) {
		this.uploadComplete = uploadComplete;
	}

	public boolean isTwosided() {
		return twosided;
	}

	public void setTwosided(boolean twosided) {
		this.twosided = twosided;
	}

	public PartnerImage getBackSideImage() {
		return backSideImage;
	}

	public void setBackSideImage(PartnerImage backSideImage) {
		this.backSideImage = backSideImage;
	}

	public String getPartnerData() {
		return partnerData;
	}

	public void setPartnerData(String partnerData) {
		this.partnerData = partnerData;
	}
	
	
	
}
