package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * [운항 일정 엔티티]
 * 특정 날짜/시간의 여객선 운항 스케줄 (예: 포항 ➡️ 울릉도(저동) 2026-08-10 09:50 출발)
 */
@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id", nullable = false)
    private Ship ship; // 운항 선박

    @Column(nullable = false, length = 50)
    private String departurePort; // 출발항 (예: 포항, 묵호, 강릉)

    @Column(nullable = false, length = 50)
    private String arrivalPort; // 도착항 (예: 울릉도(저동), 독도)

    @Column(nullable = false)
    private LocalDateTime departureTime; // 출발 일시

    @Column(nullable = false)
    private LocalDateTime arrivalTime; // 도착 일시

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status; // 운항 상태 (SCHEDULED, DEPARTED, CANCELLED)

    public enum ScheduleStatus {
        SCHEDULED, // 운항 예정
        DEPARTED,  // 출항 완료
        CANCELLED  // 결항/취소
    }
}
