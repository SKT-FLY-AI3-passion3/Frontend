package com.example.voiceorder;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Voice {
    static Activity activity;

    /** TTS Result Play variable **/
    static MediaPlayer mediaPlayer;
    File mp3File;

    private static MediaRecorder mediaRecorder;

    public Voice(Activity activity) {
        this.activity = activity;
    }

    /** Play Notification **/
    public static void noti() {
        // 소리의 식별자 값을 얻는 과정
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 소리를 재생하는 Ringtone 객체 얻기
        Ringtone ringtone = RingtoneManager.getRingtone(activity.getApplicationContext(), notification);

        // 소리 재생하기
        ringtone.play();
    }

    /** Start Recording **/
    public static void startRecording(String outputPath) {
        // If want, stop guide voice and reply
        while (mediaPlayer != null && mediaPlayer.isPlaying())
            stopGuide();
        noti();

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
        Public.isRecording = true;
    }

    /** Stop Recording **/
    public static void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Public.isRecording = false;

            noti();
        }
    }

    /** Play Guide Voice **/
    public static void playGuide(Uri guideMp3) {
        while (mediaPlayer != null)
            stopGuide();

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(activity, guideMp3);
            mediaPlayer.start();
        }
    }

    /** Stop Guide Voice **/
    public static void stopGuide() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null ;
        }
    }
}
