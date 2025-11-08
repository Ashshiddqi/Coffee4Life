package com.example.coffee4life.network.models;

public class OrderItemRequest {
    public String order_id;      // uuid of orders.id
    public String product_id;    // uuid of products.id (nullable)
    public Integer qty;          // > 0
    public Integer price;        // unit price
    public String product_name;  // optional fallback

    public OrderItemRequest() {}

    public OrderItemRequest(String order_id, String product_id, Integer qty, Integer price, String product_name) {
        this.order_id = order_id;
        this.product_id = product_id;
        this.qty = qty;
        this.price = price;
        this.product_name = product_name;
    }
}
