package com.example.voiceorder;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/** Interface for API using **/
interface Retrofit_interface {
    @GET("/")
    Call<ResponseBody> getRoot();

    @Multipart
    @POST("/convert")
    Call<String> uploadFile(@Part MultipartBody.Part file);

    @POST("/")
    Call<String> uploadText(String text);
}
