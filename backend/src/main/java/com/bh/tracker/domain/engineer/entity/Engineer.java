package com.bh.tracker.domain.engineer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "engineers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Engineer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    public Engineer(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}
