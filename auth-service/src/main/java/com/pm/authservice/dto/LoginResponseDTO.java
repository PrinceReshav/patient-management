package com.pm.authservice.dto;


public class LoginResponseDTO {
    private final String token; // final token => Once initialized, cannot be reinitialized

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

