package com.shipbooking.shipapi.service;

import com.shipbooking.shipapi.dto.AvailabilityDto;
import com.shipbooking.shipapi.entity.Schedule;
import com.shipbooking.shipapi.entity.ScheduleSeat;
import com.shipbooking.shipapi.repository.ScheduleRepository;
import com.shipbooking.shipapi.repository.ScheduleSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;

    /**
     * [예약 가능 날짜 조회]
     * 출발항, 도착항, 인원 수를 입력하면 예약 가능한 날짜 목록을 반환합니다.
     * 1. 출발항(departurePort) + 도착항(arrivalPort)이 일치하는 운항 일정 필터링
     * 2. 운항 상태가 SCHEDULED이고, 출발일이 오늘 이후인 일정만 포함
     * 3. 해당 일정의 전체 잔여 좌석 합계가 인원 수 이상인 경우만 포함
     * 4. 날짜만 추출하여 중복 제거 후 오름차순 정렬
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableDates(String departurePort, String arrivalPort, int personCount) {
        if (departurePort == null || departurePort.isBlank()) {
            throw new IllegalArgumentException("출발항을 입력해 주세요.");
        }
        if (arrivalPort == null || arrivalPort.isBlank()) {
            throw new IllegalArgumentException("도착항을 입력해 주세요.");
        }
        if (personCount < 1) {
            throw new IllegalArgumentException("인원 수는 1명 이상이어야 합니다.");
        }

        List<String> dates = scheduleRepository.findAll().stream()
                .filter(s -> s.getDeparturePort().equals(departurePort))
                .filter(s -> s.getArrivalPort().equals(arrivalPort))
                .filter(s -> s.getStatus() == Schedule.ScheduleStatus.SCHEDULED)
                .filter(s -> s.getDepartureTime().isAfter(LocalDateTime.now()))
                .filter(s -> getTotalAvailableSeats(s.getId()) >= personCount)
                .map(s -> s.getDepartureTime().toLocalDate().toString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        log.info("[예약 가능 날짜 조회] 출발항={}, 도착항={}, 인원={}, 결과 날짜 수={}", departurePort, arrivalPort, personCount, dates.size());
        return dates;
    }

    /**
     * [예약 가능 선박 조회]
     * 출발항, 도착항, 날짜, 인원 수를 입력하면 해당 조건에 맞는 선박 목록을 반환합니다.
     * 1. 출발항 + 도착항 + 날짜 범위(당일 00:00 ~ 23:59)로 일정 필터링
     * 2. 운항 상태가 SCHEDULED인 일정만 포함
     * 3. 해당 일정의 전체 잔여 좌석 합계가 인원 수 이상인 경우만 포함
     * 4. 선박 정보, 출발/도착 시간, 등급별 좌석 정보를 응답으로 반환
     */
    @Transactional(readOnly = true)
    public List<AvailabilityDto.ShipResponse> getAvailableShips(String departurePort, String arrivalPort, int personCount, String date) {
        if (departurePort == null || departurePort.isBlank()) {
            throw new IllegalArgumentException("출발항을 입력해 주세요.");
        }
        if (arrivalPort == null || arrivalPort.isBlank()) {
            throw new IllegalArgumentException("도착항을 입력해 주세요.");
        }
        if (personCount < 1) {
            throw new IllegalArgumentException("인원 수는 1명 이상이어야 합니다.");
        }
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("날짜를 입력해 주세요.");
        }

        LocalDate searchDate = LocalDate.parse(date);
        LocalDateTime start = searchDate.atStartOfDay();
        LocalDateTime end = searchDate.atTime(LocalTime.MAX);

        List<AvailabilityDto.ShipResponse> ships = scheduleRepository.findAll().stream()
                .filter(s -> s.getDeparturePort().equals(departurePort))
                .filter(s -> s.getArrivalPort().equals(arrivalPort))
                .filter(s -> s.getStatus() == Schedule.ScheduleStatus.SCHEDULED)
                .filter(s -> !s.getDepartureTime().isBefore(start) && !s.getDepartureTime().isAfter(end))
                .filter(s -> getTotalAvailableSeats(s.getId()) >= personCount)
                .map(this::convertToShipResponse)
                .collect(Collectors.toList());

        log.info("[예약 가능 선박 조회] 출발항={}, 도착항={}, 날짜={}, 인원={}, 결과 선박 수={}", departurePort, arrivalPort, date, personCount, ships.size());
        return ships;
    }

    /**
     * [보조 메서드]
     * 특정 운항 일정의 전체 잔여 좌석 수를 모든 등급의 합으로 계산 (필터링용)
     */
    private int getTotalAvailableSeats(Long scheduleId) {
        return scheduleSeatRepository.findByScheduleId(scheduleId).stream()
                .mapToInt(ScheduleSeat::getAvailableSeats)
                .sum();
    }

    /**
     * [보조 메서드]
     * Schedule Entity → ShipResponse DTO 변환
     * 등급별 좌석 정보(SeatInfo)를 함께 조회하여 응답에 포함
     */
    private AvailabilityDto.ShipResponse convertToShipResponse(Schedule schedule) {
        List<ScheduleSeat> seats = scheduleSeatRepository.findByScheduleId(schedule.getId());

        List<AvailabilityDto.SeatInfo> seatInfos = seats.stream()
                .map(seat -> AvailabilityDto.SeatInfo.builder()
                        .seatGradeId(seat.getSeatGrade().getId())
                        .gradeName(seat.getSeatGrade().getGradeName())
                        .price(seat.getPrice())
                        .availableSeats(seat.getAvailableSeats())
                        .totalSeats(seat.getTotalSeats())
                        .build())
                .collect(Collectors.toList());

        int totalAvailableSeats = seats.stream().mapToInt(ScheduleSeat::getAvailableSeats).sum();

        return AvailabilityDto.ShipResponse.builder()
                .scheduleId(schedule.getId())
                .shipId(schedule.getShip().getId())
                .companyName(schedule.getShip().getCompany().getName())
                .companyTel(schedule.getShip().getCompany().getTel())
                .shipName(schedule.getShip().getName())
                .departurePort(schedule.getDeparturePort())
                .arrivalPort(schedule.getArrivalPort())
                .departureTime(schedule.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .arrivalTime(schedule.getArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .totalAvailableSeats(totalAvailableSeats)
                .seats(seatInfos)
                .build();
    }
}
