package com.ziegler.hereiam.Models;

import java.util.Map;

/**
 * Created by Gabriel on 09/01/2017.
 */

public class Person {
    private String picture;
    private String name;

    private Map<String, Object> rooms;
    private Map<String, Object> myRooms;

    public Person() {
    }

    public String getPicture() {
        return picture;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getRooms() {
        return rooms;
    }

    public Map<String, Object> getMyRooms() {
        return myRooms;
    }
}
