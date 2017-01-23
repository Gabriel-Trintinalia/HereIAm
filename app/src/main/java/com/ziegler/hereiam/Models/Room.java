package com.ziegler.hereiam.Models;

import java.util.Map;

/**
 * Created by Gabriel on 09/01/2017.
 */

public class Room {
    private String name;
    private String picture;
    private boolean sharing;


    private String owner;
    private Map<String, MemberMap> people;

    public Room() {
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public Map<String, MemberMap> getPeople() {
        return people;
    }

    public String getPicture() {
        return picture;
    }

    public boolean isSharing() {
        return sharing;
    }
}
