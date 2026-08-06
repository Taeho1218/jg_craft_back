package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDeparturePortAndArrivalPortAndSailingDateBetween(
            String departurePort,
            String arrivalPort,
            LocalDate start,
            LocalDate end
    );

    List<Schedule> findByDeparturePortAndArrivalPort(String departurePort, String arrivalPort);
}
