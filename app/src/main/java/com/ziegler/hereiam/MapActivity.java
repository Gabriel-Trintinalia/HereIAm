package com.ziegler.hereiam;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ziegler.hereiam.Models.Location;
import com.ziegler.hereiam.Models.Person;
import com.ziegler.hereiam.Models.Room;

import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MAPACTIVITY";
    private static final int MY_PERMISSIONS_FINE_LOCATION = 848;
    private String roomKey;
    private String roomName;

    private GoogleMap mMap;

    private Toolbar toolbar;

    private Menu menu;
    private Room room;
    private Person person;
    private ShowcaseView showcaseView;

    private static android.location.Location lastKnownlocation;
    private int counter = 0;

    private Switch mSwitch;

    private HashMap<String, Marker> marker = new HashMap<>();
    private HashMap<DatabaseReference, ValueEventListener> eventListenersList = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_map);

        setActionBar();

        Intent intent = getIntent();
        roomKey = intent.getStringExtra(getString(R.string.EVENT_KEY));
        roomName = intent.getStringExtra(getString(R.string.NAME_ROOM));
        toolbar.setTitle(roomName);

        // Starting Google Maps
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);

        setSwitchAction();
        addSharingLocationUser();
        showTutorial();

    }

    private void setActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDetailActivity();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_FINE_LOCATION);

            return;
        }

        mMap.setMyLocationEnabled(true);
        //LatLng loc = new LatLng(-37.837329, 144.986561);
        setCenterMap(mMap);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toolbar.setTitle(roomName);
                Room room = dataSnapshot.getValue(Room.class);

                removeListeners();
                addPeople(room, roomKey);
                addSharingLocationListenerForThisRoom();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        DatabaseReference ref = FirebaseUtil.getRoomsRef().child(roomKey);
        ref.addValueEventListener(listener);
        eventListenersList.put(ref, listener);


    }

    //
    // Add content in the map
    //
    private void addPeople(Room room, String roomKey) {

        for (final String key : room.getPeople().keySet()) {
            if (key.trim().equals(FirebaseUtil.getCurrentUserId())) continue;

            ValueEventListener listener = getNewEventListener(key);

            if (room.getPeople().get(key).isSharing()) {
                DatabaseReference ref = FirebaseUtil.getLocationsRef().child(key.trim());
                ref.addValueEventListener(listener);
                eventListenersList.put(ref, listener);
            } else {
                removeMarkerPerson(key);
            }
        }
    }

    private ValueEventListener getNewEventListener(final String key) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Location location = dataSnapshot.getValue(Location.class);
                if (location == null) {
                    removeMarkerPerson(key);
                    return;
                }


                if (marker.containsKey(key)) {
                    marker.get(key).setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                } else {
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                    BitmapDescriptor markerIcon;
                    Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_person_in_map);
                    markerIcon = Util.getMarkerIconFromDrawable(circleDrawable);

                    Marker m = mMap.addMarker(new MarkerOptions()
                            .title(location.getName())
                            .icon(markerIcon)
                            .anchor(0.5f, 0.5f)
                            .position(loc));

                    marker.put(key, m);
                    Util.setMarkerIcon(location.getPicture(), m, MapActivity.this);
                    //  calculateBoundary(mMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    private void setCenterMap(final GoogleMap map) {

        if (lastKnownlocation != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownlocation.getLatitude(), lastKnownlocation.getLongitude()), 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(lastKnownlocation.getLatitude(), lastKnownlocation.getLongitude()))      // Sets the center of the map to location user
                    .zoom(14)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            return;
        }

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        try {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    lastKnownlocation = location;
                    if (lastKnownlocation != null) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                .zoom(14)                   // Sets the zoom
                                .build();                   // Creates a CameraPosition from the builder

                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            }, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_menu, menu);
        this.menu = menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity
        }

        if (item.getItemId() == R.id.action_detail) {
            startDetailActivity();  // start activity to show details of the map
        }


        if (item.getItemId() == R.id.action_invite) {

            Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                    .setMessage(getString(R.string.invitation_message))
                    .setDeepLink(Uri.parse("https://hereiam-c7f82.firebaseio.com/rooms/?" + roomKey))
                    //  .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                    .setCallToActionText("Join")
                    .build();

            startActivityForResult(intent, 1);
            // start activity to show details of the map
        }

        if (item.getItemId() == R.id.action_exit_map) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Exit Map"); //Set Alert dialog title here

            alert.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    removeListeners();
                    FirebaseUtil.exitRoom(FirebaseUtil.getCurrentUserId(), roomKey);
                    finish();

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = alert.create();
            alertDialog.show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;
            }
        }
    }

    //
    // Show map details
    //
    private void startDetailActivity() {
        Intent room = new Intent(MapActivity.this, RoomDetailActivity.class);
        room.putExtra(getString(R.string.EVENT_KEY), roomKey);
        startActivity(room);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user still belongs to the map.
        checkRoomAvailable();
    }

    //
    // Validation to check if the user belongs to the map.
    //
    private void checkRoomAvailable() {

        FirebaseUtil.getCurrentUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Person person = dataSnapshot.getValue(Person.class);
                if ((person == null || (person.getRooms()) == null) || (!person.getRooms().containsKey(roomKey))) {
                    removeListeners();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onPause() {
        removeListeners();
        super.onPause();

    }

    private void removeListeners() {
        for (DatabaseReference ref : eventListenersList.keySet()) {
            ref.removeEventListener(eventListenersList.get(ref));
        }
    }

    private void removeMarkerPerson(String key) {
        if (marker.containsKey(key)) {
            marker.get(key).remove();
            marker.remove(key);
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void addSharingLocationListenerForThisRoom() {

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                room = dataSnapshot.getValue(Room.class);
                if (room.isSharing()) {
                    mSwitch.setChecked(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        DatabaseReference database = FirebaseUtil.getCurrentUserRef().child("rooms").child(roomKey);
        database.addValueEventListener(listener);
        eventListenersList.put(database, listener);
    }


    private void addSharingLocationUser() {

        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey() == "sharing")
                    if (person.isSharing()) ;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.getKey().equals("sharing")) {
                    Intent intent = new Intent(MapActivity.this, LocationManagerService.class);
                    if ((boolean) dataSnapshot.getValue()) {
                        startService(intent);
                    } else {
                        stopService(intent);

                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        DatabaseReference database = FirebaseUtil.getCurrentUserRef();
        database.addChildEventListener(listener);
        //  eventListenersList.put(database, listener);
    }

    public static void setLastKnownlocation(android.location.Location lastKnownlocation) {
        MapActivity.lastKnownlocation = lastKnownlocation;
    }


    private void setSwitchAction() {

        mSwitch = (Switch) findViewById(R.id.switch_share_location);

        mSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);

                String buttonPositive;
                if (mSwitch.isChecked()) {
                    alert.setTitle("Share location"); //Set Alert dialog title here
                    alert.setMessage("Do you want to share your location with the users in this room?");
                    buttonPositive = "Share";
                } else {

                    alert.setTitle("Stop sharing"); //Set Alert dialog title here
                    alert.setMessage("Do you want to stop sharing?");
                    buttonPositive = "Stop";
                }

                alert.setPositiveButton(buttonPositive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        FirebaseUtil.setSharingRoom(FirebaseUtil.getCurrentUserId(), roomKey, mSwitch.isChecked());
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mSwitch.setChecked(!mSwitch.isChecked());
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        });


    }

    /*private void calculateBoundary(GoogleMap map) {


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (String k : marker.keySet()) {
            builder.include(marker.get(k).getPosition());
        }

        builder.include(map.getCameraPosition().target);
        // Creates a CameraPosition from the builder

        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

    }*/


    private void showTutorial() {

        final ViewTarget targetMap = new ViewTarget(R.id.map, this);
        final ViewTarget targetActiveSharing = new ViewTarget(R.id.switch_share_location, this);
        final ToolbarActionItemTarget targetInvitePeople = new ToolbarActionItemTarget(toolbar, R.id.action_invite);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (counter) {
                    case 0:
                        showcaseView.setShowcase(targetActiveSharing, true);
                        showcaseView.setContentTitle("Share your position");
                        showcaseView.setContentText("Tap here to activate or deactivate the location sharing");
                        showcaseView.setButtonText("Next");
                        break;

                    case 1:
                        showcaseView.setShowcase(targetInvitePeople, true);
                        showcaseView.setContentTitle("Invite people");
                        showcaseView.setContentText("Share the map with your friends and see their location");
                        showcaseView.setButtonText("Got it");
                        break;
                    case 2:
                        showcaseView.hide();
                        break;
                }
                counter++;
            }
        };

        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(targetMap)
                .withHoloShowcase()
                .setContentTitle("Map")
                .setContentText("Your friends will appear here.")
                .setStyle(R.style.CustomShowcaseTheme3)
                .setOnClickListener(listener)
                .build();

        showcaseView.setButtonText("Next");
    }

    public class ToolbarActionItemTarget implements Target {

        private final Toolbar toolbar;
        private final int menuItemId;

        public ToolbarActionItemTarget(Toolbar toolbar, @IdRes int itemId) {
            this.toolbar = toolbar;
            this.menuItemId = itemId;
        }

        @Override
        public Point getPoint() {
            return new ViewTarget(toolbar.findViewById(menuItemId)).getPoint();
        }

    }

}


