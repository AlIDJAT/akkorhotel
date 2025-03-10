package com.akkorhotel.domaine.repository;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
        User foundUser = userRepository.findByEmail("john.doe@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("john.doe@example.com");
    }
}