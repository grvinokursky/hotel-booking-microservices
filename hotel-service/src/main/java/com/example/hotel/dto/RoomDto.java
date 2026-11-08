package com.example.hotel.dto;

public record RoomDto(Long id, Long hotelId, String number, boolean available, int timesBooked) { }
