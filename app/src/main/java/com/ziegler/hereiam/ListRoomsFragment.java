package com.ziegler.hereiam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ziegler.hereiam.Models.RoomItemList;


public class ListRoomsFragment extends Fragment {

    private static final String TAG = "GENERIC_FRAGMENT";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<RoomItemListViewHolder> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_rooms, container, false);
        rootView.setTag(TAG);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);


        Query allPostsQuery = FirebaseUtil.getBaseRef().child("people").child("teste").child("rooms");
        mAdapter = getFirebaseRecyclerAdapter(allPostsQuery);

        mRecyclerView.setAdapter(mAdapter);


        FirebaseUtil.getBaseRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Entrou");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                Intent room = new Intent(getActivity(), MapActivity.class);

                String roomKey = ((FirebaseRecyclerAdapter) mAdapter).getRef(position).getKey();
                room.putExtra(getString(R.string.EVENT_KEY), roomKey);

                startActivity(room);
            }
        });

    }

}