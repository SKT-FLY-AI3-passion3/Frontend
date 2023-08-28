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

public class Retrofit {

    /** Upload FLAC file to Server **/
    public static void clearBasket() {
        Call<ResponseBody> call = Retrofit_client.getApiService().clearBasket();

        // Request to Server
        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("MResult: ", "Can't connect to Server");
            }
        });
    }

    /** Upload FLAC file to Server **/
    public static String uploadFileToServer(File outFile, String outputPath) {
        final String[] result = new String[1];

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
                    Log.e("MResult: ", "Connection succeeded but did not receive a value");
                }else{
                    // Send STT Result to Chatbot
                    ResponseBody body = response.body();
                    try {
                        result[0] = body.string();
                        ChatRoomActivity.addMessage(true, result[0]);
                        Log.d("sfddsfafadfs", result[0]);

                        uploadTextToChatbot(result[0], outputPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("MResult: ", "Can't connect to Server");
            }
        });

        return result[0];
    }

    /** Upload Text to Server **/
    public static String uploadTextToChatbot(String text, String outputPath) throws JSONException {
        final String[] result = new String[1];

        // Create JsonObject instance from file
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        jsonObject.addProperty("session", Public.session);
        Call<ResponseBody> call = Retrofit_client.getApiService().uploadTextToChatbot(jsonObject);

        // Request to Server
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    ResponseBody body = response.body();
                    Log.e("MResult: ", "Connection succeeded but did not receive a value");
                } else {
                    // Send STT Result to Chatbot
                    ResponseBody body = response.body();
                    try {
                        result[0] = body.string();
                        ChatRoomActivity.addMessage(false, result[0]);

                        uploadTextToServer(result[0], outputPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("MResult: ", "Can't connect to Server");
            }
        });
        return result[0];
    }

    /** Upload Text to Server **/
    public static Uri uploadTextToServer(String text, String outputPath) throws JSONException {
        final Uri[] mp3Url = new Uri[1];

        // Create JsonObject instance from file
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        Call<ResponseBody> call = Retrofit_client.getApiService().uploadText(jsonObject);

        // Request to Server
        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()){
                    Log.e("MResult: ", "Connection succeeded but did not receive a value");
                }else{
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
                        mp3Url[0] = Uri.fromFile(mp3File);
                        Voice.playGuide(mp3Url[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("MResult: ", "Can't connect to Server");
            }
        });

        return mp3Url[0];
    }
}
