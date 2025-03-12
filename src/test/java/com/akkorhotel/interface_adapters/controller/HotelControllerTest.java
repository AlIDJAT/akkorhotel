package com.akkorhotel.interface_adapters.controller;

import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.exception.HotelNotFoundException;
import com.akkorhotel.domain.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HotelControllerTest {

    private MockMvc mockMvc;

    @Mock private HotelService hotelService;
    @InjectMocks private HotelController hotelController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(hotelController).build();
    }

    @Test
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
}
