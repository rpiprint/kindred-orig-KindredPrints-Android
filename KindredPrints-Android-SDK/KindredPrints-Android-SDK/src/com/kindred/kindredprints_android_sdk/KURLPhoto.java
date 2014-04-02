package com.kindred.kindredprints_android_sdk;

public class KURLPhoto extends KPhoto {
	private String origUrl;
	private String prevUrl;
	
	public KURLPhoto(String id, String origUrl) {
		init(id, origUrl, null);
	}
	public KURLPhoto(String id, String origUrl, String prevUrl) {
		init(id, origUrl, prevUrl);
	}
	private void init(String id, String orig,String preview) {
		this.id = id;
		this.type = TYPE_URL;
		this.origUrl = orig;
		this.prevUrl = preview;
	}
	
	public String getOrigUrl() {
		return origUrl;
	}
	public void setOrigUrl(String origUrl) {
		this.origUrl = origUrl;
	}
	
	public String getPrevUrl() {
		return prevUrl;
	}
	public void setPrevUrl(String prevUrl) {
		this.prevUrl = prevUrl;
	}
}
