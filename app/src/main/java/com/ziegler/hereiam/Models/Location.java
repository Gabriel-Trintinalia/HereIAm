package com.ziegler.hereiam.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
 * Created by Gabriel on 09/01/2017.
 */

public class Location {

    private String picture;
    private String name;
    private float bearing;
    private Double latitude;

    private Double longitude;

    private Map time;

    public Location() {
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }


    public Map getTime() {
        return time;
    }

    public Location(android.location.Location loc) {
        if (loc != null) {
            //   this.bearing = loc.getBearing();
            this.latitude = loc.getLatitude();
            this.longitude = loc.getLongitude();
            this.time = ServerValue.TIMESTAMP;
        }
    }


    @Exclude
    public long getTimestampCreatedLong() {
        return (long) time.get("timestamp");
    }

}
