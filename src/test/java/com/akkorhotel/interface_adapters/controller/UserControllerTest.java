package com.akkorhotel.interface_adapters.controller;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import com.akkorhotel.domain.service.UserService;
import com.akkorhotel.infrastructure.security.JwtProvider;
import com.akkorhotel.infrastructure.security.MyDomainUserDetails;
import com.akkorhotel.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, UserControllerTest.TestConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @BeforeEach
    void resetMocks() {
        // Réinitialise toutes les interactions enregistrées sur le mock
        Mockito.reset(userService);
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        // On simule un retour normal du service
        User user = new User(1L, "newuser@gmail.com", "NewUser", "password", UserRole.USER);
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "newuser@gmail.com",
                            "pseudo": "NewUser",
                            "password": "password",
                            "role": "USER"
                        }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@gmail.com"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
        // On simule que le service lance une exception quand l'email existe déjà
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "existing@gmail.com",
                            "pseudo": "ExistingUser",
                            "password": "password",
                            "role": "USER"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already in use"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @WithUserDetails(value = "admin@gmail.com", userDetailsServiceBeanName = "userDetailsService")
    void shouldGetUserSuccessfully() throws Exception {
        User user = new User(1L, "user@gmail.com", "User123", "password", UserRole.USER);
        when(userService.getUserById(eq(1L))).thenReturn(user);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@gmail.com"));

        verify(userService, times(1)).getUserById(1L);
    }

    /**
     * Cas où l'utilisateur connecté (id = 1) tente de récupérer un autre user (id = 2).
     * Spring Security doit bloquer -> 403 (sans appel au service).
     */
    @Test
    @WithUserDetails(value = "user@gmail.com", userDetailsServiceBeanName = "userDetailsService")
    void shouldReturnForbiddenWhenUserTriesToAccessAnotherUser() throws Exception {
        mockMvc.perform(get("/users/{id}", 2L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied: You do not have permission to perform this action."));

        // Aucune interaction avec le service, bloqué par Spring Security
        verifyNoInteractions(userService);
    }

    @Test
    @WithUserDetails(value = "user@gmail.com", userDetailsServiceBeanName = "userDetailsService")
    void shouldUpdateOwnAccountSuccessfully() throws Exception {
        User updatedUser = new User(1L, "updated@gmail.com", "UpdatedUser", "newpassword", UserRole.USER);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "updated@gmail.com",
                                "pseudo": "UpdatedUser",
                                "password": "newpassword"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));

        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    /**
     * Cas où l'utilisateur connecté (id = 1) tente de modifier un autre user (id = 2).
     * Spring Security doit bloquer -> 403 (sans appel au service).
     */
    @Test
    @WithUserDetails(value = "user@gmail.com", userDetailsServiceBeanName = "userDetailsService")
    void shouldReturnForbiddenWhenUserTriesToUpdateAnotherUser() throws Exception {
        mockMvc.perform(put("/users/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "updated@gmail.com",
                                "pseudo": "UpdatedUser",
                                "password": "newpassword"
                            }
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied: You do not have permission to perform this action."));

        verifyNoInteractions(userService);
    }

    @Test
    @WithUserDetails(value = "user@gmail.com", userDetailsServiceBeanName = "userDetailsService")
    void shouldDeleteOwnAccountSuccessfully() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    /**
     * Cas où l'utilisateur connecté (id = 1) tente de supprimer un autre user (id = 2).
     * Spring Security doit bloquer -> 403 (sans appel au service).
     */
    @Test
    @WithUserDetails(value = "user@gmail.com", userDetailsServiceBeanName = "userDetailsService")
    void shouldReturnForbiddenWhenUserTriesToDeleteAnotherUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", 2L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied: You do not have permission to perform this action."));

        // On vérifie qu'aucun appel n'a été fait au service, puisque Spring Security bloque avant
        verifyNoInteractions(userService);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        @Primary
        public JwtProvider jwtProvider() {
            return Mockito.mock(JwtProvider.class);
        }

        @Bean
        @Primary
        public UserDetailsService userDetailsService() {
            return email -> {
                if ("admin@gmail.com".equals(email)) {
                    User domainUser = new User(99L, email, "AdminPseudo", "dummy", UserRole.ADMIN);
                    return new MyDomainUserDetails(domainUser);
                } else if ("user@gmail.com".equals(email)) {
                    User domainUser = new User(1L, email, "UserPseudo", "dummy", UserRole.USER);
                    return new MyDomainUserDetails(domainUser);
                }
                throw new UsernameNotFoundException("User not found for email: " + email);
            };
        }
    }
}
