package com.bh.tracker.domain.repairorder.entity;

import com.bh.tracker.domain.engineer.entity.Engineer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repair_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String roNumber;

    @Column(nullable = false, length = 20)
    private String vehicleNumber;

    @Column(nullable = false, length = 50)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String customerPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id", nullable = false)
    private Engineer engineer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private IncomingStatus incomingStatus = IncomingStatus.ORDER_PLACED;

    // D+0 기준일: 모든 부품이 입고된 날
    @Setter
    private LocalDate receivedDate;

    // 예약일 값 유무만으로 예약완료 상태 판단 (PRD §FR-13)
    @Setter
    private LocalDate appointmentDate;

    @Setter
    private LocalDate notification1SentAt;

    @Setter
    private LocalDate notification2SentAt;

    @Setter
    private LocalDate finalNotificationSentAt;

    @Setter
    private boolean returnProcessed;

    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Part> parts = new ArrayList<>();

    public RepairOrder(String roNumber, String vehicleNumber, String customerName,
                       String customerPhone, Engineer engineer) {
        this.roNumber = roNumber;
        this.vehicleNumber = vehicleNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.engineer = engineer;
    }

    public boolean isAppointmentConfirmed() {
        return appointmentDate != null;
    }

    public LocalDate getReturnDeadline() {
        return receivedDate != null ? receivedDate.plusDays(30) : null;
    }
}
