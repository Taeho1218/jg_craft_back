package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
