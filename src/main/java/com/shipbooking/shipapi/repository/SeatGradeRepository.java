package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.SeatGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatGradeRepository extends JpaRepository<SeatGrade, Long> {
    List<SeatGrade> findByShipId(Long shipId);
}
