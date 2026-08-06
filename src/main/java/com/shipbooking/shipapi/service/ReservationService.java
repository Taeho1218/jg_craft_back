package com.shipbooking.shipapi.service;

import com.shipbooking.shipapi.dto.ReservationDto;
import com.shipbooking.shipapi.entity.*;
import com.shipbooking.shipapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PassengerRepository passengerRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    /**
     * [예매 신청 및 결제 로직]
     * 1. 운항 일정 확인 및 잔여 좌석 차감
     * 2. 예약 정보(Reservation) 생성
     * 3. 승선객 명단(Passenger) 개별 저장 및 승선권 번호 발급
     * 4. 결제(Payment) 객체 생성 및 연동
     */
    @Transactional
    public ReservationDto.Response createReservation(ReservationDto.CreateRequest request) {
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운항 일정입니다: " + request.getScheduleId()));

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        // 등급별 요청 수량 집계 및 잔여 좌석 확인/차감
        Map<Long, Integer> requestedSeats = new HashMap<>();
        int calculatedTotalPrice = 0;

        for (ReservationDto.PassengerRequest passReq : request.getPassengers()) {
            requestedSeats.put(passReq.getSeatGradeId(), requestedSeats.getOrDefault(passReq.getSeatGradeId(), 0) + 1);
        }

        for (Map.Entry<Long, Integer> entry : requestedSeats.entrySet()) {
            Long seatGradeId = entry.getKey();
            Integer count = entry.getValue();

            ScheduleSeat scheduleSeat = scheduleSeatRepository.findByScheduleIdAndSeatGradeId(schedule.getId(), seatGradeId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 운항 일정의 좌석 등급 정보를 찾을 수 없습니다."));

            scheduleSeat.decreaseAvailableSeats(count);
            calculatedTotalPrice += scheduleSeat.getPrice() * count;
        }

        // 고유 예약 번호 생성 (BK-YYYYMMDD-Random 4자리)
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String bookingNumber = "BK-" + datePrefix + "-" + String.format("%04d", new Random().nextInt(10000));

        Reservation reservation = Reservation.builder()
                .bookingNumber(bookingNumber)
                .user(user)
                .bookerName(request.getBookerName())
                .bookerPhone(request.getBookerPhone())
                .schedule(schedule)
                .passengerCount(request.getPassengers().size())
                .totalPrice(calculatedTotalPrice)
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 승선객 명단 생성 및 티켓 번호 발급
        List<Passenger> savedPassengers = new ArrayList<>();
        int index = 1;
        for (ReservationDto.PassengerRequest passReq : request.getPassengers()) {
            SeatGrade seatGrade = seatGradeRepository.findById(passReq.getSeatGradeId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석 등급입니다."));

            String ticketNumber = "TK-" + datePrefix + "-" + String.format("%04d", new Random().nextInt(10000)) + "-" + index++;

            Passenger passenger = Passenger.builder()
                    .reservation(savedReservation)
                    .seatGrade(seatGrade)
                    .name(passReq.getName())
                    .birthDate(passReq.getBirthDate())
                    .gender(passReq.getGender())
                    .nationality(passReq.getNationality() != null ? passReq.getNationality() : "KOR")
                    .phone(passReq.getPhone())
                    .ticketNumber(ticketNumber)
                    .build();

            savedPassengers.add(passengerRepository.save(passenger));
        }

        // 결제 정보 생성
        Payment payment = Payment.builder()
                .reservation(savedReservation)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD")
                .amount(calculatedTotalPrice)
                .status(Payment.PaymentStatus.COMPLETED)
                .build();
        paymentRepository.save(payment);

        log.info("[예매 성공] 예약번호: {}, 예약자: {}, 인원: {}명, 총금액: {}원",
                savedReservation.getBookingNumber(), savedReservation.getBookerName(), savedReservation.getPassengerCount(), savedReservation.getTotalPrice());

        return convertToDto(savedReservation, savedPassengers);
    }

    /**
     * [예약 번호 + 전화번호로 비회원/회원 예약 단건 조회]
     */
    @Transactional(readOnly = true)
    public ReservationDto.Response lookupReservation(String bookingNumber, String bookerPhone) {
        Reservation reservation = reservationRepository.findByBookingNumberAndBookerPhone(bookingNumber, bookerPhone)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 예약 정보를 찾을 수 없습니다. (예약번호/연락처 확인)"));

        List<Passenger> passengers = passengerRepository.findByReservationId(reservation.getId());
        return convertToDto(reservation, passengers);
    }

    /**
     * [특정 회원의 전체 예매 내역 조회]
     */
    @Transactional(readOnly = true)
    public List<ReservationDto.Response> getUserReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream()
                .map(res -> convertToDto(res, passengerRepository.findByReservationId(res.getId())))
                .collect(Collectors.toList());
    }

    /**
     * [예약 취소 및 좌석 수량 복구]
     */
    @Transactional
    public ReservationDto.Response cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다: " + reservationId));

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);

        // 승선객 좌석 복구
        List<Passenger> passengers = passengerRepository.findByReservationId(reservation.getId());
        for (Passenger passenger : passengers) {
            ScheduleSeat scheduleSeat = scheduleSeatRepository.findByScheduleIdAndSeatGradeId(
                    reservation.getSchedule().getId(), passenger.getSeatGrade().getId()).orElse(null);
            if (scheduleSeat != null) {
                scheduleSeat.increaseAvailableSeats(1);
            }
        }

        log.info("[예약 취소 완료] 예약번호: {}", reservation.getBookingNumber());
        return convertToDto(reservation, passengers);
    }

    private ReservationDto.Response convertToDto(Reservation reservation, List<Passenger> passengers) {
        List<ReservationDto.PassengerResponse> passengerResponses = passengers.stream()
                .map(p -> ReservationDto.PassengerResponse.builder()
                        .id(p.getId())
                        .seatGradeName(p.getSeatGrade().getGradeName())
                        .name(p.getName())
                        .birthDate(p.getBirthDate())
                        .gender(p.getGender())
                        .nationality(p.getNationality())
                        .phone(p.getPhone())
                        .ticketNumber(p.getTicketNumber())
                        .build())
                .collect(Collectors.toList());

        return ReservationDto.Response.builder()
                .id(reservation.getId())
                .bookingNumber(reservation.getBookingNumber())
                .bookerName(reservation.getBookerName())
                .bookerPhone(reservation.getBookerPhone())
                .companyName(reservation.getSchedule().getShip().getCompany().getName())
                .shipName(reservation.getSchedule().getShip().getName())
                .departurePort(reservation.getSchedule().getDeparturePort())
                .arrivalPort(reservation.getSchedule().getArrivalPort())
                .departureTime(reservation.getSchedule().getDepartureTime())
                .passengerCount(reservation.getPassengerCount())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus().name())
                .reservedAt(reservation.getReservedAt())
                .passengers(passengerResponses)
                .build();
    }
}
