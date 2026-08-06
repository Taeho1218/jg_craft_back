package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [사용자/회원 엔티티 - EC2 members 테이블 매핑]
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id; // 회원 고유 번호 (PK)

    @Column(name = "login_id", length = 50)
    private String loginId; // 로그인 ID

    @Column(name = "password_hash", length = 255)
    private String password; // 비밀번호

    @Column(name = "member_name", nullable = false, length = 50)
    private String name; // 회원 이름

    @Column(name = "birth_date", length = 10)
    private String birthDate; // 생년월일 (YYYY-MM-DD)

    @Column(name = "gender", length = 10)
    private String gender; // 성별 (남성 / 여성 / M / F)

    @Column(name = "phone", nullable = false, length = 20)
    private String phone; // 휴대폰 번호 (로그인 ID 겸용)

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone; // 비상 연락처

    @Column(name = "nationality", length = 50)
    private String nationality; // 국적

    @Column(name = "email", length = 100)
    private String email; // 이메일

    @Column(name = "withdrawal_status", length = 10)
    @Builder.Default
    private String withdrawalStatus = "가입"; // 가입 / 탈퇴

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER; // 권한 (USER: 일반회원, ADMIN: 관리자)

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false; // 탈퇴 여부 (true: 탈퇴 처리됨)

    @Column(name = "joined_at", updatable = false)
    private LocalDateTime createdAt; // 가입 일시

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt; // 탈퇴 일시

    public enum Role {
        USER, ADMIN
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = Role.USER;
        }
        if (this.withdrawalStatus == null) {
            this.withdrawalStatus = "가입";
        }
        if (this.loginId == null) {
            this.loginId = this.phone;
        }
    }
}
