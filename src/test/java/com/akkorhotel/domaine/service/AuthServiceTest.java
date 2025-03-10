package com.akkorhotel.domaine.service;

import com.akkorhotel.application.dto.LoginRequest;
import com.akkorhotel.application.dto.LoginResponse;
import com.akkorhotel.domain.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnJwtWhenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest("john.doe@example.com", "password123");

        // Act
        LoginResponse response = authService.authenticate(request);

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        // Arrange
        LoginRequest request = new LoginRequest("wrong.email@example.com", "wrongpassword");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate(request));

    }

    }
