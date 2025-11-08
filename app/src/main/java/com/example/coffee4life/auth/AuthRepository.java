package com.example.coffee4life.auth;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.coffee4life.network.AuthApi;
import com.example.coffee4life.network.RetrofitClient;
import com.example.coffee4life.network.models.AuthResponse;
import com.example.coffee4life.network.models.SignInRequest;
import com.example.coffee4life.network.models.SignUpRequest;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public interface AuthCallback {
        void onSuccess(AuthResponse res);
        void onError(String message);
    }

    private final AuthApi api;
    private final SessionManager sessionManager;

    public AuthRepository(Context context) {
        this.api = RetrofitClient.getInstance().create(AuthApi.class);
        this.sessionManager = new SessionManager(context.getApplicationContext());
    }

    public void signIn(String email, String password, final AuthCallback cb) {
        Call<AuthResponse> call = api.signInWithPassword("password", new SignInRequest(email, password));
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse body = response.body();
                    if (body.access_token != null) {
                        sessionManager.saveSession(body.access_token, body.refresh_token, email);
                    }
                    // Save display name from Supabase metadata if present
                    try {
                        if (body.user != null && body.user.user_metadata != null) {
                            Object dn = body.user.user_metadata.get("display_name");
                            Object fn = body.user.user_metadata.get("full_name");
                            String display = null;
                            if (dn != null && dn.toString().trim().length() > 0) display = dn.toString();
                            else if (fn != null && fn.toString().trim().length() > 0) display = fn.toString();
                            if (display != null) sessionManager.setDisplayName(display);
                        }
                    } catch (Exception ignored) {}
                    cb.onSuccess(body);
                } else {
                    String msg = "Login failed";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void signUp(String fullName, String email, String password, final AuthCallback cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("full_name", fullName);
        data.put("display_name", fullName);
        SignUpRequest req = new SignUpRequest(email, password, data);
        Call<AuthResponse> call = api.signUp(req);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    AuthResponse body = response.body();
                    if (body != null && body.access_token != null) {
                        sessionManager.saveSession(body.access_token, body.refresh_token, email);
                        // Persist display name from signup input
                        try { sessionManager.setDisplayName(fullName); } catch (Exception ignored) {}
                    }
                    cb.onSuccess(body);
                } else {
                    String msg = "Signup failed";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
