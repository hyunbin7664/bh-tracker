package com.bh.tracker.domain.repairorder.service;

import com.bh.tracker.domain.engineer.entity.Engineer;
import com.bh.tracker.domain.engineer.repository.EngineerRepository;
import com.bh.tracker.domain.repairorder.entity.IncomingStatus;
import com.bh.tracker.domain.repairorder.entity.Part;
import com.bh.tracker.domain.repairorder.entity.RepairOrder;
import com.bh.tracker.domain.repairorder.repository.RepairOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderRepository repairOrderRepository;
    private final EngineerRepository engineerRepository;

    public List<RepairOrder> findAll() {
        return repairOrderRepository.findAll();
    }

    public RepairOrder findById(Long id) {
        return repairOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("RO not found: " + id));
    }

    public List<RepairOrder> findByEngineer(Long engineerId) {
        return repairOrderRepository.findByEngineerId(engineerId);
    }

    public List<RepairOrder> findReturnDeadlineApproaching() {
        LocalDate threshold = LocalDate.now().minusDays(23);
        return repairOrderRepository.findReturnDeadlineApproaching(threshold);
    }

    @Transactional
    public RepairOrder create(String roNumber, String vehicleNumber, String customerName,
                              String customerPhone, Long engineerId,
                              List<PartCommand> parts) {
        Engineer engineer = engineerRepository.findById(engineerId)
                .orElseThrow(() -> new NoSuchElementException("Engineer not found: " + engineerId));

        RepairOrder ro = new RepairOrder(roNumber, vehicleNumber, customerName, customerPhone, engineer);
        for (PartCommand part : parts) {
            ro.getParts().add(new Part(ro, part.partNumber(), part.partName()));
        }
        return repairOrderRepository.save(ro);
    }

    @Transactional
    public RepairOrder markReceived(Long id) {
        RepairOrder ro = findById(id);
        ro.setIncomingStatus(IncomingStatus.RECEIVED);
        if (ro.getReceivedDate() == null) {
            ro.setReceivedDate(LocalDate.now());
        }
        return ro;
    }

    @Transactional
    public RepairOrder registerAppointment(Long id, LocalDate appointmentDate) {
        RepairOrder ro = findById(id);
        ro.setAppointmentDate(appointmentDate);
        return ro;
    }

    @Transactional
    public RepairOrder markReturnProcessed(Long id) {
        RepairOrder ro = findById(id);
        ro.setReturnProcessed(true);
        ro.setIncomingStatus(IncomingStatus.RETURNED);
        return ro;
    }

    public record PartCommand(String partNumber, String partName) {}
}
