package com.example.coffee4life.network.models;

public class OrderItemResponse {
    public String id;           // uuid
    public String order_id;     // uuid
    public String product_id;   // uuid nullable
    public Integer qty;         // > 0
    public Integer price;       // unit price
    public String product_name; // optional fallback
}
