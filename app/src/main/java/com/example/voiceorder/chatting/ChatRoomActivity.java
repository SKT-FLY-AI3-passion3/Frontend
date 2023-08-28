package com.example.voiceorder.chatting;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.voiceorder.Public;
import com.example.voiceorder.R;
import com.example.voiceorder.API.Retrofit;
import com.example.voiceorder.Voice;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatRoomActivity extends AppCompatActivity {
    RelativeLayout chatRoom;

    /** Server Upload variable **/
    private String outputPath;
    File outFile;

    /** Touch Event variable **/
    private long lastClickTime = 0;
    private int clickTime = 0;
    private final int TIMES_REQUIRED_START_Recording = 2;
    private final int TIMES_REQUIRED_STOP_Recording = 3;
    private final int TIME_TIMEOUT = 2000;

    static RecyclerView rv;
    static ChatMsgAdapter mAdapter;

    Voice voice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatRoom = findViewById(R.id.chatRoom);
        chatRoom.setOnClickListener(v -> {
            try {
                TouchContinuously();
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        });

        rv = findViewById(R.id.rv);

        // Adapter 붙이기.
        mAdapter = new ChatMsgAdapter(Public.msgList);
        rv.scrollToPosition(Public.msgList.size()-1);
        rv.setAdapter(mAdapter);

        voice = new Voice(this);
    }

    public static void addMessage(boolean isUser, String message) {
        Public.addMessage(isUser, message);

        // Draw RecyclerView Again
        mAdapter = new ChatMsgAdapter(Public.msgList);
        rv.setAdapter(mAdapter);
        rv.scrollToPosition(Public.msgList.size()-1);
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

    /** Touch Event => Start/Stop Recording **/
    private void TouchContinuously() throws IOException, JSONException {
        if (SystemClock.elapsedRealtime() - lastClickTime < TIME_TIMEOUT) {
            clickTime++;
        } else {
            clickTime = 1;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        // Start Recording
        if (!Public.isRecording && clickTime == TIMES_REQUIRED_START_Recording) {
            // File Name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.mp3";
            outFile = new File(outputPath);

            Voice.startRecording(outputPath);
        }

        // Stop Recording
        if (Public.isRecording && clickTime == TIMES_REQUIRED_STOP_Recording) {
            Voice.stopRecording();

            Retrofit.uploadFileToServer(outFile, outputPath);
        }
    }
}