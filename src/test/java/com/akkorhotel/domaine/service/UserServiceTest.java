package com.akkorhotel.domaine.service;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import com.akkorhotel.domain.exception.UserNotFoundException;
import com.akkorhotel.domain.repository.UserRepository;
import com.akkorhotel.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User normalUser;

    private User adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        normalUser = new User(1L, "user@gmail.com", "User123", "password", UserRole.USER);
        adminUser = new User(2L, "admin@gmail.com", "Admin", "password", UserRole.ADMIN);
    }

    @Test
    void shouldCreateUserAndReturnIt() {
        User user = new User(1L, "ali.djatou@gmail.com", "AliDJATOU", "password123", UserRole.USER);

        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.createUser(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("ali.djatou@gmail.com");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldNotCreateUserIfEmailAlreadyExists() {
        User user = new User(1L, "ali.djatou@gmail.com", "AliDJATOU", "password123", UserRole.USER);

        when(userRepository.existsByEmail("ali.djatou@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFindUserById() {
        // Arrange
        User user = new User(1L, "ali.djatou@gmail.com", "AliDJATOU", "password123", UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User foundUser = userService.getUserById(1L);

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("ali.djatou@gmail.com");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void shouldReturnAllUsers() {
        // Arrange
        List<User> users = List.of(
                new User(1L, "ali.djatou@gmail.com", "AliDJATOU", "password123", UserRole.USER),
                new User(2L, "mohamed.ali@gmail.com", "MohamedAli", "password456", UserRole.ADMIN)
        );
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("ali.djatou@gmail.com");
        assertThat(result.get(1).getEmail()).isEqualTo("mohamed.ali@gmail.com");

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        // Arrange
        Long userId = 1L;
        User existingUser = new User(userId, "ali.djatou@gmail.com", "AliDJATOU", "password123", UserRole.USER);
        User updatedUser = new User(userId, "ali.djatou@gmail.com", "Ali Updated", "newpassword", UserRole.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(userId, updatedUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPseudo()).isEqualTo("Ali Updated");
        assertThat(result.getPassword()).isEqualTo("newpassword");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldNotAllowUserToAccessAnotherUser() {
        // Simuler que l'utilisateur essaie d'accéder à un autre utilisateur
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.getUserById(2L, normalUser))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You are not allowed to access this user");
    }


}
