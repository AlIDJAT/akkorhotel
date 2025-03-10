package com.akkorhotel.domaine.service;

import com.akkorhotel.application.dto.LoginRequest;
import com.akkorhotel.application.dto.LoginResponse;
import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.repository.UserRepository;
import com.akkorhotel.domain.service.AuthService;
import com.akkorhotel.infrastructure.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.akkorhotel.domain.entity.UserRole.USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;  //

    @Mock
    private JwtProvider jwtProvider; //
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnJwtWhenCredentialsAreValid() {
        // Arrange
        User user = new User(1L, "john.doe@example.com", "JohnDoe", "hashedpassword", USER);
        LoginRequest request = new LoginRequest("john.doe@example.com", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtProvider.generateToken(user.getEmail())).thenReturn("mocked-jwt-token");

        // Act
        LoginResponse response = authService.authenticate(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked-jwt-token");
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        // Arrange
        LoginRequest request = new LoginRequest("wrong.email@example.com", "wrongpassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate(request));

        verify(userRepository, times(1)).findByEmail(request.getEmail());
    }

    }
