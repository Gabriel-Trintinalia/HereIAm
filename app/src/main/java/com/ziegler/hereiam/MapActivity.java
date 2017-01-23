package com.ziegler.hereiam;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
        }

        mMap.setMyLocationEnabled(true);

        //LatLng loc = new LatLng(-37.837329, 144.986561);
        setCenterMap(mMap);

        FirebaseUtil.getRoomsRef().child(roomKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toolbar.setTitle(roomName);
                Room room = dataSnapshot.getValue(Room.class);

                removeListeners();
                addPeople(room, roomKey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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
                    setIconPicture(location.getPicture(), m);
                    //  calculateBoundary(mMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    private void setCenterMap(GoogleMap map) {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        android.location.Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(14)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void calculateBoundary(GoogleMap map) {


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (String k : marker.keySet()) {
            builder.include(marker.get(k).getPosition());
        }

        builder.include(map.getCameraPosition().target);
        // Creates a CameraPosition from the builder

        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

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

        if (item.getItemId() == R.id.action_share_location_map) {

            FirebaseUtil.setSharingRoom(FirebaseUtil.getCurrentUserId(), roomKey, true);
            // start activity to show details of the map
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
                if ((person == null || (person.getRooms()) == null) || (!person.getRooms().containsKey(roomKey)))
                    finish();

/*                if (person.getRooms().get(roomKey).isSharing()) {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_map_sharing));
                } else {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_map));
                }*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void setIconPicture(String src, final Marker marker) {

        Glide.with(this)
                .load(src)
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(Util.getCroppedBitmap(resource)));
                        return false;
                    }
                })
                .into(50, 50);
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
}
