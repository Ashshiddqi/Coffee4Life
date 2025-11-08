package com.example.coffee4life.network.models;

public class Category {
    public String id;           // uuid string
    public String name;
    public String description;  // nullable
    public String created_at;   // iso timestamp
    public String user_id;      // uuid string

    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
