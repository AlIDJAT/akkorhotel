package com.akkorhotel.interface_adapters.controller;

import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.exception.HotelNotFoundException;
import com.akkorhotel.domain.service.HotelService;
import com.akkorhotel.infrastructure.security.JwtProvider;
import com.akkorhotel.infrastructure.security.SecurityConfig;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HotelController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, HotelControllerTest.TestConfig.class})
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Le service est injecté via notre configuration de test
    @Autowired
    private HotelService hotelService;

    @Test
    @WithMockUser(roles = "ADMIN") // Simule un utilisateur ADMIN
    void shouldCreateHotelSuccessfully() throws Exception {
        // Given
        Hotel hotel = new Hotel(1L, "Hilton", "Paris", "Luxury hotel", List.of("img1.jpg", "img2.jpg"));
        when(hotelService.createHotel(any(Hotel.class))).thenReturn(hotel);

        // When & Then
        mockMvc.perform(post("/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Hilton",
                                "location": "Paris",
                                "description": "Luxury hotel",
                                "pictureList": ["img1.jpg", "img2.jpg"]
                            }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hilton"));

        verify(hotelService, times(1)).createHotel(any(Hotel.class));
    }

    @Test
    @WithMockUser(roles = "USER") // Simule un utilisateur NON ADMIN
    void shouldReturnForbiddenWhenUserTriesToCreateHotel() throws Exception {
        mockMvc.perform(post("/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Hilton",
                                "location": "Paris",
                                "description": "Luxury hotel",
                                "pictureList": ["img1.jpg", "img2.jpg"]
                            }
                        """))
                .andExpect(status().isForbidden());

        verify(hotelService, never()).createHotel(any(Hotel.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetHotelByIdSuccessfully() throws Exception {
        // Given
        Long hotelId = 1L;
        Hotel hotel = new Hotel(hotelId, "Hilton", "Paris", "Luxury hotel", List.of("img1.jpg", "img2.jpg"));
        when(hotelService.getHotelById(hotelId)).thenReturn(hotel);

        // When & Then
        mockMvc.perform(get("/hotels/{id}", hotelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(hotelId))
                .andExpect(jsonPath("$.name").value("Hilton"))
                .andExpect(jsonPath("$.location").value("Paris"));

        verify(hotelService, times(1)).getHotelById(hotelId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenHotelDoesNotExist() throws Exception {
        // Given
        Long hotelId = 99L;
        when(hotelService.getHotelById(hotelId)).thenThrow(new HotelNotFoundException("Hotel not found"));

        // When & Then
        mockMvc.perform(get("/hotels/{id}", hotelId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Hotel not found"));

        verify(hotelService, times(1)).getHotelById(hotelId);
    }

    @Test
    void shouldListHotelsWithDefaultLimit() throws Exception {
        List<Hotel> hotels = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            hotels.add(new Hotel((long) i, "Hotel " + i, "Location " + i, "Description " + i, List.of("img" + i + ".jpg")));
        }
        when(hotelService.listHotels(10, null)).thenReturn(hotels);

        mockMvc.perform(get("/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));

        verify(hotelService, times(1)).listHotels(10, null);
    }

    // List hotels with custom limit
    @Test
    void shouldListHotelsWithCustomLimit() throws Exception {
        int customLimit = 5;
        List<Hotel> hotels = new ArrayList<>();
        for (int i = 1; i <= customLimit; i++) {
            hotels.add(new Hotel((long) i, "Hotel " + i, "Location " + i, "Description " + i, List.of("img" + i + ".jpg")));
        }
        when(hotelService.listHotels(customLimit, null)).thenReturn(hotels);

        mockMvc.perform(get("/hotels").param("limit", String.valueOf(customLimit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(customLimit));

        verify(hotelService, times(1)).listHotels(customLimit, null);
    }

    // Update hotel endpoint (admin only)
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateHotelSuccessfully() throws Exception {
        Long hotelId = 1L;
        Hotel updatedHotel = new Hotel(hotelId, "Updated Hilton", "Paris", "Updated description", List.of("img1.jpg"));
        when(hotelService.updateHotel(eq(hotelId), any(Hotel.class))).thenReturn(updatedHotel);

        mockMvc.perform(put("/hotels/{id}", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Hilton",
                                    "location": "Paris",
                                    "description": "Updated description",
                                    "pictureList": ["img1.jpg"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Hilton"));

        verify(hotelService, times(1)).updateHotel(eq(hotelId), any(Hotel.class));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER") // S'assurer que le rôle est bien "USER"
    void shouldReturnForbiddenWhenNonAdminTriesToUpdateHotel() throws Exception {
        Long hotelId = 1L;

        mockMvc.perform(put("/hotels/{id}", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "Updated Hilton",
                            "location": "Paris",
                            "description": "Updated description",
                            "pictureList": ["img1.jpg"]
                        }
                        """))
                .andExpect(status().isForbidden()); // Vérifie que l'accès est refusé

        verify(hotelService, never()).updateHotel(eq(hotelId), any(Hotel.class)); // L'appel ne doit jamais être fait
    }



    // Delete hotel endpoint (admin only)
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteHotelSuccessfully() throws Exception {
        Long hotelId = 1L;
        doNothing().when(hotelService).deleteHotel(hotelId);

        mockMvc.perform(delete("/hotels/{id}", hotelId))
                .andExpect(status().isNoContent());

        verify(hotelService, times(1)).deleteHotel(hotelId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenNonAdminTriesToDeleteHotel() throws Exception {
        Long hotelId = 1L;
        mockMvc.perform(delete("/hotels/{id}", hotelId))
                .andExpect(status().isForbidden());

        verify(hotelService, never()).deleteHotel(hotelId);
    }

    // ---------------------------------------------------------------------------------
    // Configuration de test pour fournir les beans manquants (HotelService, JwtProvider, UserDetailsService)
    // ---------------------------------------------------------------------------------
    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public HotelService hotelService() {
            return Mockito.mock(HotelService.class);
        }

        @Bean
        @Primary
        public JwtProvider jwtProvider() {
            return Mockito.mock(JwtProvider.class);
        }

        @Bean
        @Primary
        public UserDetailsService userDetailsService() {
            // Fournit une implémentation dummy qui renvoie un utilisateur avec ROLE_USER pour toute requête
            return email -> org.springframework.security.core.userdetails.User
                    .withUsername(email)
                    .password("dummy")
                    .roles("USER") // Ceci renvoie automatiquement ROLE_USER
                    .build();
        }


    }
}
