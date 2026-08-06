package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [선박 엔티티 - EC2 ships 테이블 매핑]
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
    @Column(name = "ship_id")
    private Long id; // 선박 PK ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // 소유 선사

    @Column(name = "ship_name", nullable = false, length = 100)
    private String name; // 선박명 (예: 씨스타1호, 엘도라도 익스프레스호)

    @Column(name = "total_capacity", nullable = false)
    private Integer capacity; // 승선 총 정원

    @Column(name = "ship_status", length = 20)
    @Builder.Default
    private String shipStatus = "운항가능"; // 운항가능, 정비중, 운항중단

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.shipStatus == null) {
            this.shipStatus = "운항가능";
        }
    }
}
