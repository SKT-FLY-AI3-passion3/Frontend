package com.example.voiceorder;

import android.app.Application;
import android.util.Log;

import com.example.voiceorder.chatting.ChatMsgVO;

import java.util.ArrayList;
import java.util.List;

// 앱 전체에서 사용될 전역 변수들.
public class Public extends Application {
    public static boolean inStore = false;
    public static List<ChatMsgVO> msgList = new ArrayList<>();
    public static int session = 0;
    public static boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void addMessage(boolean isUser, String message) {
        Log.d("DFAfs", "2-1");
        message = message.replace(
                System.getProperty("line.separator").toString(), "");
        Log.d("DFAfs", "2-2");
        ChatMsgVO msgVO = new ChatMsgVO(isUser, message);
        Log.d("DFAfs", "2-3");
        msgList.add(msgVO);
        Log.d("DFAfs", "2-4");
    }
}