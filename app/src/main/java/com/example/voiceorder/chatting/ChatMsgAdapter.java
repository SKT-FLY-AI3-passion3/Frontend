package com.example.voiceorder.chatting;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.voiceorder.R;

/** Class: Adapter attached to each Chat **/
public class ChatMsgAdapter extends RecyclerView.Adapter<ChatMsgAdapter.ViewHolder> {
    private final List<ChatMsgVO> mValues;  // List variable containing Chat messages

    public ChatMsgAdapter(List<ChatMsgVO> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_chat_msg, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Get Chat Objects one by one and do the following.
        Log.d("dfafsdf", mValues.toString());
        Log.d("dfafsdf", Integer.toString(mValues.size()));
        ChatMsgVO vo = mValues.get(position);

        if (!vo.getContent().equals("")) {                  // Chat Exists => Print Content
            if (mValues.get(position).isUser()) {           // Sent by User
                holder.other_cl.setVisibility(View.GONE);
                holder.my_cl.setVisibility(View.VISIBLE);

                holder.content_tv2.setText(vo.getContent());
            } else {                                         // Sent by Chatbot
                holder.other_cl.setVisibility(View.VISIBLE);
                holder.my_cl.setVisibility(View.GONE);

                holder.content_tv.setText(vo.getContent());
            }
        } else {                                            // No Chat Exists => Print Nothing
            holder.other_cl.setVisibility(View.GONE);
            holder.my_cl.setVisibility(View.GONE);

            holder.content_tv.setVisibility(View.GONE);
            holder.content_tv2.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Components
        public ConstraintLayout my_cl, other_cl;
        public TextView content_tv, content_tv2;

        public ViewHolder(View view) {
            super(view);
            // Connect XML component and Variables
            my_cl = view.findViewById(R.id.my_cl);
            other_cl = view.findViewById(R.id.other_cl);
            content_tv = view.findViewById(R.id.content_tv);
            content_tv2 = view.findViewById(R.id.content_tv2);
        }
    }
}
