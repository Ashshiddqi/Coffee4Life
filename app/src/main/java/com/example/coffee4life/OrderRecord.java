package com.example.coffee4life;

public class OrderRecord {
    public long id; // timestamp
    public String customerName;
    public int totalPrice;
    public int itemCount;

    public OrderRecord(long id, String customerName, int totalPrice, int itemCount) {
        this.id = id;
        this.customerName = customerName;
        this.totalPrice = totalPrice;
        this.itemCount = itemCount;
    }
}
