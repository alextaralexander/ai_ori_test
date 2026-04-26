package com.bestorigin.monolith.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.bestorigin.monolith")
public class BestOriginMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(BestOriginMonolithApplication.class, args);
    }
}
