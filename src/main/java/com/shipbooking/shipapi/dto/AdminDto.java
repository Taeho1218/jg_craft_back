package com.shipbooking.shipapi.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDto {

    /**
     * 선사에 전달할 승선객 명단 출력 응답 DTO
     */
    @Data
    @Builder
    public static class ManifestResponse {
        private Long scheduleId;              // 운항 일정 ID
        private String companyName;           // 선사명
        private String shipName;              // 선박명
        private String departurePort;         // 출발항
        private String arrivalPort;           // 도착항
        private LocalDateTime departureTime;  // 출발 일시
        private Integer totalPassengers;      // 총 승선객 수
        private List<PassengerDetail> passengers; // 승선객 상세 목록 (선사 전달용)
    }

    /**
     * 선사 제출용 개별 승선객 데이터
     */
    @Data
    @Builder
    public static class PassengerDetail {
        private String ticketNumber; // 승선권 번호
        private String bookingNumber;// 예매 번호
        private String name;         // 이름
        private String birthDate;    // 생년월일
        private String gender;       // 성별
        private String nationality;  // 국적
        private String phone;        // 비상 연락처
        private String gradeName;    // 등급 (일반실 등)
        private String bookerName;   // 예매자
        private String bookerPhone;  // 예매자 연락처
    }
}
