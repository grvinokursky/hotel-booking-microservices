package com.example.booking.service;

import com.example.booking.client.dto.*;
import com.example.booking.dto.*;
import com.example.booking.entity.*;
import com.example.booking.exception.*;
import com.example.booking.mapper.BookingMapper;
import com.example.booking.repository.BookingRepository;
import com.example.booking.security.HotelClientGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final HotelClientGateway hotelGateway;

    @Transactional(readOnly = true)
    public Page<BookingDto> listForUser(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(bookingMapper::toDto);
    }

    @Transactional(readOnly = true)
    public BookingDto findById(Long id, Long userId, boolean isAdmin) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));
        if (!isAdmin && !b.getUserId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        return bookingMapper.toDto(b);
    }

    public BookingDto create(Long userId, CreateBookingRequest req) {
        var existing = bookingRepository.findByRequestId(req.requestId());
        if (existing.isPresent()) {
            log.info("[requestId={}] Booking already exists, returning existing", req.requestId());
            return bookingMapper.toDto(existing.get());
        }
        if (!req.endDate().isAfter(req.startDate())) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        Booking booking = Booking.builder()
                .userId(userId).roomId(req.roomId())
                .startDate(req.startDate()).endDate(req.endDate())
                .status(BookingStatus.PENDING)
                .requestId(req.requestId())
                .createdAt(Instant.now()).build();
        booking = bookingRepository.save(booking);
        Long bookingId = booking.getId();
        log.info("[requestId={}] [bookingId={}] Booking created PENDING", req.requestId(), bookingId);

        List<RoomDto> candidates;
        boolean auto = Boolean.TRUE.equals(req.autoSelect());
        if (auto) {
            try {
                candidates = hotelGateway.getRecommendedRooms();
            } catch (Exception e) {
                log.error("[requestId={}] [bookingId={}] Hotel call failed: {}", req.requestId(), bookingId, e.getMessage());
                cancelAndSave(booking, "Hotel service unavailable");
                throw new SagaException("Booking failed: Hotel service is unavailable");
            }
            if (candidates.isEmpty()) {
                cancelAndSave(booking, "No rooms available");
                throw new ConflictException("No rooms available");
            }
        } else {
            if (req.roomId() == null) throw new IllegalArgumentException("roomId is required when autoSelect=false");
            candidates = List.of(new RoomDto(req.roomId(), null, null, true, 0));
        }

        Exception last = null;
        for (RoomDto c : candidates) {
            Long roomId = c.id();
            try {
                hotelGateway.confirmAvailability(roomId, new ConfirmAvailabilityRequest(
                        bookingId, req.requestId(), req.startDate(), req.endDate()));
                booking.setRoomId(roomId);
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                log.info("[requestId={}] [bookingId={}] Booking CONFIRMED room={}", req.requestId(), bookingId, roomId);
                return bookingMapper.toDto(booking);
            } catch (feign.FeignException.Conflict conflict) {
                last = conflict;
                log.warn("[requestId={}] [bookingId={}] Room {} busy, trying next", req.requestId(), bookingId, roomId);
                if (!auto) break;
            } catch (Exception e) {
                last = e;
                log.error("[requestId={}] [bookingId={}] Hotel call failed: {}", req.requestId(), bookingId, e.getMessage());
                hotelGateway.release(roomId, new ReleaseRequest(bookingId, req.requestId()));
                break;
            }
        }

        String errorMsg = (last != null && last instanceof feign.FeignException.Conflict)
                ? "Requested room is not available for chosen dates"
                : "Booking failed due to remote service error";

        cancelAndSave(booking, errorMsg);

        if (last instanceof feign.FeignException.Conflict) {
            throw new ConflictException(errorMsg);
        }
        throw new SagaException(errorMsg);
    }

    @Transactional
    public void cancel(Long bookingId, Long userId, boolean isAdmin) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking " + bookingId + " not found"));
        if (!isAdmin && !b.getUserId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        if (b.getStatus() == BookingStatus.CANCELLED) return;
        if (b.getStatus() == BookingStatus.CONFIRMED && b.getRoomId() != null) {
            hotelGateway.release(b.getRoomId(), new ReleaseRequest(b.getId(), b.getRequestId()));
        }
        cancelAndSave(b, "Cancelled by user");
    }

    private void cancelAndSave(Booking b, String reason) {
        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelReason(reason);
        bookingRepository.save(b);
        log.info("[requestId={}] [bookingId={}] Booking CANCELLED: {}", b.getRequestId(), b.getId(), reason);
    }
}