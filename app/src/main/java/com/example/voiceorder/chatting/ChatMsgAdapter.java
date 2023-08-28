package com.example.voiceorder.chatting;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

import com.example.voiceorder.R;

// 각 채팅 하나하나마다 붙어있는 Adapter.
public class ChatMsgAdapter extends RecyclerView.Adapter<ChatMsgAdapter.ViewHolder> {
    private final List<ChatMsgVO> mValues;  // 채팅 메세지들을 담는 List 변수.

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
        ChatMsgVO vo = mValues.get(position);   // 채팅 객체를 하나씩 받아와 아래의 내용을 수행.

        if (!vo.getContent().equals("")) {      // 채팅 내용이 있는 경우 => 내용 출력.
            if (mValues.get(position).isUser()) {  // User 가 보낸 채팅
                holder.other_cl.setVisibility(View.GONE);
                holder.my_cl.setVisibility(View.VISIBLE);

                holder.content_tv2.setText(vo.getContent());
            } else {                                                        // 상대방이 보낸 채팅
                holder.other_cl.setVisibility(View.VISIBLE);
                holder.my_cl.setVisibility(View.GONE);

                holder.content_tv.setText(vo.getContent());
            }
        } else {                                // 채팅 내용이 없는 경우 => 아무것도 출력하지 않는다.
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
        public TextView userid_tv, date_tv, content_tv, date_tv2, content_tv2;

        public ViewHolder(View view) {
            super(view);
            // XML component 와 변수 연결
            my_cl = view.findViewById(R.id.my_cl);
            other_cl = view.findViewById(R.id.other_cl);
            content_tv = view.findViewById(R.id.content_tv);
            content_tv2 = view.findViewById(R.id.content_tv2);
        }
    }
}
