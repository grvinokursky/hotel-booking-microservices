package com.example.booking.client.dto;

public record RoomDto(Long id, Long hotelId, String number, boolean available, int timesBooked) { }
