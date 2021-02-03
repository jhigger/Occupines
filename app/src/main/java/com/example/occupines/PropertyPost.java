package com.example.occupines;

import java.io.File;

public class PropertyPost {

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
}
