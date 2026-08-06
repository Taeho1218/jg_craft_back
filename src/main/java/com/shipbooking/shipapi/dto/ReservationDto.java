package com.shipbooking.shipapi.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationDto {

    /**
     * 예매 신청 요청 DTO
     */
    @Data
    public static class CreateRequest {
        private Long scheduleId;          // 운항 일정 ID
        private Long userId;              // 회원 ID (비회원은 null)
        private String bookerName;        // 예약자 이름
        private String bookerPhone;       // 예약자 연락처
        private List<PassengerRequest> passengers; // 승선객 명단 목록
    }

    /**
     * 승선객 개별 정보 요청 DTO
     */
    @Data
    public static class PassengerRequest {
        private Long seatGradeId;  // 선택한 좌석 등급 ID
        private String name;         // 승선객 이름
        private String birthDate;    // 생년월일 (YYYY-MM-DD)
        private String gender;       // 성별 (M / F)
        private String nationality;  // 국적 (KOR 등)
        private String phone;        // 승선객 연락처
    }

    /**
     * 예매 조회 응답 DTO
     */
    @Data
    @Builder
    public static class Response {
        private Long id;                     // 예약 DB ID
        private String bookingNumber;        // 예약 번호 (예: BK-20260806-9821)
        private String bookerName;           // 예약자 이름
        private String bookerPhone;          // 예약자 전화번호
        private String companyName;          // 선사명
        private String shipName;             // 선박명
        private String departurePort;        // 출발항
        private String arrivalPort;          // 도착항
        private LocalDateTime departureTime;    // 출발 일시
        private Integer passengerCount;      // 총 승선 인원 수
        private Integer totalPrice;          // 총 결제 금액
        private String status;               // 예약 상태 (CONFIRMED, CANCELLED)
        private LocalDateTime reservedAt;       // 예매 일시
        private List<PassengerResponse> passengers; // 승선객 상세 목록
    }

    /**
     * 승선객 응답 DTO
     */
    @Data
    @Builder
    public static class PassengerResponse {
        private Long id;             // 승선객 ID
        private String seatGradeName; // 좌석 등급명
        private String name;         // 이름
        private String birthDate;    // 생년월일
        private String gender;       // 성별
        private String nationality;  // 국적
        private String phone;        // 연락처
        private String ticketNumber; // 발급된 승선권 번호
    }
}
