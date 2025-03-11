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
        String token = jwtProvider.generateToken("john.doe@example.com");

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Arrange
        String token = jwtProvider.generateToken("john.doe@example.com");

        // Act
        boolean isValid = jwtProvider.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

}