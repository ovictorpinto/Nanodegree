package com.github.ovictorpinto.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable {
    
    protected String name;
    protected String key;
    
    public Video() {
    }
    
    protected Video(Parcel in) {
        name = in.readString();
        key = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(key);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }
        
        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
}
