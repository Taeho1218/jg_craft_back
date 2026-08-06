package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.ScheduleSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long> {
    List<ScheduleSeat> findByScheduleId(Long scheduleId);
    Optional<ScheduleSeat> findByScheduleIdAndSeatGradeId(Long scheduleId, Long seatGradeId);
}
