package com.example.voiceorder;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.IOException;

/** Class: Main Page **/
public class Voice {
    static Activity activity;

    /** Variables used to Recording & Playing **/
    public static MediaPlayer mediaPlayer;
    public static MediaRecorder mediaRecorder;

    /** Constructor **/
    public Voice(Activity activity) {
        this.activity = activity;
    }

    /** Play Notification **/
    public static void noti() {
        // Obtaining an Identifier Value for a Sound
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Get Ringtone Objects that Play Sound
        Ringtone ringtone = RingtoneManager.getRingtone(activity.getApplicationContext(), notification);

        // Play Sound
        ringtone.play();
    }

    /** Start Recording **/
    public static void startRecording(String outputPath) {
        // If want, Stop Guide Voice and Reply
        while(mediaPlayer != null && mediaPlayer.isPlaying())
            stopGuide();
        noti();                 // Play Notification

        // Setting Media Recorder
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

            noti();     // Play Notification
        }
    }

    /** Play Guide Voice **/
    public static void playGuide(Uri guideMp3) {
        // If Guide Playing, Stop First
        stopGuide();

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(activity, guideMp3);
            mediaPlayer.start();
        }
    }

    /** Stop Guide Voice **/
    public static void stopGuide() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()){    // If Playingg
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null ;
        } else if (mediaPlayer != null) {                       // If not Playing
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null ;
        }
    }
}
