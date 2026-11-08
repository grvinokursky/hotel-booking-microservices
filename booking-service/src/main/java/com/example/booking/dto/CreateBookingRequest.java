package com.example.booking.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateBookingRequest(
        @NotBlank @Size(max = 64) String requestId,
        Long roomId,
        Boolean autoSelect,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) { }
