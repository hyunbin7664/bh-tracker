package com.bh.tracker.domain.engineer.repository;

import com.bh.tracker.domain.engineer.entity.Engineer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineerRepository extends JpaRepository<Engineer, Long> {
}
