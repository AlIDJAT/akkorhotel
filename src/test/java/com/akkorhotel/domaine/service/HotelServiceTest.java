package com.akkorhotel.domaine.service;

import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.repository.HotelRepository;
import com.akkorhotel.domain.service.HotelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock private HotelRepository hotelRepository;
    @InjectMocks private HotelService hotelService;

    @Test
    void shouldCreateHotelSuccessfully() {
        // Given
        Hotel hotel = new Hotel(null, "Hilton", "Paris", "Luxury hotel", List.of("img1.jpg", "img2.jpg"));
        when(hotelRepository.save(any(Hotel.class)))
                .thenReturn(new Hotel(1L, "Hilton", "Paris", "Luxury hotel", List.of("img1.jpg", "img2.jpg")));

        // When
        Hotel createdHotel = hotelService.createHotel(hotel);

        // Then
        assertNotNull(createdHotel.getId());
        assertEquals("Hilton", createdHotel.getName());
        assertEquals("Paris", createdHotel.getLocation());
    }

}

