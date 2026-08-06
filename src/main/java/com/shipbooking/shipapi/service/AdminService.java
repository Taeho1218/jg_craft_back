package com.shipbooking.shipapi.service;

import com.shipbooking.shipapi.dto.AdminDto;
import com.shipbooking.shipapi.entity.Passenger;
import com.shipbooking.shipapi.entity.Schedule;

import com.shipbooking.shipapi.repository.PassengerRepository;
import com.shipbooking.shipapi.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ScheduleRepository scheduleRepository;
    private final PassengerRepository passengerRepository;

    /**
     * [선사 제출용 승선객 명단 데이터 추출 (출발 1일 전 오전에 선사에 전달할 명단)]
     */
    @Transactional(readOnly = true)
    public AdminDto.ManifestResponse getPassengerManifest(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운항 일정입니다: " + scheduleId));

        List<Passenger> passengers = passengerRepository.findByReservationScheduleId(scheduleId);

        List<AdminDto.PassengerDetail> passengerDetails = passengers.stream()
                .filter(p -> p.getReservation().getStatus() != com.shipbooking.shipapi.entity.Reservation.ReservationStatus.CANCELLED)
                .map(p -> AdminDto.PassengerDetail.builder()
                        .ticketNumber(p.getTicketNumber())
                        .bookingNumber(p.getReservation().getBookingNumber())
                        .name(p.getName())
                        .birthDate(p.getBirthDate())
                        .gender(p.getGender())
                        .nationality(p.getNationality())
                        .phone(p.getPhone())
                        .gradeName(p.getSeatGrade().getGradeName())
                        .bookerName(p.getReservation().getBookerName())
                        .bookerPhone(p.getReservation().getBookerPhone())
                        .build())
                .collect(Collectors.toList());

        return AdminDto.ManifestResponse.builder()
                .scheduleId(schedule.getId())
                .companyName(schedule.getShip().getCompany().getName())
                .shipName(schedule.getShip().getName())
                .departurePort(schedule.getDeparturePort())
                .arrivalPort(schedule.getArrivalPort())
                .departureTime(schedule.getDepartureTime())
                .totalPassengers(passengerDetails.size())
                .passengers(passengerDetails)
                .build();
    }
}
