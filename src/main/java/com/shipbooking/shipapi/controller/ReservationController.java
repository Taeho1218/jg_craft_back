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
     * [예매 신청 API]
     * HTTP Method: POST
     *
     * @param request 운항일정ID, 회원ID, 예약자 정보, 승선객 명단, 결제 수단
     * @return 예매 완료 정보 (예약번호, 승선권 목록 포함)
     */
    @PostMapping
    public ResponseEntity<ReservationDto.Response> createReservation(
            @RequestBody ReservationDto.CreateRequest request) {
        log.info("[예매 API] 예매 신청 - 회원ID={}, 운항일정ID={}, 인원={}명",
                request.getUserId(), request.getScheduleId(), request.getPassengers().size());

        // 서비스 계층의 createReservation 메서드를 호출하여 예매 신청 및 결제 처리 수행
        ReservationDto.Response response = reservationService.createReservation(request);

        // HTTP 상태 코드 200(OK)과 함께 예매 완료 정보 반환
        return ResponseEntity.ok(response);
    }

    /**
     * [회원 예매 내역 조회 API]
     * HTTP Method: GET
     *
     * @param userId 회원 ID
     * @return 해당 회원의 전체 예매 내역 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDto.Response>> getUserReservations(
            @PathVariable Long userId) {
        log.info("[예매 API] 예매 내역 조회 - 회원ID={}", userId);

        // 서비스 계층의 getUserReservations 메서드를 호출하여 회원의 전체 예매 내역 조회
        List<ReservationDto.Response> response = reservationService.getUserReservations(userId);

        // HTTP 상태 코드 200(OK)과 함께 예매 내역 목록 반환
        return ResponseEntity.ok(response);
    }

    /**
     * [예매 취소 API]
     * HTTP Method: PATCH
     *
     * @param id 취소할 예약 ID
     * @return 취소 처리된 예약 정보 (status: CANCELLED)
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationDto.Response> cancelReservation(
            @PathVariable Long id) {
        log.info("[예매 API] 예매 취소 요청 - 예약ID={}", id);

        // 서비스 계층의 cancelReservation 메서드를 호출하여 예매 취소 및 좌석 복구 처리
        ReservationDto.Response response = reservationService.cancelReservation(id);

        // HTTP 상태 코드 200(OK)과 함께 취소된 예약 정보 반환
        return ResponseEntity.ok(response);
    }
}
