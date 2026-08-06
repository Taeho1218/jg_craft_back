package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [좌석 등급 엔티티]
 * 선박 내 좌석 등급 정보 (예: 일반실, 우등실, VIP실) 및 기본 요금
 */
@Entity
@Table(name = "seat_grades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id", nullable = false)
    private Ship ship; // 해당 선박

    @Column(nullable = false, length = 50)
    private String gradeName; // 등급명 (예: 일반실, 우등실, VIP실)

    @Column(nullable = false)
    private Integer basePrice; // 기본 운임 요금 (원)
}
