package com.ziegler.hereiam;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ziegler.hereiam.Models.Location;
import com.ziegler.hereiam.Models.Room;

import java.util.HashMap;
import java.util.Map;


public class FirebaseUtil {

    final String TAG = "FirebaseUtil";

    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }


    public static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("people").child(getCurrentUserId());
        }
        return null;
    }

    public static DatabaseReference getUserDocumentsRef() {
        return getBaseRef().child("documents_users");
    }

    public static String getUserDocumentsPath() {
        return "documents_users/";
    }

    public static DatabaseReference getListOfDocumentsRef() {
        return getBaseRef().child("documents");
    }

    public static DatabaseReference getPeopleRef() {
        return getBaseRef().child("people");
    }

    public static String getCurrentDisplayName() {
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    public static String getCurrentPhotoUrl() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
    }

    public static void createRoom(String roomName) {

        String owner = FirebaseUtil.getCurrentUserId();
        String picture = FirebaseUtil.getCurrentPhotoUrl();
        String name = FirebaseUtil.getCurrentDisplayName();

        String roomKey = FirebaseUtil.getBaseRef().child("rooms").push().getKey();
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("people/" + owner + "/rooms/" + roomKey + "/name", roomName);
        updateValues.put("rooms/" + roomKey + "/name", roomName);
        updateValues.put("rooms/" + roomKey + "/owner", owner);

        updateValues.put("rooms/" + roomKey + "/people/ " + owner + "/name", name);
        updateValues.put("rooms/" + roomKey + "/people/ " + owner + "/picture", picture);


        FirebaseUtil.getBaseRef().updateChildren(updateValues,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, final DatabaseReference databaseReference) {
                        if (firebaseError != null) {
                        } else {
                        }
                    }
                });
    }


    public static void joinRoom(final String user, final String roomKey) {
        FirebaseUtil.getBaseRef().child("rooms").child(roomKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Room room = dataSnapshot.getValue(Room.class);
                if (room == null) return;

                Map<String, Object> updateValues = new HashMap<>();
                updateValues.put("people/" + user + "/rooms/" + roomKey + "/name", room.getName());
                updateValues.put("rooms/" + roomKey + "/people/ " + user + "/name", FirebaseUtil.getCurrentDisplayName());
                updateValues.put("rooms/" + roomKey + "/people/ " + user + "/picture", FirebaseUtil.getCurrentPhotoUrl());


                FirebaseUtil.getBaseRef().updateChildren(updateValues,
                        new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, final DatabaseReference databaseReference) {
                                if (firebaseError != null) {
                                } else {
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void postLocation(android.location.Location location) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Location loc = new Location(location);

            loc.setPicture(FirebaseUtil.getCurrentPhotoUrl());
            loc.setName(FirebaseUtil.getCurrentDisplayName());

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("locations").child(FirebaseUtil.getCurrentUserId()).setValue(loc);
        }
    }
}
