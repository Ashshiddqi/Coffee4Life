package com.example.coffee4life.network;

import com.example.coffee4life.network.models.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Header;

public interface CategoryApi {

    @GET("rest/v1/categories")
    Call<List<Category>> list(@Query("select") String select, @Header("Authorization") String authorization);

    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/categories")
    Call<List<Category>> create(@Body Category body, @Header("Authorization") String authorization);

    // Update by id using filter id=eq.<uuid>
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/categories")
    Call<List<Category>> update(@Query("id") String idEq, @Body Category body, @Header("Authorization") String authorization);

    // Delete by id using DELETE with filter id=eq.<uuid>
    @DELETE("rest/v1/categories")
    Call<Void> delete(@Query("id") String idEq, @Header("Authorization") String authorization);
}
