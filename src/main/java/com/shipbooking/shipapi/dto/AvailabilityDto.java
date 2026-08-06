package com.shipbooking.shipapi.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

public class AvailabilityDto {

    /**
     * 예약 가능 선박 조회 응답 DTO
     */
    @Data
    @Builder
    public static class ShipResponse {
        private Long scheduleId;       // 예약 시 필요한 운항 일정 ID
        private Long shipId;           // 선박 ID
        private String companyName;    // 선사명 (예: 대저해운)
        private String companyTel;     // 선사 전화번호 (예: 1899-8114)
        private String shipName;       // 선박명 (예: 썬플라워호)
        private String departurePort;  // 출발항 (예: 포항)
        private String arrivalPort;    // 도착항 (예: 울릉도(저동))
        private String departureTime;  // 출발 시간 (HH:mm 형식, 예: 09:50)
        private String arrivalTime;    // 도착 시간 (HH:mm 형식, 예: 13:20)
        private Integer totalAvailableSeats; // 전체 잔여 좌석 합계
        private List<SeatInfo> seats;  // 등급별 좌석 상세 정보
    }

    /**
     * 등급별 좌석 정보 DTO
     */
    @Data
    @Builder
    public static class SeatInfo {
        private Long seatGradeId;      // 좌석 등급 ID (예매 시 필요)
        private String gradeName;      // 등급명 (예: 일반실, 우등실, VIP실)
        private Integer price;         // 1인당 요금 (원)
        private Integer availableSeats; // 잔여 좌석 수
        private Integer totalSeats;    // 전체 좌석 수
    }
}
