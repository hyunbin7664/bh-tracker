package com.bh.tracker.domain.organization.controller;

import com.bh.tracker.domain.organization.dto.response.OrganizationListResponse;
import com.bh.tracker.domain.organization.dto.response.OrganizationSummary;
import com.bh.tracker.domain.organization.service.OrganizationService;
import com.bh.tracker.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    // 로그인 전에 호출되므로 id, name만 노출
    @GetMapping
    public ApiResponse<OrganizationListResponse> search(@RequestParam(required = false) String search) {
        List<OrganizationSummary> items = organizationService.search(search).stream()
                .map(org -> new OrganizationSummary(org.getId(), org.getName()))
                .toList();
        return ApiResponse.ok("지점 목록 조회 성공", new OrganizationListResponse(items));
    }
}
