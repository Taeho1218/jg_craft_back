package com.shipbooking.shipapi.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleDto {

    /**
     * 운항 일정 조회 응답 DTO
     */
    @Data
    @Builder
    public static class Response {
        private Long id;                  // 운항 일정 ID
        private String companyName;       // 선사명 (예: 대저해운)
        private String shipName;          // 선박명 (예: 썬플라워호)
        private String departurePort;     // 출발항 (예: 포항)
        private String arrivalPort;       // 도착항 (예: 울릉도(저동))
        private LocalDateTime departureTime; // 출발 일시
        private LocalDateTime arrivalTime;   // 도착 일시
        private String status;            // 운항 상태 (SCHEDULED 등)
        private List<SeatInventory> seats; // 등급별 잔여 좌석 및 요금
    }

    /**
     * 등급별 좌석 재고 및 요금 DTO
     */
    @Data
    @Builder
    public static class SeatInventory {
        private Long seatGradeId;   // 좌석 등급 ID
        private String gradeName;   // 등급명 (예: 일반실, 우등실, VIP실)
        private Integer price;      // 요금 (원)
        private Integer availableSeats; // 잔여 좌석 수
        private Integer totalSeats; // 전체 좌석 수
    }
}
