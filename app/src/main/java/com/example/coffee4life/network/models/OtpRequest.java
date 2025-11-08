package com.example.coffee4life.network.models;

public class OtpRequest {
    public String email;
    public String type; // e.g., "signup"

    public OtpRequest(String email, String type) {
        this.email = email;
        this.type = type;
    }
}
