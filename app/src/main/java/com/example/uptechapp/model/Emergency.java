package com.example.uptechapp.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.SimpleTimeZone;

public class Emergency {
    private String id;
    private String email;
    private String photoUrl;
    private String title;
    private String description;
    private String time;
    private double latitude;
    private double longitude;
    private LatLng location;


    public Emergency(String id, String email, String title, String description, String time, String photoUrl, double latitude, double longitude) {
        this.id = id;
        this.email = email;
        this.title = title;
        this.description = description;
        this.time = time;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = new LatLng(this.latitude, this.longitude);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public LatLng getLocation() {
        return location;
    }

    public Double getLattitude(){
        return latitude;
    }

    public Double getLongitude(){
        return longitude;
    }

    public void setLocation(double latitude, double longitude) {
        this.location = new LatLng(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Emergency{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", time='" + time + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", location=" + location +
                '}';
    }
}
