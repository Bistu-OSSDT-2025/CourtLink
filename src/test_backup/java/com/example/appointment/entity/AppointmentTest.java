package com.example.appointment.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setUserId(1L);
        appointment.setVenueId(1L);
        appointment.setStartTime(LocalDateTime.now().plusHours(1));
        appointment.setEndTime(LocalDateTime.now().plusHours(2));
        appointment.setSportType("篮球");
        appointment.setStatus("PENDING");
        appointment.setAmount(new BigDecimal("100.00"));
        appointment.setNotes("测试预约");
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testAppointmentCreation() {
        assertNotNull(appointment);
        assertEquals(1L, appointment.getId());
        assertEquals(1L, appointment.getUserId());
        assertEquals(1L, appointment.getVenueId());
        assertEquals("篮球", appointment.getSportType());
        assertEquals("PENDING", appointment.getStatus());
        assertEquals(new BigDecimal("100.00"), appointment.getAmount());
        assertEquals("测试预约", appointment.getNotes());
    }

    @Test
    void testAppointmentSettersAndGetters() {
        // 测试设置和获取方法
        appointment.setId(2L);
        appointment.setUserId(3L);
        appointment.setVenueId(4L);
        appointment.setSportType("足球");
        appointment.setStatus("CONFIRMED");
        appointment.setAmount(new BigDecimal("150.00"));
        appointment.setNotes("更新后的预约");

        assertEquals(2L, appointment.getId());
        assertEquals(3L, appointment.getUserId());
        assertEquals(4L, appointment.getVenueId());
        assertEquals("足球", appointment.getSportType());
        assertEquals("CONFIRMED", appointment.getStatus());
        assertEquals(new BigDecimal("150.00"), appointment.getAmount());
        assertEquals("更新后的预约", appointment.getNotes());
    }

    @Test
    void testTimeValidation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);

        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);

        assertEquals(startTime, appointment.getStartTime());
        assertEquals(endTime, appointment.getEndTime());
        assertTrue(appointment.getEndTime().isAfter(appointment.getStartTime()));
    }

    @Test
    void testStatusTransitions() {
        // 测试状态转换
        appointment.setStatus("PENDING");
        assertEquals("PENDING", appointment.getStatus());

        appointment.setStatus("CONFIRMED");
        assertEquals("CONFIRMED", appointment.getStatus());

        appointment.setStatus("COMPLETED");
        assertEquals("COMPLETED", appointment.getStatus());

        appointment.setStatus("CANCELLED");
        assertEquals("CANCELLED", appointment.getStatus());
    }

    @Test
    void testAmountValidation() {
        // 测试金额验证
        appointment.setAmount(new BigDecimal("0.00"));
        assertEquals(new BigDecimal("0.00"), appointment.getAmount());

        appointment.setAmount(new BigDecimal("999.99"));
        assertEquals(new BigDecimal("999.99"), appointment.getAmount());

        // 测试负数金额
        appointment.setAmount(new BigDecimal("-50.00"));
        assertEquals(new BigDecimal("-50.00"), appointment.getAmount());
    }

    @Test
    void testToString() {
        String toString = appointment.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Appointment"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("userId=1"));
        assertTrue(toString.contains("venueId=1"));
    }

    @Test
    void testEqualsAndHashCode() {
        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        appointment1.setUserId(1L);
        appointment1.setVenueId(1L);

        Appointment appointment2 = new Appointment();
        appointment2.setId(1L);
        appointment2.setUserId(1L);
        appointment2.setVenueId(1L);

        Appointment appointment3 = new Appointment();
        appointment3.setId(2L);
        appointment3.setUserId(1L);
        appointment3.setVenueId(1L);

        // 测试equals方法
        assertEquals(appointment1, appointment2);
        assertNotEquals(appointment1, appointment3);

        // 测试hashCode方法
        assertEquals(appointment1.hashCode(), appointment2.hashCode());
        assertNotEquals(appointment1.hashCode(), appointment3.hashCode());
    }
} 