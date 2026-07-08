package com.bh.tracker.domain.repairorder.controller;

import com.bh.tracker.domain.repairorder.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/repair-orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @GetMapping
    public Object list(@RequestParam(required = false) Long engineerId) {
        return engineerId != null
                ? repairOrderService.findByEngineer(engineerId)
                : repairOrderService.findAll();
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) {
        return repairOrderService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CreateRepairOrderRequest request) {
        List<RepairOrderService.PartCommand> parts = request.parts().stream()
                .map(p -> new RepairOrderService.PartCommand(p.partNumber(), p.partName()))
                .toList();
        var ro = repairOrderService.create(
                request.roNumber(), request.vehicleNumber(),
                request.customerName(), request.customerPhone(),
                request.engineerId(), parts
        );
        return ResponseEntity.ok(ro.getId());
    }

    @PatchMapping("/{id}/received")
    public Object markReceived(@PathVariable Long id) {
        return repairOrderService.markReceived(id);
    }

    @PatchMapping("/{id}/appointment")
    public Object registerAppointment(@PathVariable Long id, @RequestBody AppointmentRequest request) {
        return repairOrderService.registerAppointment(id, request.appointmentDate());
    }

    @PatchMapping("/{id}/return-processed")
    public Object markReturnProcessed(@PathVariable Long id) {
        return repairOrderService.markReturnProcessed(id);
    }

    @GetMapping("/return-deadline-approaching")
    public Object returnDeadlineApproaching() {
        return repairOrderService.findReturnDeadlineApproaching();
    }

    record CreateRepairOrderRequest(
            String roNumber,
            String vehicleNumber,
            String customerName,
            String customerPhone,
            Long engineerId,
            List<PartRequest> parts
    ) {}

    record PartRequest(String partNumber, String partName) {}

    record AppointmentRequest(LocalDate appointmentDate) {}
}
