package com.kindredprints.android.sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class KURLPhoto extends KPhoto {
	private String origUrl;
	private String prevUrl;
	
	public KURLPhoto(String id, String origUrl) {
		init(id, origUrl, null);
	}
	public KURLPhoto(String id, String origUrl, String prevUrl) {
		init(id, origUrl, prevUrl);
	}
	
	private KURLPhoto(Parcel in) {
		this.id = in.readString();
		this.type = in.readString();
		this.origUrl = in.readString();
		this.prevUrl = in.readString();
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
