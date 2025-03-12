package com.akkorhotel.domain.service;

import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.repository.HotelRepository;

public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }
}
