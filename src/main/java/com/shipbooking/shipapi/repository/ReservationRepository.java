package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByBookingNumber(String bookingNumber);
    Optional<Reservation> findByBookingNumberAndBookerPhone(String bookingNumber, String bookerPhone);
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByScheduleId(Long scheduleId);
}
