package com.fincore.fincore_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FincoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FincoreApiApplication.class, args);
    }

}
