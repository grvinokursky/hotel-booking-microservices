package com.example.hotel.controller;

import com.example.hotel.dto.*;
import com.example.hotel.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDto create(@Valid @RequestBody CreateRoomRequest req) { return roomService.create(req); }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<RoomDto> list() { return roomService.getAllAvailable(); }

    @GetMapping("/recommend")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<RoomDto> recommend() { return roomService.recommend(); }

    @PostMapping("/{id}/confirm-availability")
    @PreAuthorize("isAuthenticated()")
    public void confirm(@PathVariable Long id, @Valid @RequestBody ConfirmAvailabilityRequest req) {
        roomService.confirmAvailability(id, req);
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("isAuthenticated()")
    public void release(@PathVariable Long id, @Valid @RequestBody ReleaseRequest req) {
        roomService.release(id, req);
    }
}
