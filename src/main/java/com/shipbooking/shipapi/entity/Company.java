package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [선사 엔티티 - EC2 shipping_companies 테이블 매핑]
 */
@Entity
@Table(name = "shipping_companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id; // 선사 PK ID

    @Column(name = "company_name", nullable = false, length = 100)
    private String name; // 선사명 (예: 대저해운, 씨스포빌)

    @Column(name = "phone", length = 20)
    private String tel; // 고객센터 전화번호

    @Column(name = "website_url", length = 255)
    private String websiteUrl; // 웹사이트 URL

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
