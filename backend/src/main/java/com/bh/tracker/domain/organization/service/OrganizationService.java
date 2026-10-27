package com.bh.tracker.domain.organization.service;

import com.bh.tracker.domain.organization.entity.Organization;
import com.bh.tracker.domain.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    // 지점 수가 최대 수백 개 규모라 페이징 없이 전체 반환 (COM-09)
    public List<Organization> search(String keyword) {
        return organizationRepository.findByNameContainingOrderByNameAsc(keyword == null ? "" : keyword.trim());
    }
}
