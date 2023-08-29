package com.example.voiceorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.LinearLayout;

import com.example.voiceorder.API.Retrofit;
import com.example.voiceorder.chatting.ChatRoomActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/** Class: Main Activity **/
public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    /** Components **/
    LinearLayout voiceRecorder;

    /** Record & Stop, Audio variable **/
    private static final int REQUEST_PERMISSION = 100;     // Audio Authorization
    Voice voice;

    /** Beacon **/
    private static final String TAG = "Beacon: ";
    private BeaconManager beaconManager;
    private List<Beacon> beaconList = new ArrayList<>();
    private static final String BEACON_UUID = "fda50693-a4e2-4fb1-afcf-c6eb07647825";
    private static final int BEACON_MAJOR = 10004;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect XML component and Variables
        voiceRecorder = findViewById(R.id.recodeBtn);

        // Attach Touch Event to Screen
        voiceRecorder.setOnClickListener(v -> {
            try {
                // File Name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.mp3";

                // Boot Chatbot
                Retrofit.uploadTextToChatbot("안녕", outputPath);

                // Execute ChatRoomActivity
                Intent intent = new Intent(this, ChatRoomActivity.class);
                startActivity(intent);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        // Check Permission needed to execute APP
        checkPermission();

        // Set Settings regarding to Beacon
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);
        handler.sendEmptyMessage(0);

        voice = new Voice(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        Public.inStore = false;
        Public.msgList.clear();

        vibrator.cancel();
        vibrator = null;
    }

    /** Obtaining Required Privileges **/
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION
            );
            return true;
        } else {
        }
        return false;
    }

    /** Beacon **/
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            // Execute when Beacon detected
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        for(Beacon beacon : beaconList){
            String uuid=beacon.getId1().toString();     // Beacon UUID
            int major = beacon.getId2().toInt();        // Beacon Major

            // If Detection of Target Beacon Success
            if(uuid.equals(BEACON_UUID) && major == BEACON_MAJOR && !Public.inStore){
                vibrate();          // Make VIbration
                drawOverlayView();  // Draw APP View on the Other APP

                try {
                    // Initialize
                    Public.session++;
                    Retrofit.clearBasket();
                    Public.msgList.clear();

                    // File Name & Path
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.mp3";

                    Retrofit.uploadTextToChatbot("안녕", outputPath);     // Chatbot: Send Start Text to Chatbot

                    // Execute ChatRoomActivity
                    Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                    startActivity(intent);

                    Public.inStore = true;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        beaconList.clear();

        // Call itself every second
        handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    /** Draw APP View Over other APP **/
    private void drawOverlayView() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.example.voiceorder", "com.example.voiceorder.MainActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /** Vibration **/
    private void vibrate() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {100, 200, 100, 200, 100, 200, 100, 200};
            int repeat = -1;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, repeat);
                vibrator.vibrate(vibrationEffect);
            } else {
                vibrator.vibrate(pattern, repeat);
            }
        }
    }
}