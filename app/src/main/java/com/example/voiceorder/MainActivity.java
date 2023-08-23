package com.example.voiceorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    Button voiceRecorder;

    /** Record & Stop, Audio file variable **/
    // Audio Authorization
    private static final int REQUEST_PERMISSION = 100;

    // Recording
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;    // 현재 녹음 상태를 확인하기 위함.

    /** Server Upload variable **/
    private String outputPath;
    File outFile;

    /** Touch Event variable **/
    private long lastClickTime = 0;                 // 마지막 클릭 시간
    private int clickTime = 0;                      // 클릭된 횟수
    private final int TIMES_REQUIRED_START = 2;     // 녹음을 시작하는 데 총 필요한 클릭 횟수
    private final int TIMES_REQUIRED_Terminate = 3; // 녹음을 종료하는 데 총 필요한 클릭 횟수
    private final int TIME_TIMEOUT = 2000;          // 마지막 클릭후 제한시간


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voiceRecorder = findViewById(R.id.recoder);

        voiceRecorder.setOnClickListener(v -> {
            try {
                TouchContinuously();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Obtaining Required Privileges **/
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION
            );
        } else {
        }
    }

    /** Start Recording **/
    private void startRecording() {
        checkPermission();

        // File Name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.flac";
        outFile = new File(outputPath);

        // Media Recorder
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputPath);

        // Prepare and Start Recording
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
        isRecording = true;
    }

    /** Stop Recording **/
    private void stopRecording() throws IOException {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;

        // Upload FLAC file to Server
        uploadFileToServer();
    }

    /** Touch Event => Start/Stop Recording **/
    private void TouchContinuously() throws IOException {
        if (SystemClock.elapsedRealtime() - lastClickTime < TIME_TIMEOUT) {
            clickTime++;
        } else {
            clickTime = 1;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        // Start Recording
        if (!isRecording && clickTime == TIMES_REQUIRED_START)
            startRecording();
        else if (isRecording)
            Toast.makeText(this, "Already Recording", Toast.LENGTH_SHORT).show();

        // Stop Recording
        if (isRecording && clickTime == TIMES_REQUIRED_Terminate)
            stopRecording();
    }

    /** Upload FLAC file to Server **/
    private void uploadFileToServer() {
        // Create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), outFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", outFile.getName(), requestFile);

        Call<String> call = Retrofit_client.getApiService().uploadFile(body);

        call.enqueue(new Callback<String>(){
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()){
                    Log.e("결과", "응안돼~" + response.toString());
                }else{
                    Log.i("결과", "됨!");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i("결과", call.toString());
            }
        });
    }
}