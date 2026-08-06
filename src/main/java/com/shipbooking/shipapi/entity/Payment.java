package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [결제 엔티티]
 * 예약에 대한 결제 정보 매핑
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation; // 해당 예약

    @Column(nullable = false, length = 30)
    private String paymentMethod; // 결제 수단 (CARD, KAKAO_PAY, NAVER_PAY 등)

    @Column(nullable = false)
    private Integer amount; // 결제 금액 (원)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status; // 결제 상태

    @Column(name = "paid_at", updatable = false)
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.paidAt = LocalDateTime.now();
    }

    public enum PaymentStatus {
        COMPLETED, // 결제 완료
        REFUNDED,  // 환불
        FAILED     // 결제 실패
    }
}
