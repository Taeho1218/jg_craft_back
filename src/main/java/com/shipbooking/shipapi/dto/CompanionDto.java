package com.shipbooking.shipapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shipbooking.shipapi.entity.Companion;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [동행자 데이터 전송 객체 (DTO)]
 * 동행자 정보 생성, 수정 요청 및 응답 포맷 정의
 */
public class CompanionDto {

    /**
     * [동행자 정보 등록 요청 DTO]
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long memberId; // 동행자를 등록한 대표 회원 번호 (FK)
        private String companionName; // 동행자 이름

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate; // 동행자 생년월일

        private Companion.Gender gender; // 동행자 성별 (MALE, FEMALE)
        private String nationality; // 동행자 국적 (예: 대한민국, USA 등)
        private String phoneNumber; // 동행자 휴대폰 번호 (선택)
        private String emergencyContact; // 비상 연락처 (선택)
    }

    /**
     * [다중 동행자 일괄 등록 요청 DTO]
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchCreateRequest {
        private Long memberId; // 대표 회원 번호 (옵션: 모든 동행자에게 일괄 적용)
        private java.util.List<CreateRequest> companions; // 동행자 정보 리스트
    }

    /**
     * [동행자 정보 수정 요청 DTO]
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String companionName; // 동행자 이름

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate; // 동행자 생년월일

        private Companion.Gender gender; // 동행자 성별 (MALE, FEMALE)
        private String nationality; // 동행자 국적
        private String phoneNumber; // 동행자 휴대폰 번호
        private String emergencyContact; // 비상 연락처
    }

    /**
     * [동행자 정보 응답 DTO]
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long companionId;
        private Long memberId;
        private String companionName;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

        private Companion.Gender gender;
        private String nationality;
        private String phoneNumber;
        private String emergencyContact;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
    }
}
