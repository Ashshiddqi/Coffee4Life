package com.example.coffee4life.network;

import com.example.coffee4life.network.models.OrderRequest;
import com.example.coffee4life.network.models.OrderResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OrderApi {
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/orders")
    Call<List<OrderResponse>> create(@Body OrderRequest body, @Header("Authorization") String authorization);

    @GET("rest/v1/orders")
    Call<List<OrderResponse>> list(@Query("select") String select,
                                   @Header("Authorization") String authorization,
                                   @Query("order") String order);
}
