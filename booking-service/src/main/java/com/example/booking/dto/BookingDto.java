package com.example.booking.dto;

import java.time.*;
public record BookingDto(Long id, Long userId, Long roomId, LocalDate startDate, LocalDate endDate,
                         String status, String requestId, Instant createdAt, String cancelReason) { }
