package com.ziegler.hereiam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ziegler.hereiam.Models.Location;
import com.ziegler.hereiam.Models.Person;
import com.ziegler.hereiam.Models.Room;

import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_FINE_LOCATION = 848;
    private String roomKey;
    private String roomName;

    private GoogleMap mMap;
    private Toolbar toolbar;

    private HashMap<String, Marker> marker = new HashMap<>();

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

        FirebaseUtil.getRoomsRef().child(roomKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toolbar.setTitle(roomName);
                Room room = dataSnapshot.getValue(Room.class);
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
            FirebaseUtil.getLocationsRef().child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Location location = dataSnapshot.getValue(Location.class);

                    if (location == null) return;

                    if (marker.containsKey(key)) {
                        marker.get(key).setPosition(new LatLng(location.getLatitude(), location.getLongitude()));

                    } else {
                        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                        Marker m = mMap.addMarker(new MarkerOptions()
                                .title(location.getName())
                                .position(loc));
                        marker.put(dataSnapshot.getKey(), m);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_menu, menu);
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
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .build();                   // Creates a CameraPosition from the builder

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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

        FirebaseUtil.getCurrentUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Person person = dataSnapshot.getValue(Person.class);
                if ((person == null || (person.getRooms()) == null) || (!person.getRooms().containsKey(roomKey)))
                    finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
