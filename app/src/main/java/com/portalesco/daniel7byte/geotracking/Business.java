package com.portalesco.daniel7byte.geotracking;

public class Business {

    String business, notes, image, lat, lon;

    public Business(String business, String notes, String image) {
        this.business = business;
        this.notes = notes;
        this.image = image;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getBusiness() {
        return business;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getImage() {
        return image;
    }

    public String getNotes() {
        return notes;
    }
}
