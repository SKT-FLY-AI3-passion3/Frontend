package com.example.voiceorder.API;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/** Interface: Interface for API using **/
interface Retrofit_interface {
    /** STT: Upload MP3 file to Server **/
    // Send: MP3 File
    // Receive: Text
    @Multipart
    @POST("/stt")
    Call<ResponseBody> uploadFile(
            @Part("key") String key,
            @Part MultipartBody.Part file
    );

    /** Chatbot: Send User's Text to Chatbot **/
    // Send: Text
    // Receive: Text
    @POST("/chatbot")
    Call<ResponseBody> uploadTextToChatbot(
            @Body JsonObject jsonObject
    );

    /** TTS: Upload Text to Server **/
    // Send: Text
    // Receive: MP3 File
    @POST("/tts")
    Call<ResponseBody> uploadText(
            @Body JsonObject jsonObject
    );

    /** Delete Records of Basket **/
    // Send: NULL
    // Receive: NULL
    @DELETE("/food/clear")
    Call<ResponseBody> clearBasket();
}
