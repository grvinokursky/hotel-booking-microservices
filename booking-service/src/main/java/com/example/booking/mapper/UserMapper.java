package com.example.booking.mapper;

import com.example.booking.dto.UserDto;
import com.example.booking.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserDto toDto(User user);
}
