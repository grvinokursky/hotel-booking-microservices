package com.example.booking.security;

import com.example.booking.client.HotelClient;
import com.example.booking.client.dto.*;
import com.example.booking.exception.SagaException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelClientGateway {

    private final HotelClient hotelClient;

    @Retry(name = "hotelService")
    @CircuitBreaker(name = "hotelService")
    public void confirmAvailability(Long roomId, ConfirmAvailabilityRequest req) {
        log.info("[requestId={}] Calling hotel-service confirmAvailability room={}", req.requestId(), roomId);
        hotelClient.confirmAvailability(roomId, req);
    }

    @Retry(name = "hotelRelease", fallbackMethod = "releaseFallback")
    public void release(Long roomId, ReleaseRequest req) {
        hotelClient.release(roomId, req);
    }

    @Retry(name = "hotelService")
    @CircuitBreaker(name = "hotelService")
    public List<RoomDto> getRecommendedRooms() {
        return hotelClient.getRecommendedRooms();
    }

    private void releaseFallback(Long roomId, ReleaseRequest req, Throwable t) {
        log.warn("[requestId={}] Release failed for room={}: {}", req.requestId(), roomId, t.getMessage());
    }
}
