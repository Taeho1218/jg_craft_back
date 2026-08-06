package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [운항별 좌석 재고 및 가격 엔티티]
 * 특정 운항 일정(Schedule)의 등급별(SeatGrade) 전체 수량 및 남은 잔여 좌석 수 관리
 */
@Entity
@Table(name = "schedule_seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; // 해당 운항 일정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade_id", nullable = false)
    private SeatGrade seatGrade; // 해당 좌석 등급

    @Column(nullable = false)
    private Integer totalSeats; // 등급별 전체 좌석 수

    @Column(nullable = false)
    private Integer availableSeats; // 잔여 좌석 수 (예매 시 차감)

    @Column(nullable = false)
    private Integer price; // 해당 운항 일정에서의 최종 요금 (원)

    /**
     * 좌석 예매 시 잔여 수량 차감
     */
    public void decreaseAvailableSeats(int count) {
        if (this.availableSeats < count) {
            throw new IllegalArgumentException("잔여 좌석이 부족합니다. (현재 잔여: " + this.availableSeats + "석)");
        }
        this.availableSeats -= count;
    }

    /**
     * 예매 취소 시 잔여 수량 복구
     */
    public void increaseAvailableSeats(int count) {
        this.availableSeats += count;
    }
}
