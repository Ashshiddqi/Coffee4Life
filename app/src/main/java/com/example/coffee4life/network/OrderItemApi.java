package com.example.coffee4life.network;

import com.example.coffee4life.network.models.OrderItemRequest;
import com.example.coffee4life.network.models.OrderItemResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OrderItemApi {
    @Headers({
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("rest/v1/order_items")
    Call<List<Object>> bulkInsert(@Body List<OrderItemRequest> body, @Header("Authorization") String authorization);

    @GET("rest/v1/order_items")
    Call<List<OrderItemResponse>> list(@Query("select") String select,
                                       @Header("Authorization") String authorization,
                                       @Query("order_id") String orderIdEq);
}
