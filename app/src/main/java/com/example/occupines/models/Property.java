package com.example.occupines.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Property implements Parcelable {

    private File imageFile1;
    private File imageFile2;
    private File imageFile3;
    private File imageFile4;
    private File imageFile5;

    public Property(File imageFile1, String type, double price, String location, String owner, String info, String id) {
        this.imageFile1 = imageFile1;
        this.type = type;
        this.price = price;
        this.location = location;
        this.owner = owner;
        this.info = info;
        this.id = id;
    }

    public File getImageFile2() {
        return imageFile2;
    }

    public void setImageFile2(File imageFile2) {
        this.imageFile2 = imageFile2;
    }

    public File getImageFile3() {
        return imageFile3;
    }

    public void setImageFile3(File imageFile3) {
        this.imageFile3 = imageFile3;
    }

    public File getImageFile4() {
        return imageFile4;
    }

    public void setImageFile4(File imageFile4) {
        this.imageFile4 = imageFile4;
    }

    public File getImageFile5() {
        return imageFile5;
    }

    private String type;
    private double price;
    private String location;
    private String owner;
    private String info;
    private String id;

    public static final Creator<Property> CREATOR = new Creator<Property>() {
        @Override
        public Property createFromParcel(Parcel in) {
            return new Property(in);
        }

        @Override
        public Property[] newArray(int size) {
            return new Property[size];
        }
    };

    public void setImageFile5(File imageFile5) {
        this.imageFile5 = imageFile5;
    }

    public Property(String type, double price, String location, String owner, String info, String id) {
        this.type = type;
        this.price = price;
        this.location = location;
        this.owner = owner;
        this.info = info;
        this.id = id;
    }

    protected Property(Parcel in) {
        type = in.readString();
        price = in.readDouble();
        location = in.readString();
        info = in.readString();
    }

    public File getImageFile1() {
        return imageFile1;
    }

    public void setImageFile1(File imageFile1) {
        this.imageFile1 = imageFile1;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
