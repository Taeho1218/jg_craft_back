package com.shipbooking.shipapi.controller;

import com.shipbooking.shipapi.dto.ReservationDto;
import com.shipbooking.shipapi.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * [1. 예매 신청 및 승선객 등록 API]
     * POST /api/reservations
     */
    @PostMapping
    public ResponseEntity<ReservationDto.Response> createReservation(@RequestBody ReservationDto.CreateRequest request) {
        log.info("[예약 API] 예매 신청 - 예약자: {}, 인원: {}명", request.getBookerName(), request.getPassengers() != null ? request.getPassengers().size() : 0);
        ReservationDto.Response response = reservationService.createReservation(request);
        return ResponseEntity.ok(response);
    }

    /**
     * [2. 비회원/회원 예약 조회 API (예약번호 + 연락처)]
     * GET /api/reservations/lookup?bookingNumber=BK-20260806-1234&phone=01012345678
     */
    @GetMapping("/lookup")
    public ResponseEntity<ReservationDto.Response> lookupReservation(
            @RequestParam String bookingNumber,
            @RequestParam String phone
    ) {
        log.info("[예약 API] 예약 조회 - 예약번호: {}, 연락처: {}", bookingNumber, phone);
        ReservationDto.Response response = reservationService.lookupReservation(bookingNumber, phone);
        return ResponseEntity.ok(response);
    }

    /**
     * [3. 특정 회원의 전체 예매 목록 조회 API]
     * GET /api/reservations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDto.Response>> getUserReservations(@PathVariable Long userId) {
        log.info("[예약 API] 회원 예매 내역 조회 - 회원 ID: {}", userId);
        List<ReservationDto.Response> response = reservationService.getUserReservations(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * [4. 예매 취소 API]
     * POST /api/reservations/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationDto.Response> cancelReservation(@PathVariable Long id) {
        log.info("[예약 API] 예매 취소 요청 - 예약 ID: {}", id);
        ReservationDto.Response response = reservationService.cancelReservation(id);
        return ResponseEntity.ok(response);
    }
}
