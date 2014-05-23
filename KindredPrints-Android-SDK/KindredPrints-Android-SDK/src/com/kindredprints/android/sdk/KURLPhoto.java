package com.kindredprints.android.sdk;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class KURLPhoto extends KPhoto {
	private String origUrl;
	private String prevUrl;
	private Bitmap prevThumb;
	
	public KURLPhoto(String origUrl) {
		init(origUrl, origUrl, origUrl, null);
	}
	
	public KURLPhoto(String origUrl, String prevUrl) {
		init(origUrl, origUrl, prevUrl, null);
	}
	public KURLPhoto(String origUrl, Bitmap prevThumb) {
		init(origUrl, origUrl, origUrl, prevThumb);
	}
	
	
	private KURLPhoto(Parcel in) {
		this.id = in.readString();
		this.type = in.readString();
		this.origUrl = in.readString();
		this.prevUrl = in.readString();
		this.prevThumb = in.readParcelable(getClass().getClassLoader());
	}
	
	private void init(String id, String orig, String preview, Bitmap bm) {
		this.id = id;
		this.type = TYPE_URL;
		this.origUrl = orig;
		this.prevUrl = preview;
		this.prevThumb = bm;
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
	
	public Bitmap getPrevThumb() {
		return prevThumb;
	}

	public void setPrevThumb(Bitmap prevThumb) {
		this.prevThumb = prevThumb;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(this.id);
		out.writeString(this.type);
		out.writeString(this.origUrl);
		out.writeString(this.prevUrl);
		out.writeParcelable(this.prevThumb, flags);
	}
	
	public static final Parcelable.Creator<KURLPhoto> CREATOR = new Parcelable.Creator<KURLPhoto>() {
        public KURLPhoto createFromParcel(Parcel in) {
            return new KURLPhoto(in);
        }

        public KURLPhoto[] newArray(int size) {
            return new KURLPhoto[size];
        }
    };
}
