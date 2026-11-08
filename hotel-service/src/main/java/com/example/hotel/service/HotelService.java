package com.example.hotel.service;

import com.example.hotel.dto.*;
import com.example.hotel.entity.Hotel;
import com.example.hotel.exception.NotFoundException;
import com.example.hotel.mapper.HotelMapper;
import com.example.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Transactional
    public HotelDto create(CreateHotelRequest req) {
        Hotel h = Hotel.builder().name(req.name()).address(req.address()).build();
        return hotelMapper.toDto(hotelRepository.save(h));
    }

    @Transactional(readOnly = true)
    public List<HotelDto> findAll() {
        return hotelRepository.findAll().stream().map(hotelMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public HotelDto findById(Long id) {
        return hotelRepository.findById(id).map(hotelMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Hotel " + id + " not found"));
    }
}
