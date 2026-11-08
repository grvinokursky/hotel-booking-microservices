package com.example.booking.controller;

import com.example.booking.dto.*;
import com.example.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/booking")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(@Valid @RequestBody CreateBookingRequest req, Authentication auth) {
        return bookingService.create(currentUserId(auth), req);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<BookingDto> list(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 Authentication auth) {
        return bookingService.listForUser(currentUserId(auth), PageRequest.of(page, size));
    }

    @GetMapping("/booking/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public BookingDto get(@PathVariable Long id, Authentication auth) {
        return bookingService.findById(id, currentUserId(auth), isAdmin(auth));
    }

    @DeleteMapping("/booking/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, Authentication auth) {
        bookingService.cancel(id, currentUserId(auth), isAdmin(auth));
    }

    private Long currentUserId(Authentication auth) {
        Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
        return jwt.getClaim("userId");
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
