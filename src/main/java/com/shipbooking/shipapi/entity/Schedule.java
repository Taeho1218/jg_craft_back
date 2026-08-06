package com.shipbooking.shipapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * [운항 일정 엔티티 - EC2 sailing_schedules 테이블 매핑]
 */
@Entity
@Table(name = "sailing_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id; // 스케줄 PK ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id", nullable = false)
    private Ship ship; // 운항 선박

    @Column(name = "sailing_date", nullable = false)
    private LocalDate sailingDate; // 운항 날짜 (YYYY-MM-DD)

    @Column(name = "departure_port", nullable = false, length = 50)
    private String departurePort; // 출발항 (예: 포항, 묵호, 강릉)

    @Column(name = "arrival_port", nullable = false, length = 50)
    private String arrivalPort; // 도착항 (예: 울릉도(저동), 울릉도(도동))

    @Column(name = "departure_time", nullable = false)
    private LocalTime timeOfDeparture; // 출발 시간 (HH:mm:ss)

    @Column(name = "estimated_duration_minutes")
    @Builder.Default
    private Integer estimatedDurationMinutes = 210; // 운항 소요 시간 (분 - 기본 3시간 30분)

    @Column(name = "operation_status", length = 20)
    @Builder.Default
    private String operationStatus = "운항"; // 운항, 결항

    @Enumerated(EnumType.STRING)
    @Transient
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 빌더 및 호환성 헬퍼 메서드
    public LocalDateTime getDepartureTime() {
        if (this.sailingDate != null && this.timeOfDeparture != null) {
            return LocalDateTime.of(this.sailingDate, this.timeOfDeparture);
        }
        return LocalDateTime.now();
    }

    public void setDepartureTime(LocalDateTime dt) {
        if (dt != null) {
            this.sailingDate = dt.toLocalDate();
            this.timeOfDeparture = dt.toLocalTime();
        }
    }

    public LocalDateTime getArrivalTime() {
        if (this.sailingDate != null && this.timeOfDeparture != null) {
            int duration = (this.estimatedDurationMinutes != null) ? this.estimatedDurationMinutes : 210;
            return LocalDateTime.of(this.sailingDate, this.timeOfDeparture).plusMinutes(duration);
        }
        return LocalDateTime.now();
    }

    public ScheduleStatus getStatus() {
        if ("결항".equals(this.operationStatus)) {
            return ScheduleStatus.CANCELLED;
        }
        return ScheduleStatus.SCHEDULED;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
        if (status == ScheduleStatus.CANCELLED) {
            this.operationStatus = "결항";
        } else {
            this.operationStatus = "운항";
        }
    }

    public static class ScheduleBuilder {
        public ScheduleBuilder departureTime(LocalDateTime dt) {
            if (dt != null) {
                this.sailingDate = dt.toLocalDate();
                this.timeOfDeparture = dt.toLocalTime();
            }
            return this;
        }

        public ScheduleBuilder arrivalTime(LocalDateTime dt) {
            if (dt != null && this.sailingDate != null && this.timeOfDeparture != null) {
                long minutes = java.time.Duration.between(LocalDateTime.of(this.sailingDate, this.timeOfDeparture), dt).toMinutes();
                this.estimatedDurationMinutes((int) minutes);
            }
            return this;
        }
    }

    public enum ScheduleStatus {
        SCHEDULED, // 운항 예정
        DEPARTED,  // 출항 완료
        CANCELLED  // 결항/취소
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.operationStatus == null) {
            this.operationStatus = "운항";
        }
        if (this.estimatedDurationMinutes == null) {
            this.estimatedDurationMinutes = 210;
        }
    }
}
