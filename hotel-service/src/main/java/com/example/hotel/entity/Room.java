package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms", indexes = @Index(name = "idx_rooms_hotel", columnList = "hotel_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    @Column(nullable = false) private String number;
    @Column(nullable = false) private boolean available = true;
    @Column(name = "times_booked", nullable = false) private int timesBooked = 0;
    @Version private Long version;
}
