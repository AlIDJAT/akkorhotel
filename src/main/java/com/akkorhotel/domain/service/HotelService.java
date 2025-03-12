package com.akkorhotel.domain.service;

import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.exception.HotelNotFoundException;
import com.akkorhotel.domain.repository.HotelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Hotel with ID {} not found", id);
                    return new HotelNotFoundException("Hotel not found");
                });
    }
}
