package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
}
