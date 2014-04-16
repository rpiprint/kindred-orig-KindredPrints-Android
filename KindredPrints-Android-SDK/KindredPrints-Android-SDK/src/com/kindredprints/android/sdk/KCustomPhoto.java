package com.kindredprints.android.sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class KCustomPhoto extends KPhoto {
	private String customType;
	private String associatedData;
	
	public KCustomPhoto(String customType, String data) {
		this.id = data;
		this.type = TYPE_CUSTOM;
		this.customType = customType;
		this.associatedData = data;
	}
	
	public KCustomPhoto(String id, String customType, String data) {
		this.id = id;
		this.type = TYPE_CUSTOM;
		this.customType = customType;
		this.associatedData = data;
	}
	
	private KCustomPhoto(Parcel in) {
		this.id = in.readString();
		this.type = in.readString();
		this.customType = in.readString();
		this.associatedData = in.readString();
	}

	public String getCustomType() {
		return customType;
	}

	public void setCustomType(String customType) {
		this.customType = customType;
	}

	public String getAssociatedData() {
		return associatedData;
	}

	public void setAssociatedData(String associatedData) {
		this.associatedData = associatedData;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(this.id);
		out.writeString(this.type);
		out.writeString(this.customType);
		out.writeString(this.associatedData);
	}
	
	public static final Parcelable.Creator<KCustomPhoto> CREATOR = new Parcelable.Creator<KCustomPhoto>() {
        public KCustomPhoto createFromParcel(Parcel in) {
            return new KCustomPhoto(in);
        }

        public KCustomPhoto[] newArray(int size) {
            return new KCustomPhoto[size];
        }
    };
}
