package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [예약/예매 엔티티]
 * 회원 또는 비회원이 특정 운항 일정을 예매한 전체 예약 정보
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String bookingNumber; // 예약 번호 (예: BK-20260806-9821)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 예약자 (회원인 경우 FK, 비회원은 null 가능)

    @Column(length = 50)
    private String bookerName; // 예약자 이름 (비회원/회원 공통)

    @Column(length = 20)
    private String bookerPhone; // 예약자 전화번호 (비회원 조회용)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; // 예약한 운항 일정

    @Column(nullable = false)
    private Integer passengerCount; // 총 승선 인원 수

    @Column(nullable = false)
    private Integer totalPrice; // 총 결제 금액 (원)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status; // 예약 상태

    @Column(name = "reserved_at", updatable = false)
    private LocalDateTime reservedAt;

    @PrePersist
    protected void onCreate() {
        this.reservedAt = LocalDateTime.now();
    }

    public enum ReservationStatus {
        CONFIRMED, // 예매 확정
        CANCELLED  // 예매 취소
    }
}
