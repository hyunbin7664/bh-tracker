package com.bh.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BhTrackerApplication {

    // 서버 위치(UTC)와 무관하게 한국 날짜 기준으로 동작하도록, 빈 초기화 전에 고정
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(BhTrackerApplication.class, args);
    }
}
