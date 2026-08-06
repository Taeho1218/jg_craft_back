package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [선박 엔티티]
 * 선사가 보유한 배 정보 (예: 썬플라워호, 씨스타5호 등)
 */
@Entity
@Table(name = "ships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // 소유 선사

    @Column(nullable = false, length = 100)
    private String name; // 선박명 (예: 썬플라워호)

    @Column(nullable = false)
    private Integer capacity; // 승선 총 정원 (예: 443)
}
