package com.bh.tracker.domain.auth.controller;

import com.bh.tracker.domain.organization.entity.Organization;
import com.bh.tracker.domain.organization.repository.OrganizationRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization gangnam;

    @BeforeEach
    void setUp() {
        gangnam = organizationRepository.save(new Organization("강남점", passwordEncoder.encode("pw1234")));
        organizationRepository.save(new Organization("잠실점", passwordEncoder.encode("pw5678")));
    }

    @AfterEach
    void tearDown() {
        organizationRepository.deleteAll();
    }

    @Test
    void 지점_검색은_로그인_없이_id와_이름만_반환한다() throws Exception {
        mockMvc.perform(get("/api/organizations").param("search", "강남"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(gangnam.getId()))
                .andExpect(jsonPath("$.data.items[0].name").value("강남점"))
                .andExpect(jsonPath("$.data.items[0].password").doesNotExist());
    }

    @Test
    void 검색어가_없으면_전체_지점을_이름순으로_반환한다() throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].name").value("강남점"))
                .andExpect(jsonPath("$.data.items[1].name").value("잠실점"));
    }

    @Test
    void 올바른_비밀번호로_로그인하면_세션_쿠키가_발급된다() throws Exception {
        Cookie sessionCookie = login(gangnam.getId(), "pw1234");

        assertThat(sessionCookie).isNotNull();
        assertThat(sessionCookie.isHttpOnly()).isTrue();
    }

    @Test
    void 비밀번호가_틀리면_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(gangnam.getId(), "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_ERROR_001"));
    }

    @Test
    void 존재하지_않는_지점이면_비밀번호_불일치와_같은_응답을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(999999L, "pw1234")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_ERROR_001"));
    }

    @Test
    void 필수값이_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationId\": " + gangnam.getId() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_ERROR_002"))
                .andExpect(jsonPath("$.field").value("password"));
    }

    @Test
    void 로그인하지_않으면_보호된_API에_401을_반환한다() throws Exception {
        mockMvc.perform(get("/api/test/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON_ERROR_005"));
    }

    @Test
    void 로그인하면_보호된_API에서_로그인한_지점_id를_받는다() throws Exception {
        Cookie sessionCookie = login(gangnam.getId(), "pw1234");

        mockMvc.perform(get("/api/test/me").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(gangnam.getId()));
    }

    @Test
    void 로그아웃하면_기존_세션_쿠키로_접근할_수_없다() throws Exception {
        Cookie sessionCookie = login(gangnam.getId(), "pw1234");

        mockMvc.perform(post("/api/auth/logout").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/test/me").cookie(sessionCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그아웃은_GET으로_처리되지_않는다() throws Exception {
        Cookie sessionCookie = login(gangnam.getId(), "pw1234");

        mockMvc.perform(get("/api/auth/logout").cookie(sessionCookie));

        mockMvc.perform(get("/api/test/me").cookie(sessionCookie))
                .andExpect(status().isOk());
    }

    @Test
    void 지원하지_않는_요청_형식이면_415를_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("COMMON_ERROR_007"));
    }

    @Test
    void 경로_변수_타입이_맞지_않으면_400을_반환한다() throws Exception {
        Cookie sessionCookie = login(gangnam.getId(), "pw1234");

        mockMvc.perform(get("/api/test/items/abc").cookie(sessionCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_ERROR_002"))
                .andExpect(jsonPath("$.field").value("id"));
    }

    private Cookie login(Long organizationId, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(organizationId, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.organizationId").value(organizationId))
                .andExpect(jsonPath("$.data.organizationName").value("강남점"))
                .andReturn()
                .getResponse()
                .getCookie("SESSION");
    }

    private String loginBody(Long organizationId, String password) {
        return "{\"organizationId\": " + organizationId + ", \"password\": \"" + password + "\"}";
    }
}
