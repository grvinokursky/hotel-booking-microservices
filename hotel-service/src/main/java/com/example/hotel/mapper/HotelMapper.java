package com.example.hotel.mapper;

import com.example.hotel.dto.HotelDto;
import com.example.hotel.entity.Hotel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HotelMapper {
    HotelDto toDto(Hotel hotel);
}
