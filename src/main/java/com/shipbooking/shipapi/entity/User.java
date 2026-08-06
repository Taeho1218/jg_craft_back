package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [사용자/회원 엔티티]
 * 회원 정보 및 권한 (USER / ADMIN)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String phone; // 회원 로그인 ID (전화번호)

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String gender; // 성별 (M / F)

    @Column(length = 30)
    private String nationality; // 국적 (내국인: KOR 등)

    @Column(length = 20)
    private String emergencyPhone; // 비상 연락처

    @Column(length = 10)
    private String birthDate; // 생년월일 (YYYY-MM-DD)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER; // 권한 (USER: 일반회원, ADMIN: 관리자)

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false; // 탈퇴 여부 (true: 탈퇴 처리됨)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = Role.USER;
        }
    }

    public enum Role {
        USER,  // 일반 고객
        ADMIN  // 관리자 (배표 재고/운항/명단 관리)
    }
}
