package com.example.voiceorder.chatting;

import androidx.annotation.NonNull;

// 채팅 객체를 담을 클래스.
public class ChatMsgVO {
    private String content;
    private boolean isUser;

    public ChatMsgVO() {

    }

    public ChatMsgVO(boolean isUser, String content) {
        this.isUser = isUser;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    @NonNull
    @Override
    public String toString() {
        return "ChatMsgVO{" +
                "isUser='" + isUser + '\'' +
                ", content='" + content + '\'' +
                "}";
    }
}
