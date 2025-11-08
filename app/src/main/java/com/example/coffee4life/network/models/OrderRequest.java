package com.example.coffee4life.network.models;

public class OrderRequest {
    public String order_number;   // e.g., timestamp string or custom format
    public String customer_name;
    public Integer subtotal;      // total price
    public Integer cash;          // amount paid
    public Integer change;        // change to return

    public OrderRequest() {}

    public OrderRequest(String order_number, String customer_name, Integer subtotal, Integer cash, Integer change) {
        this.order_number = order_number;
        this.customer_name = customer_name;
        this.subtotal = subtotal;
        this.cash = cash;
        this.change = change;
    }
}
