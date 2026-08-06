package com.shipbooking.shipapi.controller;

import com.shipbooking.shipapi.dto.ScheduleDto;
import com.shipbooking.shipapi.service.ScheduleService;
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
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * [운항 일정 및 잔여 좌석 조회 API]
     * 예: GET /api/schedules?departurePort=포항&arrivalPort=울릉도(저동)&date=2026-08-10
     */
    @GetMapping
    public ResponseEntity<List<ScheduleDto.Response>> getSchedules(
            @RequestParam(required = false) String departurePort,
            @RequestParam(required = false) String arrivalPort,
            @RequestParam(required = false) String date
    ) {
        log.info("[운항 API] 일정 검색 요청 - 출발: {}, 도착: {}, 날짜: {}", departurePort, arrivalPort, date);
        if (departurePort != null && arrivalPort != null) {
            return ResponseEntity.ok(scheduleService.searchSchedules(departurePort, arrivalPort, date));
        } else {
            return ResponseEntity.ok(scheduleService.getAllSchedules());
        }
    }
}
