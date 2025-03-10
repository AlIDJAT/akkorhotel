package com.akkorhotel.domaine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.mockito.Mockito.*;

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
}
