package com.bh.tracker.global.config;

import com.bh.tracker.global.common.exception.BaseErrorCode;
import com.bh.tracker.global.common.response.ApiResponse;
import com.bh.tracker.global.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // 세션 쿠키가 SameSite=Lax라 다른 사이트에서 보낸 POST/PATCH엔 쿠키가 실리지 않음
                // (GET은 실리므로 상태를 바꾸는 요청을 GET으로 받지 않아야 함, 로그아웃도 POST만 허용)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 서블릿 에러 포워딩(/error)이 401로 덮이지 않도록 허용
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        // 로그인 전 화면에서 호출되는 엔드포인트만 인증 없이 허용 (공통기능_API명세.md)
                        .requestMatchers(HttpMethod.GET, "/api/organizations").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .anyRequest().authenticated())
                .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout", "POST"))
                        .logoutSuccessHandler((request, response, authentication) ->
                                writeJson(response, objectMapper, 200, ApiResponse.ok("로그아웃 성공"))))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            BaseErrorCode errorCode = BaseErrorCode.UNAUTHENTICATED;
                            writeJson(response, objectMapper, errorCode.getHttpStatus().value(), ErrorResponse.of(errorCode));
                        }));
        return http.build();
    }

    // 시큐리티 필터 단계의 응답은 컨트롤러를 거치지 않으므로 직접 JSON으로 작성
    private static void writeJson(HttpServletResponse response, ObjectMapper objectMapper,
                                  int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
