package com.shipbooking.shipapi.controller;

import com.shipbooking.shipapi.dto.AvailabilityDto;
import com.shipbooking.shipapi.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * [예약 가능 날짜 조회 API]
     * HTTP Method: GET
     *
     * @param departurePort 출발항 (예: 포항, 묵호)
     * @param arrivalPort   도착항 (예: 울릉도(저동), 울릉도(사동))
     * @param personCount   탑승 인원 수
     * @return 예약 가능한 날짜 목록 (YYYY-MM-DD 문자열 배열)
     */
    @GetMapping("/available-dates")
    public ResponseEntity<List<String>> getAvailableDates(
            @RequestParam String departurePort,
            @RequestParam String arrivalPort,
            @RequestParam int personCount) {
        log.info("[가용성 API] 예약 가능 날짜 조회 - 출발항={}, 도착항={}, 인원={}", departurePort, arrivalPort, personCount);

        // 서비스 계층의 getAvailableDates 메서드를 호출하여 예약 가능한 날짜 목록 조회
        List<String> response = availabilityService.getAvailableDates(departurePort, arrivalPort, personCount);

        // HTTP 상태 코드 200(OK)과 함께 날짜 목록 반환
        return ResponseEntity.ok(response);
    }

    /**
     * [예약 가능 선박 조회 API]
     * HTTP Method: GET
     *
     * @param departurePort 출발항 (예: 포항, 묵호)
     * @param arrivalPort   도착항 (예: 울릉도(저동), 울릉도(사동))
     * @param date          조회 날짜 (YYYY-MM-DD 형식)
     * @param personCount   탑승 인원 수
     * @return 예약 가능한 선박 목록 (선사, 선박, 출발/도착 시간, 등급별 좌석 정보)
     */
    @GetMapping("/search")
    public ResponseEntity<List<AvailabilityDto.ShipResponse>> getAvailableShips(
            @RequestParam String departurePort,
            @RequestParam String arrivalPort,
            @RequestParam String date,
            @RequestParam int personCount) {
        log.info("[가용성 API] 예약 가능 선박 조회 - 출발항={}, 도착항={}, 날짜={}, 인원={}", departurePort, arrivalPort, date, personCount);

        // 서비스 계층의 getAvailableShips 메서드를 호출하여 예약 가능한 선박 및 좌석 정보 조회
        List<AvailabilityDto.ShipResponse> response = availabilityService.getAvailableShips(departurePort, arrivalPort, personCount, date);

        // HTTP 상태 코드 200(OK)과 함께 선박 목록 반환
        return ResponseEntity.ok(response);
    }
}
