package com.bh.tracker.domain.auth.service;

import com.bh.tracker.domain.organization.entity.Organization;
import com.bh.tracker.domain.organization.repository.OrganizationRepository;
import com.bh.tracker.global.common.exception.BaseErrorCode;
import com.bh.tracker.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public Organization authenticate(Long organizationId, String rawPassword) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BaseException(BaseErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(rawPassword, organization.getPassword())) {
            throw new BaseException(BaseErrorCode.LOGIN_FAILED);
        }
        return organization;
    }
}
