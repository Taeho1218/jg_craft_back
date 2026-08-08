package com.shipbooking.shipapi.config;

import com.shipbooking.shipapi.entity.*;
import com.shipbooking.shipapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * [KSA DB 데이터 시딩 자동 초기화 객체]
 * KSA_DATABASE (01_schema.sql & 02_seed_data.sql)의 모든 선사, 선박, 좌석등급,
 * 2026년 8월 전 기간 운항일정 및 회원/동행자 데이터를 자동으로 DB에 초기화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final ShipRepository shipRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final UserRepository userRepository;
    private final CompanionRepository companionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (companyRepository.count() > 0) {
            log.info("[DataInitializer] DB 초기 데이터가 이미 활성화되어 있습니다.");
            seedMembersAndCompanionsIfEmpty();
            return;
        }

        log.info("[DataInitializer] KSA_DATABASE 스키마 및 시드 데이터 동기화 시작...");

        // =========================================
        // 1. 선사 데이터 (shipping_companies)
        // =========================================
        Company companySeaspovill = companyRepository.save(Company.builder().name("씨스포빌").tel("1577-8665").build());
        Company companyDaezer = companyRepository.save(Company.builder().name("대저해운").tel("1899-8114").build());

        // =========================================
        // 2. 선박 데이터 (ships)
        // =========================================
        Ship shipSeastar1 = shipRepository.save(Ship.builder().company(companySeaspovill).name("씨스타1호").capacity(442).build());
        Ship shipEldorado = shipRepository.save(Ship.builder().company(companyDaezer).name("엘도라도 익스프레스호").capacity(970).build());

        // =========================================
        // 3. 선박별 좌석등급 (ship_seat_classes)
        // =========================================
        // 씨스타1호: 1층 (300석, 60,000원), 2층 (142석, 60,000원)
        SeatGrade seastar1F = seatGradeRepository.save(SeatGrade.builder().ship(shipSeastar1).gradeName("1층").seatCapacity(300).classOrder(1).basePrice(60000).build());
        SeatGrade seastar2F = seatGradeRepository.save(SeatGrade.builder().ship(shipSeastar1).gradeName("2층").seatCapacity(142).classOrder(2).basePrice(60000).build());

        // 엘도라도 익스프레스호: 이코노미 (700석, 86,000원), 비즈니스 (200석, 120,000원), 퍼스트 (70석, 170,000원)
        SeatGrade eldoradoEco = seatGradeRepository.save(SeatGrade.builder().ship(shipEldorado).gradeName("이코노미").seatCapacity(700).classOrder(1).basePrice(86000).build());
        SeatGrade eldoradoBiz = seatGradeRepository.save(SeatGrade.builder().ship(shipEldorado).gradeName("비즈니스").seatCapacity(200).classOrder(2).basePrice(120000).build());
        SeatGrade eldoradoFst = seatGradeRepository.save(SeatGrade.builder().ship(shipEldorado).gradeName("퍼스트").seatCapacity(70).classOrder(3).basePrice(170000).build());

        // =========================================
        // 4. 운항일정 (sailing_schedules & schedule_seats)
        // 2026년 8월 6일 ~ 8월 31일 전 기간 생성
        // =========================================
        for (int day = 6; day <= 31; day++) {
            LocalDate sailingDate = LocalDate.of(2026, 8, day);

            // [씨스타1호 - 묵호 ↔ 도동항]
            Schedule s1 = scheduleRepository.save(Schedule.builder()
                    .ship(shipSeastar1)
                    .departurePort("묵호")
                    .arrivalPort("도동항")
                    .departureTime(LocalDateTime.of(sailingDate, LocalTime.of(8, 20)))
                    .arrivalTime(LocalDateTime.of(sailingDate, LocalTime.of(11, 0)))
                    .status(Schedule.ScheduleStatus.SCHEDULED)
                    .build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(s1).seatGrade(seastar1F).totalSeats(300).availableSeats(300).price(60000).build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(s1).seatGrade(seastar2F).totalSeats(142).availableSeats(142).price(60000).build());

            Schedule s2 = scheduleRepository.save(Schedule.builder()
                    .ship(shipSeastar1)
                    .departurePort("도동항")
                    .arrivalPort("묵호")
                    .departureTime(LocalDateTime.of(sailingDate, LocalTime.of(17, 10)))
                    .arrivalTime(LocalDateTime.of(sailingDate, LocalTime.of(19, 50)))
                    .status(Schedule.ScheduleStatus.SCHEDULED)
                    .build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(s2).seatGrade(seastar1F).totalSeats(300).availableSeats(300).price(60000).build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(s2).seatGrade(seastar2F).totalSeats(142).availableSeats(142).price(60000).build());

            // [씨스타1호 - 도동항 ↔ 독도] (02_seed_data.sql 및 씨스포빌 운항일정과 동일한 날짜: 7,8,10,12,14,15,16,18,20,22,25,27,29일)
            java.util.Set<Integer> dokdoDays = java.util.Set.of(7, 8, 10, 12, 14, 15, 16, 18, 20, 22, 25, 27, 29);
            if (dokdoDays.contains(day)) {
                LocalTime dokdoTime = LocalTime.of(12, 30);
                if (day == 14) dokdoTime = LocalTime.of(15, 30);
                else if (day == 15) dokdoTime = LocalTime.of(15, 0);
                else if (day == 16) dokdoTime = LocalTime.of(5, 0);

                Schedule sDokdo = scheduleRepository.save(Schedule.builder()
                        .ship(shipSeastar1)
                        .departurePort("도동항")
                        .arrivalPort("독도")
                        .departureTime(LocalDateTime.of(sailingDate, dokdoTime))
                        .arrivalTime(LocalDateTime.of(sailingDate, dokdoTime.plusHours(3)))
                        .status(Schedule.ScheduleStatus.SCHEDULED)
                        .build());
                scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sDokdo).seatGrade(seastar1F).totalSeats(300).availableSeats(300).price(60000).build());
                scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sDokdo).seatGrade(seastar2F).totalSeats(142).availableSeats(142).price(60000).build());
            }

            // [엘도라도 익스프레스호 - 포항 ↔ 도동항]
            Schedule e1 = scheduleRepository.save(Schedule.builder()
                    .ship(shipEldorado)
                    .departurePort("포항")
                    .arrivalPort("도동항")
                    .departureTime(LocalDateTime.of(sailingDate, LocalTime.of(9, 50)))
                    .arrivalTime(LocalDateTime.of(sailingDate, LocalTime.of(12, 50)))
                    .status(Schedule.ScheduleStatus.SCHEDULED)
                    .build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(e1).seatGrade(eldoradoEco).totalSeats(700).availableSeats(700).price(86000).build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(e1).seatGrade(eldoradoBiz).totalSeats(200).availableSeats(200).price(120000).build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(e1).seatGrade(eldoradoFst).totalSeats(70).availableSeats(70).price(170000).build());

            Schedule e2 = scheduleRepository.save(Schedule.builder()
                    .ship(shipEldorado)
                    .departurePort("도동항")
                    .arrivalPort("포항")
                    .departureTime(LocalDateTime.of(sailingDate, LocalTime.of(14, 20)))
                    .arrivalTime(LocalDateTime.of(sailingDate, LocalTime.of(17, 20)))
                    .status(Schedule.ScheduleStatus.SCHEDULED)
                    .build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(e2).seatGrade(eldoradoEco).totalSeats(700).availableSeats(700).price(86000).build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(e2).seatGrade(eldoradoBiz).totalSeats(200).availableSeats(200).price(120000).build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(e2).seatGrade(eldoradoFst).totalSeats(70).availableSeats(70).price(170000).build());
        }

        seedMembersAndCompanionsIfEmpty();

        log.info("[DataInitializer] KSA_DATABASE 모든 초기 데이터 100% 동기화 완료!");
    }

    private void seedMembersAndCompanionsIfEmpty() {
        if (!userRepository.existsByPhone("01012341234")) {
            userRepository.save(User.builder()
                    .loginId("01012341234")
                    .phone("01012341234")
                    .password("1234")
                    .name("정현영")
                    .birthDate("2000-01-01")
                    .gender("여성")
                    .nationality("대한민국")
                    .build());
        }

        if (!userRepository.existsByPhone("12345")) {
            userRepository.save(User.builder()
                    .loginId("12345")
                    .phone("12345")
                    .password("12345")
                    .name("테스트12345")
                    .birthDate("2000-01-01")
                    .gender("남성")
                    .nationality("대한민국")
                    .build());
        }

        if (!userRepository.existsByPhone("01000000000")) {
            userRepository.save(User.builder()
                    .loginId("01000000000")
                    .phone("01000000000")
                    .password("admin123")
                    .name("관리자")
                    .email("admin@shipbooking.com")
                    .role(User.Role.ADMIN)
                    .build());
        }

        if (companionRepository.count() == 0) {
            companionRepository.save(Companion.builder()
                    .memberId(1L)
                    .companionName("김철수")
                    .birthDate(java.time.LocalDate.of(1998, 5, 20))
                    .gender(Companion.Gender.MALE)
                    .nationality("대한민국")
                    .phoneNumber("010-1234-5678")
                    .emergencyContact("010-9876-5432")
                    .build());

            companionRepository.save(Companion.builder()
                    .memberId(1L)
                    .companionName("박영희")
                    .birthDate(java.time.LocalDate.of(2000, 11, 3))
                    .gender(Companion.Gender.FEMALE)
                    .nationality("대한민국")
                    .phoneNumber("010-1111-2222")
                    .emergencyContact("010-3333-4444")
                    .build());
        }
    }
}
