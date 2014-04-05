package com.kindred.kindredprints_android_sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class KLOCPhoto extends KPhoto {
	private String filename;
	
	public KLOCPhoto(String id, String filename) {
		this.id = id;
		this.type = TYPE_LOCAL_FILE;
		this.filename = filename;
	}

	private KLOCPhoto(Parcel in) {
		this.id = in.readString();
		this.type = in.readString();
		this.filename = in.readString();
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(this.id);
		out.writeString(this.type);
		out.writeString(this.filename);
	}
	
	public static final Parcelable.Creator<KLOCPhoto> CREATOR = new Parcelable.Creator<KLOCPhoto>() {
        public KLOCPhoto createFromParcel(Parcel in) {
            return new KLOCPhoto(in);
        }

        public KLOCPhoto[] newArray(int size) {
            return new KLOCPhoto[size];
        }
    };
}
