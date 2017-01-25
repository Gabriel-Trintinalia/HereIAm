package com.ziegler.hereiam.Models;

import java.util.Map;

/**
 * Created by Gabriel on 09/01/2017.
 */

public class Person {
    private String picture;
    private String name;

    private boolean sharing;


    private Map<String, Room> rooms;
    private Map<String, Object> myRooms;

    public Person() {
    }

    public String getPicture() {
        return picture;
    }

    public String getName() {
        return name;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Object> getMyRooms() {
        return myRooms;
    }


    public boolean isSharing() {
        return sharing;
    }
}
