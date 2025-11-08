package com.example.coffee4life.network.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.coffee4life.auth.SessionManager;
import com.example.coffee4life.network.OrderItemApi;
import com.example.coffee4life.network.RetrofitClient;
import com.example.coffee4life.network.models.OrderItemRequest;
import com.example.coffee4life.network.models.OrderItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderItemRepository {

    public interface CallbackVoid {
        void onSuccess();
        void onError(String message);
    }

    public interface CallbackList {
        void onSuccess(java.util.List<OrderItemResponse> items);
        void onError(String message);
    }

    private final OrderItemApi api;
    private final SessionManager sessionManager;

    public OrderItemRepository(Context ctx) {
        this.api = RetrofitClient.getInstance().create(OrderItemApi.class);
        this.sessionManager = new SessionManager(ctx.getApplicationContext());
    }

    private String bearer() {
        String token = sessionManager.getAccessToken();
        return token != null ? "Bearer " + token : null;
    }

    public void bulkInsert(List<OrderItemRequest> body, CallbackVoid cb) {
        Call<List<Object>> call = api.bulkInsert(body, bearer());
        call.enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(@NonNull Call<List<Object>> call, @NonNull Response<List<Object>> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess();
                } else {
                    String msg = "Failed to insert order items";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Object>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void list(String orderId, CallbackList cb) {
        String filter = orderId != null ? "eq." + orderId : null;
        Call<java.util.List<OrderItemResponse>> call = api.list("*", bearer(), filter);
        call.enqueue(new Callback<java.util.List<OrderItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.List<OrderItemResponse>> call, @NonNull Response<java.util.List<OrderItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    String msg = "Failed to load order items";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<java.util.List<OrderItemResponse>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
