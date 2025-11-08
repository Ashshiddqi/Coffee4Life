package com.example.coffee4life.network.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.coffee4life.auth.SessionManager;
import com.example.coffee4life.network.OrderApi;
import com.example.coffee4life.network.RetrofitClient;
import com.example.coffee4life.network.models.OrderRequest;
import com.example.coffee4life.network.models.OrderResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {

    public interface CallbackVoid {
        void onSuccess();
        void onError(String message);
    }

    public interface CallbackList {
        void onSuccess(java.util.List<com.example.coffee4life.network.models.OrderResponse> items);
        void onError(String message);
    }

    public void list(CallbackList cb) {
        Call<java.util.List<com.example.coffee4life.network.models.OrderResponse>> call = api.list("*", bearer(), "created_at.desc");
        call.enqueue(new Callback<java.util.List<com.example.coffee4life.network.models.OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.List<com.example.coffee4life.network.models.OrderResponse>> call, @NonNull Response<java.util.List<com.example.coffee4life.network.models.OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    String msg = "Failed to load orders";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<java.util.List<com.example.coffee4life.network.models.OrderResponse>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public interface CallbackOrderId {
        void onSuccess(String orderId);
        void onError(String message);
    }

    private final OrderApi api;
    private final SessionManager sessionManager;

    public OrderRepository(Context ctx) {
        this.api = RetrofitClient.getInstance().create(OrderApi.class);
        this.sessionManager = new SessionManager(ctx.getApplicationContext());
    }

    private String bearer() {
        String token = sessionManager.getAccessToken();
        return token != null ? "Bearer " + token : null;
    }

    public void create(OrderRequest body, CallbackOrderId cb) {
        Call<List<OrderResponse>> call = api.create(body, bearer());
        call.enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderResponse>> call, @NonNull Response<List<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String id = response.body().get(0).id;
                    cb.onSuccess(id);
                } else {
                    String msg = "Failed to create order";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderResponse>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
