package com.example.coffee4life.network.models;

import java.math.BigDecimal;

public class Product {
    public String id;           // uuid string
    public String name;
    public BigDecimal price;    // numeric
    public Integer stock;       // integer
    public String image_url;    // nullable
    public String created_at;   // iso timestamp
    public String user_id;      // uuid string
    public String category_id;  // uuid string, nullable

    public Product() {}

    public Product(String name, BigDecimal price, Integer stock, String category_id, String image_url) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category_id = category_id;
        this.image_url = image_url;
    }
}
