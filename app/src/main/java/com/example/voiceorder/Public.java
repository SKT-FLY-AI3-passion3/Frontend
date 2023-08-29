package com.example.voiceorder;

import android.app.Application;

import com.example.voiceorder.chatting.ChatMsgVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        if (!message.equals(""))
            msgList.add(msgVO);
    }

    public static void createRandNum() {
        int max_num_value = 2147483647;
        int min_num_value = 1;

        Random random = new Random();

        int randomNum = random.nextInt(max_num_value - min_num_value + 1) + min_num_value;
        session = randomNum;
    }
}