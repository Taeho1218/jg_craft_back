package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [승선객 명단 엔티티]
 * 선사에 전달할 필수 승선객 개인정보 (1개 예약에 N개 승선객 포함)
 */
@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation; // 해당 예약

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade_id", nullable = false)
    private SeatGrade seatGrade; // 선택한 좌석 등급

    @Column(nullable = false, length = 50)
    private String name; // 승선객 이름

    @Column(nullable = false, length = 10)
    private String birthDate; // 생년월일 (YYYY-MM-DD)

    @Column(nullable = false, length = 10)
    private String gender; // 성별 (M / F)

    @Column(nullable = false, length = 30)
    private String nationality; // 국적 (내국인: KOR 등)

    @Column(length = 20)
    private String phone; // 승선객 연락처

    @Column(nullable = false, unique = true, length = 40)
    private String ticketNumber; // 발급된 승선권 번호 (예: TK-20260806-8812)
}
