package com.example.coffee4life.network.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.coffee4life.auth.SessionManager;
import com.example.coffee4life.network.ProductApi;
import com.example.coffee4life.network.RetrofitClient;
import com.example.coffee4life.network.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {

    public interface CallbackList {
        void onSuccess(List<Product> items);
        void onError(String message);
    }

    public interface CallbackOne {
        void onSuccess(Product item);
        void onError(String message);
    }

    public interface CallbackVoid {
        void onSuccess();
        void onError(String message);
    }

    private final ProductApi api;
    private final SessionManager sessionManager;

    public ProductRepository(Context ctx) {
        this.api = RetrofitClient.getInstance().create(ProductApi.class);
        this.sessionManager = new SessionManager(ctx.getApplicationContext());
    }

    private String bearer() {
        String token = sessionManager.getAccessToken();
        return token != null ? "Bearer " + token : null;
    }

    public void list(CallbackList cb) {
        Call<List<Product>> call = api.list("*", bearer(), null, null, null);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    String msg = "Failed to load products";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void listFiltered(String categoryIdEq, Integer limit, String order, CallbackList cb) {
        String filter = null;
        if (categoryIdEq != null) filter = "eq." + categoryIdEq;
        Call<List<Product>> call = api.list("*", bearer(), filter, limit, order);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    String msg = "Failed to load products";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void create(Product body, CallbackOne cb) {
        Call<List<Product>> call = api.create(body, bearer());
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    cb.onSuccess(response.body().get(0));
                } else {
                    String msg = "Failed to create product";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void update(String id, Product body, CallbackOne cb) {
        Call<List<Product>> call = api.update("eq." + id, body, bearer());
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    cb.onSuccess(response.body().get(0));
                } else {
                    String msg = "Failed to update product";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void delete(String id, CallbackVoid cb) {
        Call<Void> call = api.delete("eq." + id, bearer());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess();
                } else {
                    String msg = "Failed to delete product";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
