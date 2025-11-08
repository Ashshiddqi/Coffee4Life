package com.example.coffee4life.network.models;

import java.util.Map;

public class SignUpRequest {
    public String email;
    public String password;
    public Map<String, Object> data; // optional user metadata

    public SignUpRequest(String email, String password, Map<String, Object> data) {
        this.email = email;
        this.password = password;
        this.data = data;
    }
}
