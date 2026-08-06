package com.shipbooking.shipapi.config;

import com.shipbooking.shipapi.entity.*;
import com.shipbooking.shipapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * [샘플 데이터 자동 생성기]
 * 서버 구동 시 선사, 선박, 좌석등급, 운항일정, 잔여좌석 초기 데이터를 자동으로 시딩합니다.
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

    @Override
    public void run(String... args) throws Exception {
        if (companyRepository.count() > 0) {
            log.info("[DataInitializer] 초기 데이터가 이미 존재합니다.");
            return;
        }

        log.info("[DataInitializer] 샘플 데이터 시딩 시작...");

        // 1. 선사 생성
        Company company1 = companyRepository.save(Company.builder().name("대저해운").tel("1899-8114").build());
        Company company2 = companyRepository.save(Company.builder().name("씨스포빌").tel("1577-8665").build());

        // 2. 선박 생성
        Ship ship1 = shipRepository.save(Ship.builder().company(company1).name("썬플라워호").capacity(440).build());
        Ship ship2 = shipRepository.save(Ship.builder().company(company2).name("씨스타5호").capacity(350).build());

        // 3. 좌석 등급 생성 (썬플라워호)
        SeatGrade gradeNormal1 = seatGradeRepository.save(SeatGrade.builder().ship(ship1).gradeName("일반실").basePrice(64500).build());
        SeatGrade gradeSuperior1 = seatGradeRepository.save(SeatGrade.builder().ship(ship1).gradeName("우등실").basePrice(70700).build());
        SeatGrade gradeVip1 = seatGradeRepository.save(SeatGrade.builder().ship(ship1).gradeName("VIP실").basePrice(120000).build());

        // 좌석 등급 생성 (씨스타5호)
        SeatGrade gradeNormal2 = seatGradeRepository.save(SeatGrade.builder().ship(ship2).gradeName("일반실").basePrice(60000).build());
        SeatGrade gradeSuperior2 = seatGradeRepository.save(SeatGrade.builder().ship(ship2).gradeName("우등실").basePrice(66000).build());

        // 4. 운항 일정 생성 (포항 ↔ 울릉도)
        LocalDateTime now = LocalDateTime.now();
        Schedule sched1 = scheduleRepository.save(Schedule.builder()
                .ship(ship1)
                .departurePort("포항")
                .arrivalPort("울릉도(저동)")
                .departureTime(now.plusDays(2).withHour(9).withMinute(50).withSecond(0))
                .arrivalTime(now.plusDays(2).withHour(13).withMinute(20).withSecond(0))
                .status(Schedule.ScheduleStatus.SCHEDULED)
                .build());

        Schedule sched2 = scheduleRepository.save(Schedule.builder()
                .ship(ship1)
                .departurePort("울릉도(저동)")
                .arrivalPort("포항")
                .departureTime(now.plusDays(4).withHour(14).withMinute(0).withSecond(0))
                .arrivalTime(now.plusDays(4).withHour(17).withMinute(30).withSecond(0))
                .status(Schedule.ScheduleStatus.SCHEDULED)
                .build());

        Schedule sched3 = scheduleRepository.save(Schedule.builder()
                .ship(ship2)
                .departurePort("묵호")
                .arrivalPort("울릉도(사동)")
                .departureTime(now.plusDays(2).withHour(8).withMinute(20).withSecond(0))
                .arrivalTime(now.plusDays(2).withHour(11).withMinute(20).withSecond(0))
                .status(Schedule.ScheduleStatus.SCHEDULED)
                .build());

        // 5. 운항별 좌석 재고 및 가격 설정 (ScheduleSeat)
        scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sched1).seatGrade(gradeNormal1).totalSeats(300).availableSeats(280).price(64500).build());
        scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sched1).seatGrade(gradeSuperior1).totalSeats(120).availableSeats(110).price(70700).build());
        scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sched1).seatGrade(gradeVip1).totalSeats(20).availableSeats(18).price(120000).build());

        scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sched2).seatGrade(gradeNormal1).totalSeats(300).availableSeats(295).price(64500).build());
        scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sched2).seatGrade(gradeSuperior1).totalSeats(120).availableSeats(115).price(70700).build());

        scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sched3).seatGrade(gradeNormal2).totalSeats(250).availableSeats(240).price(60000).build());
        scheduleSeatRepository.save(ScheduleSeat.builder().schedule(sched3).seatGrade(gradeSuperior2).totalSeats(100).availableSeats(90).price(66000).build());

        // 6. 관리자 계정 생성 (전화번호: 01000000000, 비번: admin123)
        if (!userRepository.existsByPhone("01000000000")) {
            userRepository.save(User.builder()
                    .phone("01000000000")
                    .password("admin123")
                    .name("관리자")
                    .email("admin@shipbooking.com")
                    .role(User.Role.ADMIN)
                    .build());
        }

        log.info("[DataInitializer] 샘플 데이터 시딩 완료! (선사 2개, 선박 2개, 운항일정 3개, 관리자 계정 생성됨)");
    }
}
