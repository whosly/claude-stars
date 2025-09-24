package com.whosly.stars.netty.visualizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Netty可视化工具主应用
 * 
 * @author fengyang
 * @date 2025-08-22
 */
@SpringBootApplication
public class NettySeeApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(NettySeeApplication.class, args);
        System.out.println("Netty See started at http://localhost:8081");
    }
}