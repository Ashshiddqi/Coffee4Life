package com.example.coffee4life.network.models;

public class OrderResponse {
    public String id;           // uuid
    public String order_number;
    public String customer_name;
    public Integer subtotal;
    public Integer cash;
    public Integer change;
    public String created_at;   // ISO timestamp
}
