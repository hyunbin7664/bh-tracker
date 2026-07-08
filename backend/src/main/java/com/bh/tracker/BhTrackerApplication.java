package com.bh.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BhTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BhTrackerApplication.class, args);
    }
}
