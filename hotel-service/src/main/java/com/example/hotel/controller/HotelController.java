package com.example.hotel.controller;

import com.example.hotel.dto.*;
import com.example.hotel.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {
    private final HotelService hotelService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public HotelDto create(@Valid @RequestBody CreateHotelRequest req) { return hotelService.create(req); }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<HotelDto> list() { return hotelService.findAll(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public HotelDto get(@PathVariable Long id) { return hotelService.findById(id); }
}
