package com.example.booking.client;

import com.example.booking.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "hotel-service")
public interface HotelClient {
    @PostMapping("/api/rooms/{id}/confirm-availability")
    void confirmAvailability(@PathVariable("id") Long id, @RequestBody ConfirmAvailabilityRequest req);

    @PostMapping("/api/rooms/{id}/release")
    void release(@PathVariable("id") Long id, @RequestBody ReleaseRequest req);

    @GetMapping("/api/rooms/recommend")
    List<RoomDto> getRecommendedRooms();
}
