package com.ziegler.hereiam;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ziegler.hereiam.Models.Room;
import com.ziegler.hereiam.Models.RoomItemList;


public class RoomDetailActivity extends AppCompatActivity {
    private static final String TAG = "ROOMDETAILACTIVITY";
    private static final int REQUEST_INVITE = 987;


    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<RoomItemListViewHolder> mAdapter;
    private String roomKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        Intent intent = getIntent();
        roomKey = intent.getStringExtra(getString(R.string.EVENT_KEY));

        setRecycler();
        setActionBar();
        setComponents();
    }

    private void setComponents() {

        LinearLayout buttonAddPeople = (LinearLayout) findViewById(R.id.button_add_people);
        LinearLayout buttonExitMap = (LinearLayout) findViewById(R.id.button_exit_map);

        buttonAddPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setMessage(getString(R.string.invitation_message))
                        .setDeepLink(Uri.parse("https://hereiam-c7f82.firebaseio.com/rooms/?" + roomKey))
                        //  .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                        .setCallToActionText("Join")
                        .build();
                startActivityForResult(intent, REQUEST_INVITE);
            }
        });

        FirebaseUtil.getRoomsRef().child(roomKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Room room = dataSnapshot.getValue(Room.class);
                ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
                Glide.with(RoomDetailActivity.this)
                        .load(room.getPicture())
                        .placeholder(R.drawable.ic_map)
                        .dontAnimate()
                        .fitCenter()
                        .into(backdrop);

                toolbar.setTitle(room.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        buttonExitMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                       /* Alert Dialog Code Start*/
                AlertDialog.Builder alert = new AlertDialog.Builder(RoomDetailActivity.this);
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
        });

    }


    private void setActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.room_detail_recycler_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        Query allPostsQuery = FirebaseUtil.getRoomsRef().child(roomKey).child("people");
        mAdapter = getFirebaseRecyclerAdapter(allPostsQuery);
        mRecyclerView.setAdapter(mAdapter);
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
        roomItemListViewHolder.setPicture(roomItemList.getPicture());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }
}
