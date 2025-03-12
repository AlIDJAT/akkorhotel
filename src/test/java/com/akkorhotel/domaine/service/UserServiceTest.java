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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
        User user = new User(null, "ali.djatou@gmail.com", "AliDJATOU", "password123", UserRole.USER);

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.createUser(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("ali.djatou@gmail.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldNotCreateUserIfEmailAlreadyExists() {
        User user = new User(null, "ali.djatou@gmail.com", "AliDJATOU", "password123", UserRole.USER);

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFindUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        User foundUser = userService.getUserById(1L);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("user@gmail.com");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void shouldAllowAdminToAccessAnyUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        User foundUser = userService.getUserById(1L, adminUser);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("user@gmail.com");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void shouldNotAllowUserToAccessAnotherUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.getUserById(2L, normalUser))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You are not allowed to access this user");
    }

    @Test
    void shouldUpdateOwnAccountSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(normalUser);

        User updatedUser = new User(1L, "user@gmail.com", "NewPseudo", "newpassword", UserRole.USER);

        User result = userService.updateUser(1L, updatedUser, normalUser);

        assertThat(result.getPseudo()).isEqualTo("NewPseudo");
        assertThat(result.getPassword()).isEqualTo("hashedNewPassword");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldNotAllowUserToUpdateAnotherUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        User updatedUser = new User(2L, "updated@gmail.com", "UpdatedUser", "password", UserRole.USER);

        assertThatThrownBy(() -> userService.updateUser(2L, updatedUser, normalUser))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You are not allowed to update this user");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldAllowAdminToUpdateAnyUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(normalUser);

        User updatedUser = new User(1L, "updated@gmail.com", "UpdatedUser", "newpassword", UserRole.USER);

        User result = userService.updateUser(1L, updatedUser, adminUser);

        assertThat(result.getEmail()).isEqualTo("updated@gmail.com");
        assertThat(result.getPassword()).isEqualTo("hashedNewPassword");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldNotAllowUserToDeleteAnotherUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.deleteUser(2L, normalUser))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You are not allowed to delete this user");

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void shouldAllowUserToDeleteTheirOwnAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        userService.deleteUser(1L, normalUser);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldAllowAdminToDeleteAnyUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        userService.deleteUser(1L, adminUser);

        verify(userRepository, times(1)).deleteById(1L);
    }
}
