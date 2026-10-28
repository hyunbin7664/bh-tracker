package com.bh.tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BhTrackerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void 기본_타임존은_한국_시간이다() {
        assertThat(TimeZone.getDefault().getID()).isEqualTo("Asia/Seoul");
    }
}
