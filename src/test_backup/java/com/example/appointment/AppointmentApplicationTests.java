package com.example.appointment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AppointmentApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否正常加载
    }
} 