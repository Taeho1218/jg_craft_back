package com.shipbooking.shipapi.service;

import com.shipbooking.shipapi.dto.AvailabilityDto;
import com.shipbooking.shipapi.entity.Schedule;
import com.shipbooking.shipapi.entity.ScheduleSeat;
import com.shipbooking.shipapi.entity.SeatGrade;
import com.shipbooking.shipapi.repository.ScheduleRepository;
import com.shipbooking.shipapi.repository.ScheduleSeatRepository;
import com.shipbooking.shipapi.repository.SeatGradeRepository;
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
    private final SeatGradeRepository seatGradeRepository;

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
                .filter(s -> matchPort(s.getDeparturePort(), departurePort))
                .filter(s -> matchPort(s.getArrivalPort(), arrivalPort))
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
                .filter(s -> matchPort(s.getDeparturePort(), departurePort))
                .filter(s -> matchPort(s.getArrivalPort(), arrivalPort))
                .filter(s -> s.getStatus() == Schedule.ScheduleStatus.SCHEDULED)
                .filter(s -> !s.getDepartureTime().isBefore(start) && !s.getDepartureTime().isAfter(end))
                .filter(s -> getTotalAvailableSeats(s.getId()) >= personCount)
                .map(this::convertToShipResponse)
                .collect(Collectors.toList());

        log.info("[예약 가능 선박 조회] 출발항={}, 도착항={}, 날짜={}, 인원={}, 결과 선박 수={}", departurePort, arrivalPort, date, personCount, ships.size());
        return ships;
    }

    private boolean matchPort(String dbPort, String reqPort) {
        if (dbPort == null || reqPort == null) return false;
        String cleanDb = dbPort.trim();
        String cleanReq = reqPort.trim();

        if (cleanDb.equalsIgnoreCase(cleanReq)) return true;

        // 항구명에 '울릉', '도동', '저동', '사동'이 포함되어 있으면 울릉도 항구 상호 매칭
        boolean isDbUlleung = cleanDb.contains("도동") || cleanDb.contains("저동") || cleanDb.contains("사동") || cleanDb.contains("울릉");
        boolean isReqUlleung = cleanReq.contains("도동") || cleanReq.contains("저동") || cleanReq.contains("사동") || cleanReq.contains("울릉");
        if (isDbUlleung && isReqUlleung) {
            return true;
        }

        if (cleanDb.contains("묵호") && cleanReq.contains("묵호")) return true;
        if (cleanDb.contains("포항") && cleanReq.contains("포항")) return true;
        if (cleanDb.contains("독도") && cleanReq.contains("독도")) return true;

        String normDb = cleanDb.replaceAll("[()\\s_항]", "");
        String normReq = cleanReq.replaceAll("[()\\s_항]", "");
        return normDb.contains(normReq) || normReq.contains(normDb);
    }

    /**
     * [보조 메서드]
     * 특정 운항 일정의 전체 잔여 좌석 수를 모든 등급의 합으로 계산 (필터링용)
     */
    private int getTotalAvailableSeats(Long scheduleId) {
        List<ScheduleSeat> seats = scheduleSeatRepository.findByScheduleId(scheduleId);
        if (seats.isEmpty()) {
            return 250; // schedule_seats 데이터가 DB에 미등록된 경우 기본 250석 제공
        }
        return seats.stream()
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
        List<AvailabilityDto.SeatInfo> seatInfos;
        int totalAvailableSeats;

        if (seats.isEmpty()) {
            Long shipId = (schedule.getShip() != null) ? schedule.getShip().getId() : 1L;
            List<SeatGrade> dbSeatGrades = seatGradeRepository.findByShipId(shipId);

            if (!dbSeatGrades.isEmpty()) {
                seatInfos = dbSeatGrades.stream().map(grade -> {
                    int price = (grade.getBasePrice() != null) ? grade.getBasePrice() : getDefaultPrice(grade.getGradeName());
                    int capacity = (grade.getSeatCapacity() != null) ? grade.getSeatCapacity() : 100;
                    return AvailabilityDto.SeatInfo.builder()
                            .seatGradeId(grade.getId())
                            .gradeName(grade.getGradeName())
                            .price(price)
                            .availableSeats(capacity)
                            .totalSeats(capacity)
                            .build();
                }).collect(Collectors.toList());
                totalAvailableSeats = seatInfos.stream().mapToInt(AvailabilityDto.SeatInfo::getAvailableSeats).sum();
            } else {
                if (shipId != null && shipId == 2L) {
                    AvailabilityDto.SeatInfo eco = AvailabilityDto.SeatInfo.builder().seatGradeId(3L).gradeName("이코노미").price(81000).availableSeats(700).totalSeats(700).build();
                    AvailabilityDto.SeatInfo biz = AvailabilityDto.SeatInfo.builder().seatGradeId(4L).gradeName("비즈니스").price(121500).availableSeats(200).totalSeats(200).build();
                    AvailabilityDto.SeatInfo fst = AvailabilityDto.SeatInfo.builder().seatGradeId(5L).gradeName("퍼스트").price(171500).availableSeats(70).totalSeats(70).build();
                    seatInfos = List.of(eco, biz, fst);
                    totalAvailableSeats = 970;
                } else {
                    AvailabilityDto.SeatInfo f1 = AvailabilityDto.SeatInfo.builder().seatGradeId(1L).gradeName("1층").price(65500).availableSeats(300).totalSeats(300).build();
                    AvailabilityDto.SeatInfo f2 = AvailabilityDto.SeatInfo.builder().seatGradeId(2L).gradeName("2층").price(82500).availableSeats(142).totalSeats(142).build();
                    seatInfos = List.of(f1, f2);
                    totalAvailableSeats = 442;
                }
            }
        } else {
            seatInfos = seats.stream()
                    .map(seat -> AvailabilityDto.SeatInfo.builder()
                            .seatGradeId(seat.getSeatGrade() != null ? seat.getSeatGrade().getId() : 1L)
                            .gradeName(seat.getSeatGrade() != null ? seat.getSeatGrade().getGradeName() : "일반석")
                            .price(seat.getPrice() != null ? seat.getPrice() : getDefaultPrice(seat.getSeatGrade() != null ? seat.getSeatGrade().getGradeName() : ""))
                            .availableSeats(seat.getAvailableSeats())
                            .totalSeats(seat.getTotalSeats())
                            .build())
                    .collect(Collectors.toList());
            totalAvailableSeats = seats.stream().mapToInt(ScheduleSeat::getAvailableSeats).sum();
        }

        String companyName = (schedule.getShip() != null && schedule.getShip().getCompany() != null) 
                ? schedule.getShip().getCompany().getName() : "씨스포빌";
        String companyTel = (schedule.getShip() != null && schedule.getShip().getCompany() != null) 
                ? schedule.getShip().getCompany().getTel() : "1577-8667";

        return AvailabilityDto.ShipResponse.builder()
                .scheduleId(schedule.getId())
                .shipId(schedule.getShip() != null ? schedule.getShip().getId() : 1L)
                .companyName(companyName)
                .companyTel(companyTel)
                .shipName(schedule.getShip() != null ? schedule.getShip().getName() : "씨스타1호")
                .departurePort(schedule.getDeparturePort())
                .arrivalPort(schedule.getArrivalPort())
                .departureTime(schedule.getDepartureTime() != null ? schedule.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "08:20")
                .arrivalTime(schedule.getArrivalTime() != null ? schedule.getArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "11:50")
                .totalAvailableSeats(totalAvailableSeats)
                .seats(seatInfos)
                .build();
    }

    private int getDefaultPrice(String gradeName) {
        if (gradeName == null) return 65000;
        if (gradeName.contains("1층")) return 65500;
        if (gradeName.contains("2층")) return 82500;
        if (gradeName.contains("이코노미")) return 81000;
        if (gradeName.contains("비즈니스")) return 121500;
        if (gradeName.contains("퍼스트")) return 171500;
        if (gradeName.contains("우등")) return 85000;
        return 65000;
    }
}
