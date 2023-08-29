package com.example.voiceorder.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Class: Client for API using **/
public class Retrofit_client {
    private static final String BASE_URL = "https://passion3.sktchatbot.kr";

    public static Retrofit_interface getApiService(){return getInstance().create(Retrofit_interface.class);}

    private static Retrofit getInstance(){
        // Log Setting
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        // Client Definition
        OkHttpClient client = TrustOkHttpClientUtil.getUnsafeOkHttpClient().build();

        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }
}