package com.akkorhotel.application.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final String token;
    private final String role;
    private final String pseudo;

    public LoginResponse(String token, String role, String pseudo) {
        this.token = token;
        this.role = role;
        this.pseudo = pseudo;
    }
}