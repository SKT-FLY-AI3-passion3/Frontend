package com.example.voiceorder.chatting;

import androidx.annotation.NonNull;

/** Class: Contains Chat Objects **/
public class ChatMsgVO {
    private String content;
    private boolean isUser;

    /** Constructor **/
    public ChatMsgVO() { }

    public ChatMsgVO(boolean isUser, String content) {
        this.isUser = isUser;
        this.content = content;
    }

    /** Getter & Setter **/
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

    /** Print Variables in String **/
    @NonNull
    @Override
    public String toString() {
        return "ChatMsgVO{" +
                "isUser='" + isUser + '\'' +
                ", content='" + content + '\'' +
                "}";
    }
}
