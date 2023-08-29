package com.example.voiceorder;

import android.app.Application;

import com.example.voiceorder.chatting.ChatMsgVO;

import java.util.ArrayList;
import java.util.List;

/** CLass: Global Variables to be used throughout the APP **/
public class Public extends Application {
    public static boolean inStore = false;          // Whether User is inside the Store
    public static int session = 0;                  // Session Number used in Server
    public static boolean isRecording = false;      // Whether Recording or Not
    public static List<ChatMsgVO> msgList = new ArrayList<>();  // List variable containing Chat messages

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void addMessage(boolean isUser, String message) {
        message = message.replace(
                System.getProperty("line.separator").toString(), "");
        ChatMsgVO msgVO = new ChatMsgVO(isUser, message);
        msgList.add(msgVO);
    }
}