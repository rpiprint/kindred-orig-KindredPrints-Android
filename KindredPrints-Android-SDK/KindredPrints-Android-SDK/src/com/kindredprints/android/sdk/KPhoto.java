package com.kindredprints.android.sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class KPhoto implements Parcelable {
	protected static final String TYPE_URL = "kphoto_url";
	protected static final String TYPE_LOCAL_FILE = "kphoto_file";
	protected static final String TYPE_LOCAL_MEM = "kphoto_mem";
	protected String id;
	protected String type;
	
	public KPhoto() {
		this.id = "";
		this.type = "";
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	private KPhoto(Parcel in) {
		this.id = in.readString();
		this.type = in.readString();
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		
	}	

	public static final Parcelable.Creator<KPhoto> CREATOR = new Parcelable.Creator<KPhoto>() {
        public KPhoto createFromParcel(Parcel in) {
            return new KPhoto(in);
        }

        public KPhoto[] newArray(int size) {
            return new KPhoto[size];
        }
    };
}
