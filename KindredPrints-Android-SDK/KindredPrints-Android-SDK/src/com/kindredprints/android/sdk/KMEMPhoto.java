package com.kindredprints.android.sdk;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class KMEMPhoto extends KPhoto {
	private Bitmap bm;
	
	public KMEMPhoto (Bitmap bm) {
		this.type = TYPE_LOCAL_MEM;
		this.id = String.valueOf(bm.hashCode());
		this.bm = bm;
	}

	private KMEMPhoto(Parcel in) {
		this.id = in.readString();
		this.type = in.readString();
		this.bm = (Bitmap) in.readParcelable(getClass().getClassLoader());
	}
	
	public Bitmap getBm() {
		return bm;
	}

	public void setBm(Bitmap bm) {
		this.bm = bm;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(this.id);
		out.writeString(this.type);
		out.writeParcelable(this.bm, flags);
	}
	
	public static final Parcelable.Creator<KMEMPhoto> CREATOR = new Parcelable.Creator<KMEMPhoto>() {
        public KMEMPhoto createFromParcel(Parcel in) {
            return new KMEMPhoto(in);
        }

        public KMEMPhoto[] newArray(int size) {
            return new KMEMPhoto[size];
        }
    };
}
