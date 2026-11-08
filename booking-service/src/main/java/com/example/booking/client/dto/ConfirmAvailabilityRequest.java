package com.example.booking.client.dto;

import java.time.LocalDate;

public record ConfirmAvailabilityRequest(Long bookingId, String requestId,
                                         LocalDate startDate, LocalDate endDate) { }
