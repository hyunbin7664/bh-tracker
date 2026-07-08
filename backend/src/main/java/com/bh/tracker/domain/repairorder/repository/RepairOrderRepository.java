package com.bh.tracker.domain.repairorder.repository;

import com.bh.tracker.domain.repairorder.entity.IncomingStatus;
import com.bh.tracker.domain.repairorder.entity.RepairOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

    List<RepairOrder> findByEngineerId(Long engineerId);

    List<RepairOrder> findByIncomingStatus(IncomingStatus status);

    @Query("""
            SELECT ro FROM RepairOrder ro
            WHERE ro.incomingStatus = 'RECEIVED'
              AND ro.appointmentDate IS NULL
              AND ro.receivedDate IS NOT NULL
            """)
    List<RepairOrder> findPendingAppointments();

    // 반품 기한(D+30) 임박 건: receivedDate <= threshold (= 오늘 - 23일)
    @Query("""
            SELECT ro FROM RepairOrder ro
            WHERE ro.incomingStatus = 'RECEIVED'
              AND ro.appointmentDate IS NULL
              AND ro.receivedDate IS NOT NULL
              AND ro.receivedDate <= :threshold
            """)
    List<RepairOrder> findReturnDeadlineApproaching(@Param("threshold") LocalDate threshold);
}
