package com.ziegler.hereiam;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.ziegler.hereiam.Models.RoomItemList;


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

                    /* Alert Dialog Code Start*/
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Create Map"); //Set Alert dialog title here

                // Set an EditText view to get user input
                final EditText input = new EditText(context);
                input.setHint("Map Name");
                alert.setView(input);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String srt = input.getEditableText().toString();
                        FirebaseUtil.createRoom(userID, srt);
                    } // End of onClick(DialogInterface dialog, int whichButton)
                }); //End of alert.setPositiveButton

                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        dialog.cancel();
                    }
                }); //End of alert.setNegativeButton
                AlertDialog alertDialog = alert.create();
                alertDialog.show();

            }
        });

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

                startActivity(room);
            }
        });

    }
}
