package com.kindred.kindredprints_android_sdk.helpers.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import com.kindred.kindredprints_android_sdk.data.Size;
import com.kindred.kindredprints_android_sdk.helpers.ImageEditor;
import com.kindred.kindredprints_android_sdk.helpers.prefs.DevPrefHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

public class FileCache {
	public static final String IMAGE_META_WIDTH = "image_meta_width";
	public static final String IMAGE_META_ORIENT = "image_meta_orient";
	public static final String IMAGE_META_HEIGHT = "image_meta_height";
	public static final String IMAGE_META_FSIZE = "image_meta_file_size";
	
	private static final String PIC_CACHE_DIR = "kp_pictures";
	private static final int MAX_PICTURES_CACHE = 80;
	private static final long UPDATE_INTERVAL = 2000;

	private Semaphore mapSema_;
	private Semaphore ageQueueSema_;
	
	private File selectedPath_;
	private HashMap<String, String> map_;
	private ArrayList<String> ageQueue_;
	private DevPrefHelper devPrefHelper_;
	private static FileCache cache_;
	
	private boolean needSave_;
	
	public FileCache(Context context) {
		this.selectedPath_ = context.getDir(PIC_CACHE_DIR, Context.MODE_PRIVATE);
		this.devPrefHelper_ = new DevPrefHelper(context);

		this.map_ = this.devPrefHelper_.getFileCacheKV();
		this.ageQueue_ = this.devPrefHelper_.getFileCacheAgeQueue();
		
		this.needSave_ = false;
		
		this.mapSema_ = new Semaphore(1);
		this.ageQueueSema_ = new Semaphore(1);
		
		Timer saveTimer = new Timer();
		saveTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				saveMetaData();
			}		
  		}, 0, UPDATE_INTERVAL);
	}
	
	public static FileCache getInstance(Context context) {
		if (cache_ == null) {
			cache_ = new FileCache(context);
		}
		return cache_;
	}
		
	public boolean storageAvailableForRead() {		
		return  Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
	}
	
	public boolean storageAvailableForWrite() {		
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	private boolean doesImageExistOnDisk(String key) {
		File f = new File(this.selectedPath_, getFileName(key));
		if (f.exists()) {
			return true;
		} else {
			try {
				this.mapSema_.acquire();
				if (this.map_.containsKey(key)) {
					this.mapSema_.release();
					cleanMapAndAgeQueue(key);
				} else {
					this.mapSema_.release();
				}
				return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	private void cleanMapAndAgeQueue(String key) {
		try {
			this.mapSema_.acquire();
			this.ageQueueSema_.acquire();
			this.map_.remove(key);
			for (int i = 0; i < this.ageQueue_.size(); i++) {
				if (this.ageQueue_.get(i).equalsIgnoreCase(key)) {
					this.ageQueue_.remove(i);
				}
			}
			this.mapSema_.release();
			this.ageQueueSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		needSave_ = true;
	}
	
	private String getFileName(String pid) {
		return pid + ".jpg";
	}
	
	public String getFullFilename(String key) {
		if (hasImageForKey(key)) {
			File f = new File(this.selectedPath_, this.map_.get(key));
			return f.getAbsolutePath();
		}
		return "";
	}
	
	public HashMap<String, Integer> getImageMetaDetails(String key) {
		HashMap<String, Integer> deets = new HashMap<String, Integer>();
		boolean hasImage = false;
		String fName = "";
		
		try {
			this.mapSema_.acquire();
			this.ageQueueSema_.acquire();
			hasImage = hasImageForKey(key);
			if (hasImage) {
				fName = this.map_.get(key);
			}
			this.mapSema_.release();
			this.ageQueueSema_.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (hasImage) {
			File f = new File(this.selectedPath_, fName);

			BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		    Log.i("KindredSDK", "getting meta deets w h fsize " + options.outWidth + " " + options.outHeight + " " + (int)f.length());
		    deets.put(IMAGE_META_FSIZE, (int)f.length());
		    
		    try {
				ExifInterface exif = new ExifInterface(f.getAbsolutePath());
				int orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
				deets.put(IMAGE_META_ORIENT, orient);
				Size actSize = ImageEditor.rotateSizeFromExifOrient(new Size(options.outWidth, options.outHeight),  orient);
				deets.put(IMAGE_META_WIDTH, (int)actSize.getWidth());
			    deets.put(IMAGE_META_HEIGHT, (int)actSize.getHeight());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return deets;
	}
	
	public boolean addImage(Bitmap image, String key) {
		boolean status = false;
		if (storageAvailableForWrite()) {
			if (this.map_.size() > MAX_PICTURES_CACHE)
				popOldestFile();
			
		    FileOutputStream fOut;
			try {
				fOut = new FileOutputStream(new File(this.selectedPath_, getFileName(key)));
			    image.compress(Bitmap.CompressFormat.JPEG, 95, fOut);
			    fOut.close();
			    try {
					this.mapSema_.acquire();
					this.ageQueueSema_.acquire();
					
				    this.map_.put(key, getFileName(key));
				    if (!refreshKeyIfExist(key))
						this.ageQueue_.add(key);
				    
				    this.mapSema_.release();
					this.ageQueueSema_.release();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
				needSave_ = true;
			    //saveMetaData();
			    status = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e)  {
				e.printStackTrace();
			}
		}
		return status;
	}
	
	public boolean addImageFromFile(String orgFn, String key) {
		boolean status = false;
		if (storageAvailableForWrite()) {
			if (this.map_.size() > MAX_PICTURES_CACHE)
				popOldestFile();
			
			if (copyFileFromFile(orgFn, getFileName(key))) {
				try {
					this.mapSema_.acquire();
					this.ageQueueSema_.acquire();
					
					this.map_.put(key, getFileName(key));
				    if (!refreshKeyIfExist(key))
						this.ageQueue_.add(key);
				    
				    this.mapSema_.release();
					this.ageQueueSema_.release();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
				needSave_ = true;
			    status = true;
			}
		}
		return status;
	}
	
	public boolean addImageFromUrl(String url, String key) {
		boolean status = false;
		if (storageAvailableForWrite()) {
			if (this.map_.size() > MAX_PICTURES_CACHE)
				popOldestFile();
			Log.i("KindredSDK", "about to copy " + key + " from " + url);
			if (copyFileFromServer(url, getFileName(key))) {
				try {
					this.mapSema_.acquire();
					this.ageQueueSema_.acquire();
					
					this.map_.put(key, getFileName(key));
				    if (!refreshKeyIfExist(key))
						this.ageQueue_.add(key);
				    
				    this.mapSema_.release();
					this.ageQueueSema_.release();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			    status = true;
			}
		}
		return status;
	}
	
	public Bitmap getImageForKey(String key, Size imSize) {
		try {
			this.mapSema_.acquire();
			this.ageQueueSema_.acquire();
			
			if (this.map_.containsKey(key) && refreshKeyIfExist(key) && storageAvailableForRead()) {
				if (doesImageExistOnDisk(key)) {
					
					File f = new File(this.selectedPath_, this.map_.get(key));
					
					this.mapSema_.release();
					this.ageQueueSema_.release();
					
					BitmapFactory.Options options = new BitmapFactory.Options();
				    options.inJustDecodeBounds = true;
				    BitmapFactory.decodeFile(f.getAbsolutePath(), options);
				    
				   	options.inSampleSize = ImageEditor.calculateInSampleSize(options, (int)imSize.getWidth(), (int)imSize.getHeight());
				   	options.inJustDecodeBounds = false;
				    Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
				    				    
				    int orient = 0;
				    try {
						ExifInterface exif = new ExifInterface(f.getAbsolutePath());
						orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
					} catch (IOException e) {
						e.printStackTrace();
					}
				    bm = ImageEditor.scale_and_rotate(bm, orient, imSize);

					needSave_ = true;
				    
				    return bm;
				}
			} else {
				this.mapSema_.release();
				this.ageQueueSema_.release();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public boolean deleteImageForKey(String key) {
		try {
			this.mapSema_.acquire();
			this.ageQueueSema_.acquire();
			if (this.map_.containsKey(key) && storageAvailableForWrite()) {
				File f = new File(this.selectedPath_, this.map_.get(key));
				this.mapSema_.release();
				this.ageQueueSema_.release();
				cleanMapAndAgeQueue(key);
				return f.delete();
			} else {
				this.mapSema_.release();
				this.ageQueueSema_.release();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		return true;
	}
	
	public boolean hasImageForKey(String key) {
		if (this.map_.containsKey(key) && refreshKeyIfExist(key)) {
			return doesImageExistOnDisk(key);
		}
		return false;
	}
	
	private void popOldestFile() {
		try {
			this.ageQueueSema_.acquire();
			if (this.ageQueue_.size() > 0) {
				String key = this.ageQueue_.get(0);
				this.ageQueueSema_.release();
				deleteImageForKey(key);
			} else {
				this.ageQueueSema_.release();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	private boolean refreshKeyIfExist(String key) {
		if (this.map_.containsKey(key)) {
			for (int i = 0; i < this.ageQueue_.size(); i++) {
				if (this.ageQueue_.get(i).equalsIgnoreCase(key)) {
					this.ageQueue_.remove(i);
					this.ageQueue_.add(key);
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean copyFileFromServer(String sourceUrl, String filename) {
		boolean success = true;
		File fo = new File(this.selectedPath_, filename);
		URL urlIn;
		try {
			urlIn = new URL(sourceUrl);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return success;
		}
		
		try {
			FileOutputStream fOut = new FileOutputStream(fo);
			InputStream fIn = urlIn.openConnection().getInputStream();
			
			byte[] buf = new byte[1024];
			int len;
			while ((len = fIn.read(buf)) > 0) {
				fOut.write(buf, 0, len);
			}
	           
			fOut.close();
			fIn.close();
		} catch (Exception e) {
			success = false;
			e.printStackTrace();
		} 
		return success;
	}
	
	private boolean copyFileFromFile(String sourcefn, String filename) {
		boolean success = true;
		
		File fo = new File(this.selectedPath_, filename);
		File fi = new File(sourcefn);
		try {
			FileOutputStream fOut = new FileOutputStream(fo);
			FileInputStream fIn = new FileInputStream(fi);
			
			byte[] buf = new byte[1024];
			int len;
			while ((len = fIn.read(buf)) > 0) {
				fOut.write(buf, 0, len);
			}
	            
			fOut.close();
			fIn.close();
		} catch (Exception e) {
			success = false;
			e.printStackTrace();
		}
		return success;
	}

	private void saveMetaData() {
		if (!needSave_) {
			try {
				this.mapSema_.acquire();
				this.ageQueueSema_.acquire();
				
				this.devPrefHelper_.setFileCacheKV(this.map_);
				this.devPrefHelper_.setFileCacheAgeQueue(this.ageQueue_);
				this.needSave_ = false;
				
				this.mapSema_.release();
				this.ageQueueSema_.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}
}
