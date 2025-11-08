package com.example.coffee4life.network.models;

public class AuthResponse {
    public String access_token;
    public String token_type;
    public Long expires_in;
    public String refresh_token;
    public User user;

    public static class User {
        public String id;
        public String email;
        public java.util.Map<String, Object> user_metadata;
    }
}
