package com.kindred.kindredprints_android_sdk;

import android.graphics.Bitmap;

public class KMEMPhoto extends KPhoto {
	private Bitmap bm;
	
	public KMEMPhoto (String id, Bitmap bm) {
		this.type = TYPE_LOCAL_MEM;
		this.id = id;
		this.bm = bm;
	}

	public Bitmap getBm() {
		return bm;
	}

	public void setBm(Bitmap bm) {
		this.bm = bm;
	}
}
