package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDeparturePortAndArrivalPortAndDepartureTimeBetween(
            String departurePort,
            String arrivalPort,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Schedule> findByDeparturePortAndArrivalPort(String departurePort, String arrivalPort);
}
