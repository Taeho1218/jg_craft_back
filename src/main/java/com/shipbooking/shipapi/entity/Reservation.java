package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [예약/예매 엔티티 - EC2 reservations 테이블 매핑]
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
    @Column(name = "reservation_id")
    private Long id; // 예약 PK ID

    @Column(name = "reservation_code", nullable = false, unique = true, length = 30)
    private String bookingNumber; // 예약 코드 (예: BK-20260806-9821)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User user; // 예약 회원 (members.member_id)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; // 예약한 운항 일정 (sailing_schedules.schedule_id)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_class_id")
    private SeatGrade seatGrade; // 좌석 등급 (ship_seat_classes.seat_class_id)

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount; // 총 승선 인원 수

    @Column(name = "total_amount", nullable = false)
    private Integer totalPrice; // 총 결제 금액 (원)

    @Column(name = "reservation_status", nullable = false, length = 20)
    @Builder.Default
    private String reservationStatusStr = "예약완료"; // 예약완료, 취소

    @Transient
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "reserved_at", updatable = false)
    private LocalDateTime reservedAt;

    @Transient
    private String bookerName;

    @Transient
    private String bookerPhone;

    public String getBookerName() {
        if (this.bookerName != null) return this.bookerName;
        return this.user != null ? this.user.getName() : "";
    }

    public String getBookerPhone() {
        if (this.bookerPhone != null) return this.bookerPhone;
        return this.user != null ? this.user.getPhone() : "";
    }

    public ReservationStatus getStatus() {
        if ("취소".equals(this.reservationStatusStr)) {
            return ReservationStatus.CANCELLED;
        }
        return ReservationStatus.CONFIRMED;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
        if (status == ReservationStatus.CANCELLED) {
            this.reservationStatusStr = "취소";
        } else {
            this.reservationStatusStr = "예약완료";
        }
    }

    @PrePersist
    protected void onCreate() {
        this.reservedAt = LocalDateTime.now();
        if (this.reservationStatusStr == null) {
            this.reservationStatusStr = "예약완료";
        }
    }

    public enum ReservationStatus {
        CONFIRMED, // 예매 확정
        CANCELLED  // 예매 취소
    }
}
