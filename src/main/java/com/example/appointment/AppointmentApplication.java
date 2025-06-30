package com.example.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ԤԼ������֧������ϵͳ��������
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