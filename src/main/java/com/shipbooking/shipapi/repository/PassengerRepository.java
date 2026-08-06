package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByReservationId(Long reservationId);
    List<Passenger> findByReservationScheduleId(Long scheduleId);
}
