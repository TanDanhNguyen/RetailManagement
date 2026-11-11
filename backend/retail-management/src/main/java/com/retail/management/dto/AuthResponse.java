package com.retail.management.dto;

public class AuthResponse {

    private String tokenType;
    private String accessToken;
    private Long userId;
    private String fullName;
    private String email;
    private String role;

    public AuthResponse(
            String tokenType,
            String accessToken,
            Long userId,
            String fullName,
            String email,
            String role) {

        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}