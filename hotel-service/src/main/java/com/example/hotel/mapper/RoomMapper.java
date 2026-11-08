package com.example.hotel.mapper;

import com.example.hotel.dto.RoomDto;
import com.example.hotel.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(target = "hotelId", source = "hotel.id")
    RoomDto toDto(Room room);
}
