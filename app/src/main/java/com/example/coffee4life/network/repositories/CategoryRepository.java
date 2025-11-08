package com.example.coffee4life.network.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.coffee4life.auth.SessionManager;
import com.example.coffee4life.network.CategoryApi;
import com.example.coffee4life.network.RetrofitClient;
import com.example.coffee4life.network.models.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRepository {

    public interface CallbackList {
        void onSuccess(List<Category> items);
        void onError(String message);
    }

    public interface CallbackOne {
        void onSuccess(Category item);
        void onError(String message);
    }

    public interface CallbackVoid {
        void onSuccess();
        void onError(String message);
    }

    private final CategoryApi api;
    private final SessionManager sessionManager;

    public CategoryRepository(Context ctx) {
        this.api = RetrofitClient.getInstance().create(CategoryApi.class);
        this.sessionManager = new SessionManager(ctx.getApplicationContext());
    }

    private String bearer() {
        String token = sessionManager.getAccessToken();
        return token != null ? "Bearer " + token : null;
    }

    public void list(CallbackList cb) {
        Call<List<Category>> call = api.list("*", bearer());
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    String msg = "Failed to load categories";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void create(Category body, CallbackOne cb) {
        Call<List<Category>> call = api.create(body, bearer());
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    cb.onSuccess(response.body().get(0));
                } else {
                    String msg = "Failed to create category";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void update(String id, Category body, CallbackOne cb) {
        Call<List<Category>> call = api.update("eq." + id, body, bearer());
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    cb.onSuccess(response.body().get(0));
                } else {
                    String msg = "Failed to update category";
                    try { msg = response.errorBody() != null ? response.errorBody().string() : msg; } catch (Exception ignored) {}
                    cb.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
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
                    String msg = "Failed to delete category";
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
