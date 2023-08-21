package com.example.voiceorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button voiceRecorder;

    /** 녹음 및 중지, 오디오 파일 관련 변수 **/
    // 오디오 권한
    private String recordPermission = android.Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    // 오디오 파일 녹음 관련 변수
    private MediaRecorder mediaRecorder;
    private String audioFileName;           // 오디오 녹음 생성 파일 이름
    private boolean isRecording = false;    // 현재 녹음 상태를 확인하기 위함.
    private Uri audioUri = null;            // 오디오 파일 uri

    /** 터치 이벤트 관련 변수 **/
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

        checkAudioPermission();
        voiceRecorder.setOnClickListener(v -> TouchContinuously());
    }

    /** 녹음 및 중지 +*/
    // 오디오 파일 권한 체크
    private boolean checkAudioPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    // 녹음 시작
    private void startRecording() {
        // 파일의 외부 경로 확인
        String recordPath = getExternalFilesDir("/").getAbsolutePath();
        // 파일 이름 변수를 현재 날짜가 들어가도록 초기화. 그 이유는 중복된 이름으로 기존에 있던 파일이 덮어 쓰여지는 것을 방지하고자 함.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        audioFileName = recordPath + "/" +"RecordExample_" + timeStamp + "_"+"audio.mp4";

        // Media Recorder 생성 및 설정
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //녹음 시작
        mediaRecorder.start();
        Toast.makeText(this, "Start Record", Toast.LENGTH_SHORT).show();
    }

    // 녹음 종료
    private void stopRecording() {
        // 녹음 종료 종료
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        // 파일 경로(String) 값을 Uri로 변환해서 저장
        //      - Why? : 리사이클러뷰에 들어가는 ArrayList가 Uri를 가지기 때문
        //      - File Path를 알면 File을  인스턴스를 만들어 사용할 수 있기 때문
        audioUri = Uri.parse(audioFileName);
        Toast.makeText(this, "Stop Record", Toast.LENGTH_SHORT).show();
    }

    /** 터치 이벤트 **/
    private void TouchContinuously() {
        // 기준 시각보다 짧은 시각 안에 다음 터치를 하면 1 더하고, 아니면 1로 초기화
        if (SystemClock.elapsedRealtime() - lastClickTime < TIME_TIMEOUT) {
            clickTime++;
        } else {
            clickTime = 1;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        // 2번 연속터치시 녹음 시작
        if (mediaRecorder == null && clickTime == TIMES_REQUIRED_START)
            startRecording();
        else
            Toast.makeText(this, "Already Recording", Toast.LENGTH_SHORT).show();

        // 3번 연속터치시 녹음 중지
        if (mediaRecorder != null && clickTime == TIMES_REQUIRED_Terminate)
            stopRecording();
    }
}