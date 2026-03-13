package com.example.jcbledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JcbLedgerApplication {
    public static void main(String[] args) {
        SpringApplication.run(JcbLedgerApplication.class, args);
    }
}
