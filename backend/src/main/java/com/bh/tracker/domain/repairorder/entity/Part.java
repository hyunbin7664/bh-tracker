package com.bh.tracker.domain.repairorder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_order_id", nullable = false)
    private RepairOrder repairOrder;

    @Column(nullable = false, length = 50)
    private String partNumber;

    @Column(nullable = false, length = 100)
    private String partName;

    @Setter
    private boolean received;

    public Part(RepairOrder repairOrder, String partNumber, String partName) {
        this.repairOrder = repairOrder;
        this.partNumber = partNumber;
        this.partName = partName;
    }
}
