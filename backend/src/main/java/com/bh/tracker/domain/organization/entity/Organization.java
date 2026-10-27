package com.bh.tracker.domain.organization.entity;

import com.bh.tracker.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "organizations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 드롭다운의 선택 기준이라 유일해야 함 (공통기능_PRD.md NFR-02)
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // BCrypt 해시 (NFR-01)
    @Column(nullable = false, length = 100)
    private String password;

    // phone/address/좌표는 부트스트랩 직후 비어있고, 지점이 설정 화면에서 직접 채움 (COM-05)
    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    public Organization(String name, String encodedPassword) {
        this.name = name;
        this.password = encodedPassword;
    }
}
