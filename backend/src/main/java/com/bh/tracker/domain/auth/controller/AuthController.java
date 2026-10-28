package com.bh.tracker.domain.auth.controller;

import com.bh.tracker.domain.auth.dto.request.LoginRequest;
import com.bh.tracker.domain.auth.dto.response.LoginResponse;
import com.bh.tracker.domain.auth.service.AuthService;
import com.bh.tracker.domain.organization.entity.Organization;
import com.bh.tracker.global.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

// 로그아웃은 SecurityConfig의 logout 설정이 처리
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

        // 세션 고정 공격 방지
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        var authentication = UsernamePasswordAuthenticationToken.authenticated(organization.getId(), null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return ApiResponse.ok("로그인 성공", new LoginResponse(organization.getId(), organization.getName()));
    }
}
