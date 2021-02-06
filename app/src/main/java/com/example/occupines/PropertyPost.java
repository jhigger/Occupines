package com.example.occupines;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class PropertyPost implements Parcelable {

    private File localFile;
    private String type;
    private double price;
    private String location;
    private String owner;
    private String info;

    public PropertyPost(File localFile, String type, double price, String location, String owner, String info) {
        this.localFile = localFile;
        this.type = type;
        this.price = price;
        this.location = location;
        this.owner = owner;
        this.info = info;
    }

    public static final Creator<PropertyPost> CREATOR = new Creator<PropertyPost>() {
        @Override
        public PropertyPost createFromParcel(Parcel in) {
            return new PropertyPost(in);
        }

        @Override
        public PropertyPost[] newArray(int size) {
            return new PropertyPost[size];
        }
    };

    protected PropertyPost(Parcel in) {
        type = in.readString();
        price = in.readDouble();
        location = in.readString();
        info = in.readString();
    }

    public File getLocalFile() {
        return localFile;
    }

    public void setLocalFile(File localFile) {
        this.localFile = localFile;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeDouble(price);
        dest.writeString(location);
        dest.writeString(info);
    }
}
