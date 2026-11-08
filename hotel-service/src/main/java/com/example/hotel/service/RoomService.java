package com.example.hotel.service;

import com.example.hotel.dto.*;
import com.example.hotel.entity.*;
import com.example.hotel.exception.*;
import com.example.hotel.mapper.RoomMapper;
import com.example.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomLockRepository lockRepository;
    private final RoomMapper roomMapper;

    @Transactional
    public RoomDto create(CreateRoomRequest req) {
        Hotel hotel = hotelRepository.findById(req.hotelId())
                .orElseThrow(() -> new NotFoundException("Hotel " + req.hotelId() + " not found"));
        Room r = Room.builder()
                .hotel(hotel).number(req.number())
                .available(req.available() == null || req.available())
                .timesBooked(0).build();
        return roomMapper.toDto(roomRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getAllAvailable() {
        return roomRepository.findByAvailableTrue().stream().map(roomMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RoomDto> recommend() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedAscIdAsc()
                .stream().map(roomMapper::toDto).toList();
    }

    @Transactional
    public void confirmAvailability(Long roomId, ConfirmAvailabilityRequest req) {
        if (lockRepository.findByRequestId(req.requestId()).isPresent()) {
            log.info("[requestId={}] confirmAvailability is idempotent (already locked)", req.requestId());
            return;
        }
        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new NotFoundException("Room " + roomId + " not found"));
        if (!room.isAvailable()) {
            throw new ConflictException("Room " + roomId + " is not operationally available");
        }
        List<RoomLock> overlaps = lockRepository.findActiveOverlapping(roomId, req.startDate(), req.endDate());
        if (!overlaps.isEmpty()) {
            throw new ConflictException("Room " + roomId + " is not available for requested dates");
        }
        lockRepository.save(RoomLock.builder()
                .roomId(roomId).bookingId(req.bookingId()).requestId(req.requestId())
                .startDate(req.startDate()).endDate(req.endDate())
                .status(LockStatus.ACTIVE).createdAt(Instant.now()).build());
        room.setTimesBooked(room.getTimesBooked() + 1);
        log.info("[requestId={}] Confirmed availability room={} booking={}",
                req.requestId(), roomId, req.bookingId());
    }

    @Transactional
    public void release(Long roomId, ReleaseRequest req) {
        var opt = lockRepository.findByRequestId(req.requestId());
        if (opt.isEmpty()) {
            log.info("[requestId={}] release is idempotent (no lock found)", req.requestId());
            return;
        }
        RoomLock lock = opt.get();
        if (lock.getStatus() == LockStatus.RELEASED) {
            log.info("[requestId={}] release is idempotent (already released)", req.requestId());
            return;
        }
        lock.setStatus(LockStatus.RELEASED);
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room != null && room.getTimesBooked() > 0) room.setTimesBooked(room.getTimesBooked() - 1);
        log.info("[requestId={}] Released room={} booking={}", req.requestId(), roomId, req.bookingId());
    }
}
