package com.ziegler.hereiam;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ziegler.hereiam.Models.Location;
import com.ziegler.hereiam.Models.Room;

import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private String roomKey;
    private String roomName;

    private GoogleMap mMap;
    private Toolbar toolbar;

    private HashMap<String, Marker> marker = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_map);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent room = new Intent(MapActivity.this, RoomDetailActivity.class);
                room.putExtra(getString(R.string.EVENT_KEY), roomKey);

                startActivity(room);
            }
        });


        Intent intent = getIntent();
        roomKey = intent.getStringExtra(getString(R.string.EVENT_KEY));
        roomName = intent.getStringExtra(getString(R.string.NAME_ROOM));


        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


        mapFragment.getMapAsync(MapActivity.this);
        toolbar.setTitle(roomName);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        //mMap.setMyLocationEnabled(true);
        LatLng loc = new LatLng(-37.837329, 144.986561);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

        FirebaseUtil.getBaseRef().child("rooms").child(roomKey).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void addPeople(Room room, String roomKey) {

        for (String key : room.getPeople().keySet()) {

            FirebaseUtil.getBaseRef().child("locations").child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String key = dataSnapshot.getKey();
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
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
