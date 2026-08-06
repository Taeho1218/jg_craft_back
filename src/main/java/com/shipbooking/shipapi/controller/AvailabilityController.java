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
        return ResponseEntity.ok(availabilityService.getAvailableDates(departurePort, arrivalPort, personCount));
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
        return ResponseEntity.ok(availabilityService.getAvailableShips(departurePort, arrivalPort, personCount, date));
    }
}
