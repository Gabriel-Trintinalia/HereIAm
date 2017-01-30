package com.ziegler.hereiam;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.ziegler.hereiam.Models.RoomItemList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_FINE_LOCATION = 848;

    private FloatingActionButton mFab;

    private ShowcaseView showcaseView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<RoomItemListViewHolder> mAdapter;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        Query allPostsQuery = FirebaseUtil.getCurrentUserRef().child("rooms").orderByChild("sharing");
        mAdapter = getFirebaseRecyclerAdapter(allPostsQuery);
        mRecyclerView.setAdapter(mAdapter);

        if (Util.isFirstRun(this)) {
            mRecyclerView.setVisibility(View.GONE);
            showTutorial();
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent room = new Intent(MainActivity.this, NewMapActivity.class);
                startActivity(room);
            }
        });
    }
    private FirebaseRecyclerAdapter<RoomItemList, RoomItemListViewHolder> getFirebaseRecyclerAdapter
            (Query query) {
        return new FirebaseRecyclerAdapter<RoomItemList, RoomItemListViewHolder>(
                RoomItemList.class, R.layout.room_item_list, RoomItemListViewHolder.class, query) {
            @Override
            public void populateViewHolder(final RoomItemListViewHolder itemViewHolder,
                                           final RoomItemList room, final int position) {
                setupRoomItemList(itemViewHolder, room, position, getRef(position).getKey());
            }

            @Override
            public void onViewRecycled(RoomItemListViewHolder holder) {
                super.onViewRecycled(holder);
            }
        };
    }

    private void setupRoomItemList(final RoomItemListViewHolder roomItemListViewHolder, final RoomItemList roomItemList, final int position, final String roomKey) {
        roomItemListViewHolder.setName(roomItemList.getName());
        roomItemListViewHolder.setPicture(roomItemList.getPicture());

        if (roomItemList.isSharing()) {
            roomItemListViewHolder.setVisibilityStatusIcon(true);
            roomItemListViewHolder.setSubText("Active");
        } else {
            roomItemListViewHolder.setVisibilityStatusIcon(false);
            roomItemListViewHolder.setSubText("Invisible");
        }

        roomItemListViewHolder.setOnClickListener(new RoomItemListViewHolder.RoomItemClickListener() {
            @Override
            public void openRoom() {
                if (hasPermissions()) {
                    Intent room = new Intent(MainActivity.this, MapActivity.class);

                    room.putExtra(getString(R.string.EVENT_KEY), roomKey);
                    room.putExtra(getString(R.string.NAME_ROOM), roomItemList.getName());

                    startActivity(room);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private boolean hasPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_FINE_LOCATION);
            return false;
        }
        return true;
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

    private void showTutorial() {

        final ViewTarget target = new ViewTarget(R.id.fab, this);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (counter) {
                    case 0:
                        showcaseView.setShowcase(target, true);
                        showcaseView.setContentTitle("Create a map");
                        showcaseView.setContentText("Tap here to create and share a map");
                        showcaseView.setButtonText("Got it");
                        break;
                    case 2:
                        showcaseView.hide();
                        mRecyclerView.setVisibility(View.VISIBLE);
                        break;
                }
                counter++;


            }
        };

        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(Target.NONE)
                .setContentTitle("Welcome")
                .setContentText("Your list of maps will appear here")
                .setStyle(R.style.CustomShowcaseTheme2)
                .setOnClickListener(listener)
                .build();

        showcaseView.setButtonText("Next");
        showcaseView.setButtonPosition(lps);
        showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

            }

            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) {

            }

            @Override
            public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent login = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(login);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
