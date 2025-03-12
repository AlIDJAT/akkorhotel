package com.akkorhotel.domain.service;

import com.akkorhotel.domain.entity.Hotel;
import com.akkorhotel.domain.exception.HotelNotFoundException;
import com.akkorhotel.domain.repository.HotelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Transactional
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

    public List<Hotel> listHotels(int limit, String sortBy) {
        if (sortBy != null && !sortBy.isEmpty()) {
            return hotelRepository.findAll(PageRequest.of(0, limit, Sort.by(sortBy))).getContent();
        } else {
            return hotelRepository.findAll(PageRequest.of(0, limit)).getContent();
        }
    }

    @Transactional
    public Hotel updateHotel(Long id, Hotel updatedHotel) {
        Hotel existingHotel = getHotelById(id);
        existingHotel.setName(updatedHotel.getName());
        existingHotel.setLocation(updatedHotel.getLocation());
        existingHotel.setDescription(updatedHotel.getDescription());
        existingHotel.setPictureList(updatedHotel.getPictureList());
        return hotelRepository.save(existingHotel);
    }

    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new HotelNotFoundException("Hotel not found");
        }
        hotelRepository.deleteById(id);
    }
}
