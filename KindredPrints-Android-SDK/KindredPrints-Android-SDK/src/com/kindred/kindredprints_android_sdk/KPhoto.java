package com.kindred.kindredprints_android_sdk;

public class KPhoto {
	protected static final String TYPE_URL = "kphoto_url";
	protected static final String TYPE_LOCAL_FILE = "kphoto_file";
	protected static final String TYPE_LOCAL_MEM = "kphoto_mem";
	protected String id;
	protected String type;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	

}
