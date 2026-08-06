package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [동행자 엔티티]
 * 회원(member_id)이 동행자로 등록한 타인/가족 승선자 정보
 */
@Entity
@Table(name = "companion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Companion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "companion_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 동행자를 등록한 대표 회원의 번호 (FK)

    @Column(name = "companion_name", nullable = false, length = 50)
    private String companionName; // 동행자 이름

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate; // 동행자 생년월일 (YYYY-MM-DD)

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender; // 동행자 성별 (MALE, FEMALE)

    @Column(name = "nationality", nullable = false, length = 50)
    private String nationality; // 동행자 국적

    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // 동행자 휴대폰 번호 (선택)

    @Column(name = "emergency_contact", length = 20)
    private String emergencyContact; // 비상 연락처 (선택)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 동행자 등록 일시

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 동행자 정보 수정 일시

    public enum Gender {
        MALE, FEMALE
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
