package com.example.voiceorder.chatting;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.widget.RelativeLayout;

import com.example.voiceorder.Public;
import com.example.voiceorder.R;
import com.example.voiceorder.API.Retrofit;
import com.example.voiceorder.Voice;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Class: ChatRoom Activity **/
public class ChatRoomActivity extends AppCompatActivity {
    /** Components **/
    RelativeLayout chatRoom;
    static RecyclerView rv;
    static ChatMsgAdapter mAdapter;

    /** Server Upload variable **/
    private String outputPath;
    File outFile;

    /** Touch Event variable **/
    private long lastClickTime = 0;
    private int clickTime = 0;
    private final int TIMES_REQUIRED_START_Recording = 2;
    private final int TIMES_REQUIRED_STOP_Recording = 3;
    private final int TIME_TIMEOUT = 2000;

    Voice voice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Attach Touch Event to Screen
        chatRoom = findViewById(R.id.chatRoom);
        chatRoom.setOnClickListener(v -> {
            try {
                TouchContinuously();
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        });

        // Attach Adapter to RecyclerView
        rv = findViewById(R.id.rv);
        mAdapter = new ChatMsgAdapter(Public.msgList);
        rv.scrollToPosition(Public.msgList.size()-1);
        rv.setAdapter(mAdapter);

        voice = new Voice(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Public.inStore = false;
        Public.msgList.clear();

        Public.session++;
        Retrofit.clearBasket();
        Voice.stopRecording();
        Voice.stopGuide();
    }

    /** Add Message to List and Draw RecyclerView Again **/
    public static void addMessage(boolean isUser, String message) {
        Public.addMessage(isUser, message);     // Add Message to List

        // Draw RecyclerView Again
        mAdapter = new ChatMsgAdapter(Public.msgList);
        rv.setAdapter(mAdapter);
        rv.scrollToPosition(Public.msgList.size()-1);
    }

    /** Touch Event => Start/Stop Recording **/
    private void TouchContinuously() throws IOException, JSONException {
        // Count Touch
        if (SystemClock.elapsedRealtime() - lastClickTime < TIME_TIMEOUT) {
            clickTime++;
        } else {
            clickTime = 1;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        // Start Recording
        if (!Public.isRecording && clickTime == TIMES_REQUIRED_START_Recording) {
            // File Name & Path
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.mp3";
            outFile = new File(outputPath);

            Voice.startRecording(outputPath);                   // Start Recording
        }

        // Stop Recording
        if (Public.isRecording && clickTime == TIMES_REQUIRED_STOP_Recording) {
            Voice.stopRecording();                              // Stop Recording

            Retrofit.uploadFileToServer(outFile, outputPath);   // STT: Upload File to Server
        }
    }
}