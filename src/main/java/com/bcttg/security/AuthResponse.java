package com.bcttg.security;

import java.util.List;

public class AuthResponse {
    private final String accessToken;
    private final String tokenType;
    private final long expiresInMinutes;
    private final List<String> roles;

    public AuthResponse(String accessToken, String tokenType, long expiresInMinutes, List<String> roles) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInMinutes = expiresInMinutes;
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public List<String> getRoles() {
        return roles;
    }
}
