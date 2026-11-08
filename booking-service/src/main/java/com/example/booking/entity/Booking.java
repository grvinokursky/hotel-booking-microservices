package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_request", columnList = "request_id", unique = true),
        @Index(name = "idx_bookings_user", columnList = "user_id"),
        @Index(name = "idx_bookings_room_dates", columnList = "room_id,start_date,end_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "room_id") private Long roomId;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BookingStatus status;
    @Column(name = "request_id", nullable = false, unique = true) private String requestId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "cancel_reason") private String cancelReason;
}
