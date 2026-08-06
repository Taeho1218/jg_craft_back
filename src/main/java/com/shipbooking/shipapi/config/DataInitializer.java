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
    private final CompanionRepository companionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (companyRepository.count() > 0) {
            log.info("[DataInitializer] 초기 데이터가 이미 존재합니다.");
            seedCompanionsIfEmpty();
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

        // 4. 운항 일정 생성 (포항, 묵호, 강릉 ↔ 울릉도 도동/저동/사동)
        LocalDateTime now = LocalDateTime.now();
        int[] dayOffsets = {2, 4, 6, 8, 10, 12, 14, 16, 18, 20};

        for (int offset : dayOffsets) {
            // 포항 -> 울릉도(저동)
            Schedule p1 = scheduleRepository.save(Schedule.builder()
                    .ship(ship1)
                    .departurePort("포항")
                    .arrivalPort("울릉도(저동)")
                    .departureTime(now.plusDays(offset).withHour(9).withMinute(50).withSecond(0))
                    .arrivalTime(now.plusDays(offset).withHour(13).withMinute(20).withSecond(0))
                    .status(Schedule.ScheduleStatus.SCHEDULED)
                    .build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(p1).seatGrade(gradeNormal1).totalSeats(300).availableSeats(280).price(64500).build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(p1).seatGrade(gradeSuperior1).totalSeats(120).availableSeats(110).price(70700).build());

            // 묵호 -> 울릉도(도동)
            Schedule m1 = scheduleRepository.save(Schedule.builder()
                    .ship(ship2)
                    .departurePort("묵호")
                    .arrivalPort("울릉도(도동)")
                    .departureTime(now.plusDays(offset).withHour(8).withMinute(20).withSecond(0))
                    .arrivalTime(now.plusDays(offset).withHour(11).withMinute(20).withSecond(0))
                    .status(Schedule.ScheduleStatus.SCHEDULED)
                    .build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(m1).seatGrade(gradeNormal2).totalSeats(250).availableSeats(240).price(60000).build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(m1).seatGrade(gradeSuperior2).totalSeats(100).availableSeats(90).price(66000).build());

            // 묵호 -> 울릉도(사동)
            Schedule m2 = scheduleRepository.save(Schedule.builder()
                    .ship(ship2)
                    .departurePort("묵호")
                    .arrivalPort("울릉도(사동)")
                    .departureTime(now.plusDays(offset).withHour(12).withMinute(40).withSecond(0))
                    .arrivalTime(now.plusDays(offset).withHour(15).withMinute(40).withSecond(0))
                    .status(Schedule.ScheduleStatus.SCHEDULED)
                    .build());
            scheduleSeatRepository.save(ScheduleSeat.builder().schedule(m2).seatGrade(gradeNormal2).totalSeats(250).availableSeats(230).price(60000).build());
        }

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

        seedCompanionsIfEmpty();

        log.info("[DataInitializer] 샘플 데이터 시딩 완료!");
    }

    private void seedCompanionsIfEmpty() {
        if (companionRepository.count() == 0) {
            companionRepository.save(Companion.builder()
                    .memberId(3L)
                    .companionName("김철수")
                    .birthDate(java.time.LocalDate.of(1998, 5, 20))
                    .gender(Companion.Gender.MALE)
                    .nationality("대한민국")
                    .phoneNumber("010-1234-5678")
                    .emergencyContact("010-9876-5432")
                    .build());

            companionRepository.save(Companion.builder()
                    .memberId(3L)
                    .companionName("박영희")
                    .birthDate(java.time.LocalDate.of(2000, 11, 3))
                    .gender(Companion.Gender.FEMALE)
                    .nationality("대한민국")
                    .phoneNumber("010-1111-2222")
                    .emergencyContact("010-3333-4444")
                    .build());

            companionRepository.save(Companion.builder()
                    .memberId(5L)
                    .companionName("John Smith")
                    .birthDate(java.time.LocalDate.of(1995, 8, 10))
                    .gender(Companion.Gender.MALE)
                    .nationality("USA")
                    .phoneNumber("+1-555-1234")
                    .emergencyContact("+1-555-9999")
                    .build());

            log.info("[DataInitializer] 샘플 동행자 데이터 3건 생성 완료!");
        }
    }
}
