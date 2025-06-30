package com.example.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 预约管理与支付集成系统主启动类
 * 
 * @author Your Name
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class AppointmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppointmentApplication.class, args);
    }
} 