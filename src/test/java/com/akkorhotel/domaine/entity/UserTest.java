package com.akkorhotel.domaine.entity;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldCreateUserWithEssentialFields() {
        User user = new User(1L, "john.doe@example.com", "JohnDoe", "password123", UserRole.USER);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getPseudo()).isEqualTo("JohnDoe");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }
}