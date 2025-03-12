package com.akkorhotel.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
    }

    @Test
    void shouldGenerateValidJwtToken() {
        // Act
        String token = jwtProvider.generateToken("john.doe@example.com", "USER"); // 🔥 Ajouter le rôle

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Arrange
        String token = jwtProvider.generateToken("john.doe@example.com", "USER");

        // Act
        boolean isValid = jwtProvider.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldExtractEmailFromValidToken() {
        // Arrange
        String token = jwtProvider.generateToken("john.doe@example.com", "USER");

        // Act
        String email = jwtProvider.extractEmailFromToken(token);

        // Assert
        assertThat(email).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldExtractRoleFromValidToken() {
        // Arrange
        String token = jwtProvider.generateToken("john.doe@example.com", "USER");

        // Act
        String role = jwtProvider.extractRoleFromToken(token); // 🔥 Ajouter ce test

        // Assert
        assertThat(role).isEqualTo("USER");
    }
}
