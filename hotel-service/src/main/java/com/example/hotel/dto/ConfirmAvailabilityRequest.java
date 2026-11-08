package com.example.hotel.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ConfirmAvailabilityRequest(
        @NotNull Long bookingId,
        @NotBlank String requestId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) { }
