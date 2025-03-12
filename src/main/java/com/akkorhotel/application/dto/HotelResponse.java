package com.akkorhotel.application.dto;

import com.akkorhotel.domain.entity.Hotel;

import java.util.List;

public record HotelResponse(Long id, String name, String location, String description, List<String> pictureList) {
    public static HotelResponse fromEntity(Hotel hotel) {
        return new HotelResponse(hotel.getId(), hotel.getName(), hotel.getLocation(), hotel.getDescription(), hotel.getPictureList());
    }
}