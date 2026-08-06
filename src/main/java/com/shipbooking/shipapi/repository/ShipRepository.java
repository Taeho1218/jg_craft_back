package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.Ship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShipRepository extends JpaRepository<Ship, Long> {
    List<Ship> findByCompanyId(Long companyId);
}
