package com.kindred.kindredprints_android_sdk.helpers.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;


public class ImageCache {
	private static final int MAX_CACHE_SIZE = 12;

	private Context context_;
	
	private Semaphore mapSema_;
	private Semaphore ageQueueSema_;
	
	private HashMap<String, Bitmap> map;
	private HashMap<String, ImageView> viewMap;
	private ArrayList<String> ageQueue;
	
	private static ImageCache cache_;
	
	public static ImageCache getInstance(Context activity) {
		if (cache_ == null) {
			cache_ = new ImageCache();
			cache_.context_ = activity;
			cache_.map = new HashMap<String, Bitmap>();
			cache_.viewMap = new HashMap<String, ImageView>();
			cache_.ageQueue = new ArrayList<String>();
			cache_.mapSema_ = new Semaphore(1);
			cache_.ageQueueSema_ = new Semaphore(1);
		}
		return cache_;
	}
	
	public void addImage(Bitmap image, ImageView v, String key) {
		if (this.map.size() > MAX_CACHE_SIZE)
			ejectOldestItem();
		try {
			this.mapSema_.acquire();
			this.ageQueueSema_.acquire();
			
			if (!refreshKeyIfExist(key))
				this.ageQueue.add(key);
			
			this.map.put(key, image);
			if (v != null) this.viewMap.put(key, v);
			
			this.mapSema_.release();
			this.ageQueueSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
	public Bitmap getImageForKey(String key, ImageView v) {
		Bitmap bm = null;
		try {
			this.mapSema_.acquire();
			this.ageQueueSema_.acquire();
			
			refreshKeyIfExist(key);
			bm = this.map.get(key);
			if (bm != null) {
				this.viewMap.put(key, v);
			}
		
			this.mapSema_.release();
			this.ageQueueSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		return bm;
	}
	public void removeImage(final String key) {
			Handler mainHandler = new Handler(context_.getMainLooper());
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					try {
						mapSema_.acquire();
						ageQueueSema_.acquire();
						
						Bitmap bm = map.remove(key);
						ImageView v = viewMap.remove(key);
						if (v != null)
							v.setImageBitmap(null);
						if (bm != null)
							bm.recycle();
						for (int i = 0; i < ageQueue.size(); i++) {
							if (ageQueue.get(i).equalsIgnoreCase(key)) {
								ageQueue.remove(i);
								break;
							}
						}
						mapSema_.release();
						ageQueueSema_.release();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
			});
		
	}
	public boolean hasImage(String key) {
		boolean hasKey = false;
		try {
			this.mapSema_.acquire();
			this.ageQueueSema_.acquire();
			
			hasKey = refreshKeyIfExist(key);
			
			this.mapSema_.release();
			this.ageQueueSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		return hasKey;
	}
	private boolean refreshKeyIfExist(String key) {
		if (this.map.containsKey(key)) {
			for (int i = 0; i < this.ageQueue.size(); i++) {
				if (this.ageQueue.get(i).equalsIgnoreCase(key)) {
					this.ageQueue.remove(i);
					this.ageQueue.add(key);
					return true;
				}
			}
		}
		return false;
	}
	private void ejectOldestItem() {
		try {
			this.ageQueueSema_.acquire();
			if (this.ageQueue.size() > 0) {
				String oldKey = this.ageQueue.get(0);
				this.ageQueueSema_.release();
				removeImage(oldKey);
			} else {
				this.ageQueueSema_.release();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
}
