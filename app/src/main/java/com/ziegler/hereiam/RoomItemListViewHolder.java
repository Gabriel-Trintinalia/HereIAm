package com.ziegler.hereiam;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gabriel on 09/01/2017.
 */

public class RoomItemListViewHolder extends RecyclerView.ViewHolder {

    private RoomItemClickListener mListener;
    private TextView mRoomNameTextView;
    private CircleImageView mImageProfile;

    private RelativeLayout button;

    public RoomItemListViewHolder(View itemView) {
        super(itemView);

        mRoomNameTextView = (TextView) itemView.findViewById(R.id.main_text);
        mImageProfile = (CircleImageView) itemView.findViewById(R.id.profile_icon);

    }

    public void setName(String name) {
        mRoomNameTextView.setText(name);
    }

    public void setPicture(String url) {

        Context context = mImageProfile.getContext();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_map)
                .dontAnimate()
                .fitCenter()
                .into(mImageProfile);
    }


    public void setOnClickListener(RoomItemClickListener listener) {
        mListener = listener;

        button = (RelativeLayout) itemView.findViewById(R.id.layout_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.openRoom();
            }
        });

    }

    public interface RoomItemClickListener {
        void openRoom();
    }

}
