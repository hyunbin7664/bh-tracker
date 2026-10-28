package com.bh.tracker.support;

import com.bh.tracker.global.auth.LoginOrganization;
import com.bh.tracker.global.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// 인증·공통 예외 처리 검증용 테스트 전용 엔드포인트
@RestController
public class TestProtectedController {

    @GetMapping("/api/test/me")
    public ApiResponse<Long> me(@LoginOrganization Long organizationId) {
        return ApiResponse.ok("조회 성공", organizationId);
    }

    @GetMapping("/api/test/items/{id}")
    public ApiResponse<Long> item(@PathVariable Long id) {
        return ApiResponse.ok("조회 성공", id);
    }
}
