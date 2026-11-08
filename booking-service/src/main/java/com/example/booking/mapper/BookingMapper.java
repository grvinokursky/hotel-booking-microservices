package com.example.booking.mapper;

import com.example.booking.dto.BookingDto;
import com.example.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "status", expression = "java(b.getStatus().name())")
    BookingDto toDto(Booking b);
}
