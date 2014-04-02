package com.kindred.kindredprints_android_sdk;

public class KLOCPhoto extends KPhoto {
	private String filename;
	
	public KLOCPhoto(String id, String filename) {
		this.id = id;
		this.type = TYPE_LOCAL_FILE;
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
