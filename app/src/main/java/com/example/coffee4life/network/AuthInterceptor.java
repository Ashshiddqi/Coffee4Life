package com.example.coffee4life.network;

import androidx.annotation.NonNull;

import com.example.coffee4life.Constants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("apikey", Constants.SUPABASE_ANON_KEY);

        if (original.header("Authorization") == null) {
            builder.header("Authorization", "Bearer " + Constants.SUPABASE_ANON_KEY);
        }

        return chain.proceed(builder.build());
    }
}
