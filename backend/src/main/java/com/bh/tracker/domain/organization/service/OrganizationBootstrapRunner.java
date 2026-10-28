package com.bh.tracker.domain.organization.service;

import com.bh.tracker.domain.organization.entity.Organization;
import com.bh.tracker.domain.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 운영자가 신규 지점을 지점명 + 기본 비밀번호로 등록한다 (공통기능_PRD.md COM-01).
 * 앱 내 화면 없이, 실행 시 bootstrap.organization.name/password 인자를 줄 때만 동작한다.
 * 예: ./gradlew bootRun --args='--spring.profiles.active=local
 *     --bootstrap.organization.name=강남점 --bootstrap.organization.password=초기비밀번호'
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "bootstrap.organization.name")
@RequiredArgsConstructor
public class OrganizationBootstrapRunner implements ApplicationRunner {

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bootstrap.organization.name}")
    private String name;

    @Value("${bootstrap.organization.password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 지점명은 로그인 시 지점을 고르는 유일 키라, 앞뒤 공백 차이로 같은 지점이 중복 등록되지 않게 정리
        String name = this.name.trim();
        if (name.isBlank()) {
            throw new IllegalStateException("bootstrap.organization.name이 비어 있어 지점을 등록할 수 없습니다.");
        }
        // 빈 비밀번호로 등록되면 로그인 요청 검증(@NotBlank)에 막혀 지점이 영영 로그인할 수 없음
        if (password.isBlank()) {
            throw new IllegalStateException("bootstrap.organization.password가 비어 있어 지점을 등록할 수 없습니다.");
        }
        if (organizationRepository.existsByName(name)) {
            log.warn("이미 등록된 지점이라 건너뜀: {}", name);
            return;
        }
        organizationRepository.save(new Organization(name, passwordEncoder.encode(password)));
        log.info("지점 등록 완료: {}", name);
    }
}
