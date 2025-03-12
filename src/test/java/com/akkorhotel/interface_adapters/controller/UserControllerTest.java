package com.akkorhotel.interface_adapters.controller;

import com.akkorhotel.domain.entity.User;
import com.akkorhotel.domain.entity.UserRole;
import com.akkorhotel.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        normalUser = new User(1L, "user@gmail.com", "User123", "password", UserRole.USER);
        adminUser = new User(2L, "admin@gmail.com", "AdminUser", "password", UserRole.ADMIN);
    }

    private void mockSecurityContext(User user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldCreateUser() throws Exception {
        User user = new User(1L, "ali.djatou@gmail.com", "AliDJATOU", "password123", UserRole.USER);
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "ali.djatou@gmail.com",
                                "pseudo": "AliDJATOU",
                                "password": "password123",
                                "role": "USER"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ali.djatou@gmail.com"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void shouldNotCreateUserIfEmailExists() throws Exception {
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "email": "existing.email@example.com",
                        "pseudo": "ExistingUser",
                        "password": "password123",
                        "role": "USER"
                    }
                    """))
                .andExpect(status().isBadRequest())  // ✅ Vérifie que le code est bien 400
                .andExpect(jsonPath("$.error").value("Email already in use"));  // ✅ Vérifie le message d'erreur

        verify(userService, times(1)).createUser(any(User.class));
    }


    @Test
    void shouldAllowAdminToGetAnyUser() throws Exception {
        when(userService.getUserById(eq(1L), any(User.class))).thenReturn(normalUser);
        mockSecurityContext(adminUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@gmail.com"));

        verify(userService, times(1)).getUserById(eq(1L), any(User.class));
    }

    @Test
    void shouldNotAllowUserToGetAnotherUser() throws Exception {
        when(userService.getUserById(eq(2L), any(User.class)))
                .thenThrow(new SecurityException("You are not allowed to access this user"));

        mockSecurityContext(normalUser);

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isForbidden());

        verify(userService, times(1)).getUserById(eq(2L), any(User.class));
    }

    @Test
    void shouldAllowUserToUpdateOwnAccount() throws Exception {
        when(userService.updateUser(eq(1L), any(User.class), any(User.class))).thenReturn(normalUser);
        mockSecurityContext(normalUser);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "updated.email@gmail.com",
                            "pseudo": "UpdatedUser",
                            "password": "newpassword"
                        }
                        """))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(eq(1L), any(User.class), any(User.class));
    }

    @Test
    void shouldNotAllowUserToUpdateAnotherUser() throws Exception {
        when(userService.updateUser(eq(2L), any(User.class), any(User.class)))
                .thenThrow(new SecurityException("You are not allowed to update this user"));

        mockMvc.perform(put("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "email": "updated.email@gmail.com",
                        "pseudo": "UpdatedUser",
                        "password": "newpassword"
                    }
                    """))
                .andExpect(status().isForbidden())  //
                .andExpect(jsonPath("$.error").value("You are not allowed to update this user"));  // ✅ Vérifie le message d'erreur

        verify(userService, times(1)).updateUser(eq(2L), any(User.class), any(User.class));
    }


    @Test
    void shouldAllowUserToDeleteOwnAccount() throws Exception {
        doNothing().when(userService).deleteUser(eq(1L), any(User.class));
        mockSecurityContext(normalUser);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(eq(1L), any(User.class));
    }

    @Test
    void shouldNotAllowUserToDeleteAnotherUser() throws Exception {
        doThrow(new SecurityException("You are not allowed to delete this user"))
                .when(userService).deleteUser(eq(2L), any(User.class));

        mockMvc.perform(delete("/users/2"))
                .andExpect(status().isForbidden())  //
                .andExpect(jsonPath("$.error").value("You are not allowed to delete this user"));  // ✅ Vérifie le message d'erreur

        verify(userService, times(1)).deleteUser(eq(2L), any(User.class));
    }



    @Test
    void shouldAllowAdminToDeleteAnyUser() throws Exception {
        doNothing().when(userService).deleteUser(eq(1L), any(User.class));
        mockSecurityContext(adminUser);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(eq(1L), any(User.class));
    }
}
