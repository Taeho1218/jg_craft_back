package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [좌석 등급 엔티티 - EC2 ship_seat_classes 테이블 매핑]
 */
@Entity
@Table(name = "ship_seat_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_class_id")
    private Long id; // 좌석 등급 PK ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id", nullable = false)
    private Ship ship; // 해당 선박

    @Column(name = "class_name", nullable = false, length = 50)
    private String gradeName; // 등급명 (예: 일반실, 우등실, VIP실)

    @Column(name = "seat_capacity")
    private Integer seatCapacity; // 해당 등급 수용 인원

    @Column(name = "class_order")
    @Builder.Default
    private Integer classOrder = 1; // 정렬 순서

    @Column(name = "base_price")
    @Builder.Default
    private Integer basePrice = 60000; // 기본 요금 (원)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.basePrice == null) {
            this.basePrice = 60000;
        }
    }
}
