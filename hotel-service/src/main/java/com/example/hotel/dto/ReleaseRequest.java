package com.example.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReleaseRequest(@NotNull Long bookingId, @NotBlank String requestId) { }
