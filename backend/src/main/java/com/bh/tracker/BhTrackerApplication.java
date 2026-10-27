package com.bh.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BhTrackerApplication {

    // 영업일·노쇼 판정 등 날짜 로직이 서버 위치(클라우드 기본 UTC)와 무관하게 한국 날짜 기준으로 동작하도록 고정.
    // 빈 초기화보다 먼저 적용되도록 클래스 로딩 시점에 설정
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(BhTrackerApplication.class, args);
    }
}
