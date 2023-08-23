package com.example.voiceorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
    private boolean isRecording = false;

    /** Server Upload variable **/
    private String outputPath;
    File outFile;

    /** Touch Event variable **/
    private long lastClickTime = 0;
    private int clickTime = 0;
    private final int TIMES_REQUIRED_START_Recording = 2;
    private final int TIMES_REQUIRED_STOP_Recording = 3;
    private final int TIME_TIMEOUT = 2000;

    /** TTS Result Play variable **/
    MediaPlayer mediaPlayer;
    File mp3File;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voiceRecorder = findViewById(R.id.recodeBtn);

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

        // If want, stop guide voice and reply
        // stopGuide();

        // File Name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.mp3";
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
        if (!isRecording && clickTime == TIMES_REQUIRED_START_Recording)
            startRecording();
        else if (isRecording)
            Toast.makeText(this, "Already Recording", Toast.LENGTH_SHORT).show();

        // Stop Recording
        if (isRecording && clickTime == TIMES_REQUIRED_STOP_Recording)
            stopRecording();
    }

    /** Upload FLAC file to Server **/
    private void uploadFileToServer() {
        // Create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), outFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", outFile.getName(), requestFile);
        Call<ResponseBody> call = Retrofit_client.getApiService().uploadFile("file", body);

        // Request to Server
        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()){
                    ResponseBody body = response.body();
                    Log.e("Result", "Connection succeeded but did not receive a value");
                }else{
                    // Send STT Result to Chatbot
                    ResponseBody body = response.body();
                    String text;
                    try {
                        text = body.string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // 이게 안되는 중

                    // Send Text from Chatbot to TTS
                    try {
                        uploadTextToServer("안녕하세요! 만나서 반가워요.");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("Result", "Can't connect to Server");
            }
        });
    }

    /** Upload Text to Server **/
    private void uploadTextToServer(String text) throws JSONException {
        // Create JsonObject instance from file
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        Call<ResponseBody> call = Retrofit_client.getApiService().uploadText(jsonObject);

        // Request to Server
        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()){
                    Log.e("Result", "Connection succeeded but did not receive a value");
                }else{
                    // File Name
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String outputPath = getExternalFilesDir(null).getAbsolutePath() + "/" + timeStamp + "recorded_audio.mp3";
                    mp3File = new File(outputPath);

                    ResponseBody responseBody = response.body();
                    try {
                        InputStream inputStream = responseBody.byteStream();
                        FileOutputStream outputStream = new FileOutputStream(mp3File);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.close();
                        inputStream.close();

                         // Play Result
                         Uri uri = Uri.fromFile(mp3File);
                        playGuide(uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("Result", "Can't connect to Server");
            }
        });
    }

    /** Play Guide Voice **/
    private void playGuide(Uri guideMp3) {
        mediaPlayer = MediaPlayer.create(this, guideMp3);
        mediaPlayer.start();
    }

    /** Stop Guide Voice **/
    private void stopGuide() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null ;
        }
    }
}