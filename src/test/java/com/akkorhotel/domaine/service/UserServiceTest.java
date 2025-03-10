package com.akkorhotel.domaine.service;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import com.akkorhotel.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
}
