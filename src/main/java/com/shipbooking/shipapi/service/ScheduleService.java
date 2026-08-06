package com.shipbooking.shipapi.service;

import com.shipbooking.shipapi.dto.ScheduleDto;
import com.shipbooking.shipapi.entity.Schedule;
import com.shipbooking.shipapi.entity.ScheduleSeat;
import com.shipbooking.shipapi.repository.ScheduleRepository;
import com.shipbooking.shipapi.repository.ScheduleSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;

    /**
     * 출발항, 도착항, (선택)날짜를 조건으로 운항 일정 및 등급별 잔여 좌석 조회
     */
    @Transactional(readOnly = true)
    public List<ScheduleDto.Response> searchSchedules(String departurePort, String arrivalPort, String date) {
        List<Schedule> schedules;

        if (date != null && !date.isBlank()) {
            LocalDate searchDate = LocalDate.parse(date);
            schedules = scheduleRepository.findByDeparturePortAndArrivalPortAndSailingDateBetween(departurePort, arrivalPort, searchDate, searchDate);
        } else {
            schedules = scheduleRepository.findByDeparturePortAndArrivalPort(departurePort, arrivalPort);
        }

        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 전체 운항 일정 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ScheduleDto.Response> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ScheduleDto.Response convertToDto(Schedule schedule) {
        List<ScheduleSeat> seats = scheduleSeatRepository.findByScheduleId(schedule.getId());
        List<ScheduleDto.SeatInventory> seatInventoryList = seats.stream()
                .map(seat -> ScheduleDto.SeatInventory.builder()
                        .seatGradeId(seat.getSeatGrade().getId())
                        .gradeName(seat.getSeatGrade().getGradeName())
                        .price(seat.getPrice())
                        .availableSeats(seat.getAvailableSeats())
                        .totalSeats(seat.getTotalSeats())
                        .build())
                .collect(Collectors.toList());

        return ScheduleDto.Response.builder()
                .id(schedule.getId())
                .companyName(schedule.getShip().getCompany().getName())
                .shipName(schedule.getShip().getName())
                .departurePort(schedule.getDeparturePort())
                .arrivalPort(schedule.getArrivalPort())
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .status(schedule.getStatus().name())
                .seats(seatInventoryList)
                .build();
    }
}
