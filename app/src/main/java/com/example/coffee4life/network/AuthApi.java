package com.example.coffee4life.network;

import com.example.coffee4life.network.models.AuthResponse;
import com.example.coffee4life.network.models.SignInRequest;
import com.example.coffee4life.network.models.SignUpRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApi {
    @Headers({
            "Content-Type: application/json",
            // Authorization header will be added by OkHttp if needed per-call; keep base static anon in Retrofit client if preferred
    })
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body SignUpRequest body);

    @Headers({
            "Content-Type: application/json"
    })
    @POST("auth/v1/token")
    Call<AuthResponse> signInWithPassword(@Query("grant_type") String grantType, @Body SignInRequest body);
}
