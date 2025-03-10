package com.akkorhotel.domain.service;

import com.akkorhotel.application.dto.LoginRequest;
import com.akkorhotel.application.dto.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public LoginResponse authenticate(LoginRequest request) {
        if (!"john.doe@example.com".equals(request.getEmail())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return new LoginResponse("mocked-token");
    }
}