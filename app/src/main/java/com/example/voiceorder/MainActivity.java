package com.example.voiceorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

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


public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    LinearLayout voiceRecorder;
    TextView TTS_Result;

    /** Record & Stop, Audio file variable **/
    // Audio Authorization
    private static final int REQUEST_PERMISSION = 100;
    Voice voice;

    /** Beacon **/
    private static final String TAG = "MyBeacon: ";
    private BeaconManager beaconManager;
    private List<Beacon> beaconList = new ArrayList<>();
    private static final String BEACON_UUID = "fda50693-a4e2-4fb1-afcf-c6eb07647825";
    private static final int BEACON_MAJOR = 10004;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voiceRecorder = findViewById(R.id.recodeBtn);
        TTS_Result = findViewById(R.id.TTS_Result);

        voiceRecorder.setOnClickListener(v -> {
            try {
                // File Name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.mp3";

                Retrofit.uploadTextToChatbot("안녕", outputPath);

                Intent intent = new Intent(this, ChatRoomActivity.class);
                startActivity(intent);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        checkPermission();

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
            String uuid=beacon.getId1().toString();     // beacon uuid
            int major = beacon.getId2().toInt();        // beacon major
            Log.d(TAG, "UUID: " + uuid);
            if(uuid.equals(BEACON_UUID) && major == BEACON_MAJOR && ! Public.inStore){
                Log.d(TAG, "감지함");
                vibrate();
                drawOverlayView();

                try {
                    Public.session++;
                    Retrofit.clearBasket();
                    Public.msgList.clear();

                    // File Name
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.mp3";

                    String result = Retrofit.uploadTextToChatbot("안녕", outputPath);
                    TTS_Result.setText(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                startActivity(intent);

                Public.inStore = true;
            }
        }
        beaconList.clear();

        // Call itself in 1 second
        handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    /** Floating View **/
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
                Log.d(TAG, "진동함");
            } else {
                vibrator.vibrate(pattern, repeat);
            }
        }
    }
}