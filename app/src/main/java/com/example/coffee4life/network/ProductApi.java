package com.example.coffee4life.network;

import com.example.coffee4life.network.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ProductApi {

    @GET("rest/v1/products")
    Call<List<Product>> list(@Query("select") String select,
                             @Header("Authorization") String authorization,
                             @Query("category_id") String categoryIdFilter,
                             @Query("limit") Integer limit,
                             @Query("order") String order);

    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/products")
    Call<List<Product>> create(@Body Product body, @Header("Authorization") String authorization);

    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("rest/v1/products")
    Call<List<Product>> update(@Query("id") String idEq, @Body Product body, @Header("Authorization") String authorization);

    @DELETE("rest/v1/products")
    Call<Void> delete(@Query("id") String idEq, @Header("Authorization") String authorization);
}
