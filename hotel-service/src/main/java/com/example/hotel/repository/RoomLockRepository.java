package com.example.hotel.repository;

import com.example.hotel.entity.RoomLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomLockRepository extends JpaRepository<RoomLock, Long> {
    Optional<RoomLock> findByRequestId(String requestId);

    @Query("""
        SELECT l FROM RoomLock l
        WHERE l.roomId = :roomId AND l.status = 'ACTIVE'
          AND l.startDate < :endDate AND l.endDate > :startDate
        """)
    List<RoomLock> findActiveOverlapping(@Param("roomId") Long roomId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}
