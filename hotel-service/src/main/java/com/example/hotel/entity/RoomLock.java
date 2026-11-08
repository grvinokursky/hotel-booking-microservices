package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "room_locks", indexes = {
        @Index(name = "idx_room_locks_room", columnList = "room_id,start_date,end_date"),
        @Index(name = "idx_room_locks_request", columnList = "request_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomLock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_id", nullable = false) private Long roomId;
    @Column(name = "booking_id", nullable = false) private Long bookingId;
    @Column(name = "request_id", nullable = false, unique = true) private String requestId;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LockStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
}
