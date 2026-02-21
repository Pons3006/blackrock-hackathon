package com.ponshankar.hackathon.blackrock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MicroSavingsApplication {

    private static final Logger log = LoggerFactory.getLogger(MicroSavingsApplication.class);

    public static void main(String[] args) {
        log.info("Starting Micro-Savings Retirement API");
        SpringApplication.run(MicroSavingsApplication.class, args);
        log.info("Micro-Savings Retirement API started successfully");
    }
}
