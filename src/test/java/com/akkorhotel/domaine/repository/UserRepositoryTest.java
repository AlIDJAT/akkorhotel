package com.akkorhotel.domaine.repository;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import com.akkorhotel.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = User.builder()
                .email("john.doe@example.com")
                .pseudo("JohnDoe")
                .password("password123")
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldCheckIfEmailExists() {
        User user = User.builder()
                .email("existing.email@example.com")
                .pseudo("ExistingUser")
                .password("password123")
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
        boolean exists = userRepository.existsByEmail("existing.email@example.com");
        assertThat(exists).isTrue();

        boolean notExists = userRepository.existsByEmail("notfound@example.com");
        assertThat(notExists).isFalse();
    }
}