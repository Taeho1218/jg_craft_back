package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [선사 엔티티]
 * 여객선을 운항하는 해운 회사 정보 (예: 대저해운, 씨스포빌 등)
 */
@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 선사명 (예: 대저해운)

    @Column(length = 20)
    private String tel; // 고객센터 전화번호 (예: 1899-8114)
}
