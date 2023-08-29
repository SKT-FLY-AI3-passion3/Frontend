package com.example.voiceorder.API;

import android.net.Uri;
import android.util.Log;

import com.example.voiceorder.Public;
import com.example.voiceorder.Voice;
import com.example.voiceorder.chatting.ChatRoomActivity;
import com.google.gson.JsonObject;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Class: API Code for Communicating with Server **/
public class Retrofit {
    private static final String TAG = "Server: ";

    /** Delete Records of Basket **/
    public static void clearBasket() {
        Call<ResponseBody> call = Retrofit_client.getApiService().clearBasket();

        // Request to Server
        call.enqueue(new Callback<ResponseBody>(){
            // Success to receive Response from Server
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            // Fail to receive Response from Server
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "Can't connect to Server");
            }
        });
    }

    /** STT: Upload MP3 file to Server **/
    public static void uploadFileToServer(File outFile, String outputPath) {
        // Create MultipartBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), outFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", outFile.getName(), requestFile);
        Call<ResponseBody> call = Retrofit_client.getApiService().uploadFile("file", body);

        // Request to Server
        call.enqueue(new Callback<ResponseBody>(){
            // Success to receive Response from Server
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()){
                    ResponseBody body = response.body();
                    Log.e(TAG, "Connection succeeded but did not receive a Response");
                }else{
                    try {
                        // Processing Results
                        ResponseBody body = response.body();
                        String result = body.string();

                        ChatRoomActivity.addMessage(true, result);  // Add Message to Array

                        // Send STT Result to Chatbot for Chatbot's Response
                        if (result.equals(""))
                            uploadTextToServer("잘 못 알아들었어요. 다시 한 번 말씀해주세요.", outputPath);
                        else
                            uploadTextToChatbot(result, outputPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // Fail to receive Response from Server
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "Can't connect to Server");
            }
        });
    }

    /** Chatbot: Send User's Text to Chatbot **/
    public static void uploadTextToChatbot(String text, String outputPath) throws JSONException {
        // Create JsonObject instance from text
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        jsonObject.addProperty("session", Public.session);
        Call<ResponseBody> call = Retrofit_client.getApiService().uploadTextToChatbot(jsonObject);

        // Request to Server
        call.enqueue(new Callback<ResponseBody>() {
            // Success to receive Response from Server
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Connection succeeded but did not receive a Response");
                } else {
                    try {
                        // Processing Results
                        ResponseBody body = response.body();
                        String result = body.string();

                        ChatRoomActivity.addMessage(false, result);  // Add Message to Array

                        // Send Chatbot's Response to Server for TTS
                        uploadTextToServer(result, outputPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // Fail to receive Response from Server
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "Can't connect to Server");
            }
        });
    }

    /** TTS: Upload Text to Server **/
    public static void uploadTextToServer(String text, String outputPath) throws JSONException {
        // Create JsonObject instance from text
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        Call<ResponseBody> call = Retrofit_client.getApiService().uploadText(jsonObject);

        // Request to Server
        call.enqueue(new Callback<ResponseBody>(){
            // Success to receive Response from Server
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()){
                    Log.e(TAG, "Connection succeeded but did not receive a Response");
                }else{
                    // Create MP3 File from the Response of Server
                    File mp3File = new File(outputPath);
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
                        Uri mp3Url = Uri.fromFile(mp3File);
                        Voice.playGuide(mp3Url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Fail to receive Response from Server
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "Can't connect to Server");
            }
        });
    }
}
