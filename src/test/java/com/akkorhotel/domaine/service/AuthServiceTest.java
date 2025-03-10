package com.akkorhotel.domaine.service;

import com.akkorhotel.application.dto.LoginRequest;
import com.akkorhotel.application.dto.LoginResponse;
import com.akkorhotel.domain.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


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
        LoginResponse response = authService.authenticate();

        // Assert
        assertThat(response).isNotNull();
    }
}
