package com.bh.tracker.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// BaseEntity의 createdAt/updatedAt 자동 기록
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
