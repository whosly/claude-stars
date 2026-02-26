package com.yueny.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Bom21Application {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Bom21Application.class);
        application.run(args);
    }
}
