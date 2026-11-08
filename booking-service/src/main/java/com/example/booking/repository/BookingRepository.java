package com.example.booking.repository;

import com.example.booking.entity.Booking;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    Optional<Booking> findByRequestId(String requestId);
}
