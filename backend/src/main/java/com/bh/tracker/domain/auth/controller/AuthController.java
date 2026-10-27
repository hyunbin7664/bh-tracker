package com.bh.tracker.domain.auth.controller;

import com.bh.tracker.domain.auth.service.AuthService;
import com.bh.tracker.domain.organization.entity.Organization;
import com.bh.tracker.global.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 로그아웃(POST /api/auth/logout)은 SecurityConfig의 logout 설정이 처리
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Organization organization = authService.authenticate(request.organizationId(), request.password());

        // 세션 고정 공격 방지: 로그인 전 세션은 버리고 새 세션에 인증 정보 저장
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // 세션에는 지점 id만 principal로 저장, @LoginOrganization으로 꺼내 씀
        var authentication = UsernamePasswordAuthenticationToken.authenticated(organization.getId(), null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return ApiResponse.ok("로그인 성공", new LoginResponse(organization.getId(), organization.getName()));
    }

    record LoginRequest(@NotNull Long organizationId, @NotBlank String password) {}

    record LoginResponse(Long organizationId, String organizationName) {}
}
