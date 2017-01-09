package com.ziegler.hereiam;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Gabriel on 09/01/2017.
 */

public class RoomItemListViewHolder extends RecyclerView.ViewHolder {

    private RoomItemClickListener mListener;
    private TextView mRoomNameTextView;
    private RelativeLayout button;

    public RoomItemListViewHolder(View itemView) {
        super(itemView);

        mRoomNameTextView = (TextView) itemView.findViewById(R.id.main_text);
    }

    public void setName(String name) {
        mRoomNameTextView.setText(name);
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
