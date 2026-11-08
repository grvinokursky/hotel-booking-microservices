package com.example.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(@NotBlank String username, String password, String role) { }
