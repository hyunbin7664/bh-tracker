package com.bh.tracker.domain.organization.repository;

import com.bh.tracker.domain.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByNameContainingOrderByNameAsc(String name);

    boolean existsByName(String name);
}
