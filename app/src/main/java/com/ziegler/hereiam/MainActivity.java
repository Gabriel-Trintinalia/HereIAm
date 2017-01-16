package com.ziegler.hereiam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.ziegler.hereiam.Models.RoomItemList;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FloatingActionButton mFab;
    final Context context = this;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<RoomItemListViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String userID = FirebaseUtil.getCurrentUserId();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        Query allPostsQuery = FirebaseUtil.getBaseRef().child("people").child(userID).child("rooms");
        mAdapter = getFirebaseRecyclerAdapter(allPostsQuery);

        mRecyclerView.setAdapter(mAdapter);
        // adapter.addFragment(new EarningsFragment(), "EARNINGS");


        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent room = new Intent(MainActivity.this, NewMapActivity.class);
                startActivity(room);


            }
        });

        startService(new Intent(this, LocationManager.class));
    }


    private FirebaseRecyclerAdapter<RoomItemList, RoomItemListViewHolder> getFirebaseRecyclerAdapter(Query query) {
        return new FirebaseRecyclerAdapter<RoomItemList, RoomItemListViewHolder>(
                RoomItemList.class, R.layout.room_item_list, RoomItemListViewHolder.class, query) {
            @Override
            public void populateViewHolder(final RoomItemListViewHolder itemViewHolder,
                                           final RoomItemList room, final int position) {
                setupRoomItemList(itemViewHolder, room, position, null);
            }

            @Override
            public void onViewRecycled(RoomItemListViewHolder holder) {
                super.onViewRecycled(holder);
//                FirebaseUtil.getLikesRef().child(holder.mPostKey).removeEventListener(holder.mLikeListener);
            }
        };
    }


    private void setupRoomItemList(final RoomItemListViewHolder roomItemListViewHolder, final RoomItemList roomItemList, final int position, final String roomKey) {
        roomItemListViewHolder.setName(roomItemList.getName());

        roomItemListViewHolder.setOnClickListener(new RoomItemListViewHolder.RoomItemClickListener() {
            @Override
            public void openRoom() {
                Intent room = new Intent(MainActivity.this, MapActivity.class);

                String roomKey = ((FirebaseRecyclerAdapter) mAdapter).getRef(position).getKey();
                room.putExtra(getString(R.string.EVENT_KEY), roomKey);
                room.putExtra(getString(R.string.NAME_ROOM), roomItemList.getName());

                // shareRoom(roomKey);
                startActivity(room);
            }
        });
    }

    private void shareRoom(String keyRoom) {

        String dld = getString(R.string.DynamicLinkDomain);
        String link = "https://james-1b462.firebaseapp.com/room/?cod=";

        String pck = "&apn=com.ziegler.hereiam";
        String url = null;
        URL urla = null;
        try {
            url = dld + "?link=" + link + keyRoom.replace("-", "%2D") + pck;

            URI uri = new URI(url);
            urla = uri.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, urla.toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share with..."));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == R.id.action_sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent login = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(login);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }


}
