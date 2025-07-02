package com.courtlink.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * BookingApplication Integration Tests
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Booking Application Integration Tests")
class BookingApplicationTests {

    @Test
    @DisplayName("Should load application context successfully")
    void contextLoads() {
        // This test will fail if the application context cannot be loaded
        // It validates that all beans can be created and injected properly
    }
} 