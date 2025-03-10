package com.akkorhotel.domain.service;

import com.akkorhotel.application.dto.LoginRequest;
import com.akkorhotel.application.dto.LoginResponse;
import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.repository.UserRepository;
import com.akkorhotel.infrastructure.security.JwtProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    public LoginResponse authenticate(LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(user.get().getEmail());
        return new LoginResponse(token);
    }
}