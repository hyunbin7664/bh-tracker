package com.bh.tracker.domain.organization.entity;

import com.bh.tracker.domain.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrganizationTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @AfterEach
    void tearDown() {
        organizationRepository.deleteAll();
    }

    @Test
    void 지점을_저장하면_생성_수정_시각이_기록된다() {
        Organization organization = organizationRepository.save(new Organization("강남점", "hashed"));

        assertThat(organization.getCreatedAt()).isNotNull();
        assertThat(organization.getUpdatedAt()).isNotNull();
    }
}
