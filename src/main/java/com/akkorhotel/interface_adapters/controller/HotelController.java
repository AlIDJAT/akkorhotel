package com.akkorhotel.interface_adapters.controller;

import com.akkorhotel.application.dto.HotelResponse;
import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.exception.HotelNotFoundException;
import com.akkorhotel.domain.service.HotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(HotelResponse.fromEntity(hotelService.getHotelById(id)));
    }

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleHotelNotFound(HotelNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Seuls les admins peuvent créer un hôtel
    public ResponseEntity<Hotel> createHotel(@RequestBody Hotel hotel) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.createHotel(hotel));
    }

    @GetMapping
    public ResponseEntity<List<Hotel>> listHotels(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String sortBy) {
        List<Hotel> hotels = hotelService.listHotels(limit, sortBy);
        return ResponseEntity.ok(hotels);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hotel> updateHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        Hotel updatedHotel = hotelService.updateHotel(id, hotel);
        return ResponseEntity.ok(updatedHotel);
    }
}
