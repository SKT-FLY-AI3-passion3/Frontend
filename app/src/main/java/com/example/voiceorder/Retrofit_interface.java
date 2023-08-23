package com.example.voiceorder;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/** Interface for API using **/
interface Retrofit_interface {
    @GET("/")
    Call<ResponseBody> getRoot();

    // Send FLAC file to Server
    @Multipart
    @POST("/stt")
    Call<ResponseBody> uploadFile(
            @Part("key") String key,
            @Part MultipartBody.Part file
    );

    // Send Text to Server
    @POST("/tts/synthesize")
    Call<ResponseBody> uploadText(
            @Body JsonObject jsonObject
    );
}
