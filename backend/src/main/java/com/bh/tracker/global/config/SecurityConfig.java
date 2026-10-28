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
                // SameSite=Lax 쿠키로 대신 방어. GET엔 쿠키가 실리므로 상태 변경 API는 GET으로 만들지 않음
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // /error 포워딩이 401로 덮이지 않도록
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        // 로그인 전 화면에서 호출하는 API
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
