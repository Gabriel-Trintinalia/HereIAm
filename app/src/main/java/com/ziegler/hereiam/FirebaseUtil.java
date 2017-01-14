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


    public static DatabaseReference getPostsRef() {
        return getBaseRef().child("posts");
    }


    public static DatabaseReference getEventsRef() {
        return getBaseRef().child("events");
    }

    public static DatabaseReference getShiftOpen() {
        return getBaseRef().child("shift_open");
    }

    public static DatabaseReference getShiftsRef() {
        return getBaseRef().child("shifts");
    }

    public static DatabaseReference getStatementRef() {
        return getBaseRef().child("payments");
    }

    public static DatabaseReference getPaymentsStatementsRef() {
        return getBaseRef().child("payment_statements");
    }

    public static DatabaseReference getPaymentsStatementsRef(String user) {
        return getBaseRef().child("payment_statements").child(user);
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


    public static DatabaseReference getListOfVideosRef() {
        return getBaseRef().child("videos");
    }

    public static DatabaseReference getListOfWatchedVideosRef() {
        return getBaseRef().child("videos_watched");
    }


    public static String getPaymentsStatementsPath() {
        return "payment_statements/";
    }

    public static String getPaymentsStatementsPath(String user) {
        return "payment_statements/" + user + "/";
    }


    public static String getPostsPath() {
        return "posts/";
    }

    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
    }

    public static String getPeoplePath() {
        return "people/";
    }


    public static DatabaseReference getPeopleRef() {
        return getBaseRef().child("people");
    }

    public static DatabaseReference getCommentsRef() {
        return getBaseRef().child("comments");
    }

    public static DatabaseReference getFeedRef() {
        return getBaseRef().child("feed");
    }

    public static DatabaseReference getLikesRef() {
        return getBaseRef().child("likes");
    }

    public static DatabaseReference getFollowersRef() {
        return getBaseRef().child("followers");
    }

    public static String getCurrentDisplayName() {
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    public static String getCurrentPhotoUrl() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;

    }

    /*public static void deleteOfferJob(String userKey, String eventKey) {

        if (userKey == "") return;
        if (userKey.equals(null)) return;

        if (eventKey == "") return;
        if (eventKey.equals(null)) return;


        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("people/" + userKey + "/offers/" + eventKey, null);
        updateValues.put("events/" + eventKey + "/offers/" + userKey, null);

        getBaseRef().updateChildren(
                updateValues,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, final DatabaseReference databaseReference) {
                        Log.d("TAG", databaseReference.toString());

                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Had a snack at Snackbar", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.RED);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.DKGRAY);
                        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.YELLOW);
                        snackbar.show();
                    }
                }

        );
    }*/


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
                updateValues.put("rooms/" + roomKey + "/people/ " + user, true);
                updateValues.put("rooms/" + roomKey + "/people/ " + user, true);


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
