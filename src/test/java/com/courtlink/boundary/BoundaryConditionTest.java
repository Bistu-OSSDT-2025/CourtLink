package com.courtlink.boundary;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.service.AppointmentService;
import com.courtlink.booking.service.PaymentService;
import com.courtlink.dto.CourtDTO;
import com.courtlink.service.CourtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("边界条件测试")
public class BoundaryConditionTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CourtService courtService;

    @Test
    @DisplayName("测试预约时间边界条件")
    @Transactional
    void testAppointmentTimeBoundary() {
        AppointmentRequest request = new AppointmentRequest();
        request.setUserId("user123");
        request.setProviderId("provider123");
        request.setServiceType("BADMINTON");
        
        // 测试过去时间
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        request.setStartTime(pastTime);
        request.setEndTime(pastTime.plusHours(1));
        assertThrows(RuntimeException.class, () -> 
            appointmentService.createAppointment("user123", request));

        // 测试开始时间晚于结束时间
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        request.setStartTime(futureTime.plusHours(1));
        request.setEndTime(futureTime);
        assertThrows(RuntimeException.class, () -> 
            appointmentService.createAppointment("user123", request));

        // 测试预约时间过长（超过4小时）
        request.setStartTime(futureTime);
        request.setEndTime(futureTime.plusHours(5));
        assertThrows(RuntimeException.class, () -> 
            appointmentService.createAppointment("user123", request));

        // 测试预约时间过短（少于30分钟）
        request.setStartTime(futureTime);
        request.setEndTime(futureTime.plusMinutes(20));
        assertThrows(RuntimeException.class, () -> 
            appointmentService.createAppointment("user123", request));
    }

    @Test
    @DisplayName("测试支付金额边界条件")
    @Transactional
    void testPaymentAmountBoundary() {
        Payment payment = new Payment();
        payment.setUserId("user123");
        payment.setAppointmentId("1");
        payment.setPaymentNo("PAY" + System.currentTimeMillis());
        
        // 测试负数金额
        payment.setAmount(new BigDecimal("-1.00"));
        assertThrows(ConstraintViolationException.class, () -> 
            paymentService.createPayment(payment));

        // 测试零金额
        payment.setAmount(BigDecimal.ZERO);
        assertThrows(ConstraintViolationException.class, () -> 
            paymentService.createPayment(payment));

        // 测试超大金额
        payment.setAmount(new BigDecimal("999999.99"));
        assertThrows(ConstraintViolationException.class, () -> 
            paymentService.createPayment(payment));

        // 测试小数位过多
        payment.setAmount(new BigDecimal("50.999"));
        assertThrows(ConstraintViolationException.class, () -> 
            paymentService.createPayment(payment));
    }

    @Test
    @DisplayName("测试场地信息边界条件")
    @Transactional
    void testCourtInfoBoundary() {
        CourtDTO courtDTO = new CourtDTO();
        
        // 测试场地名称为空
        courtDTO.setName("");
        assertThrows(IllegalArgumentException.class, () -> 
            courtService.createCourt(courtDTO));

        // 测试场地名称过长（超过100字符）
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            longName.append("a");
        }
        courtDTO.setName(longName.toString());
        assertThrows(IllegalArgumentException.class, () -> 
            courtService.createCourt(courtDTO));

        // 测试场地描述过长（超过500字符）
        StringBuilder longDesc = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            longDesc.append("a");
        }
        courtDTO.setDescription(longDesc.toString());
        assertThrows(IllegalArgumentException.class, () -> 
            courtService.createCourt(courtDTO));
    }

    @Test
    @DisplayName("测试并发预约边界条件")
    @Transactional
    void testConcurrentAppointmentBoundary() {
        AppointmentRequest request1 = new AppointmentRequest();
        request1.setUserId("user123");
        request1.setProviderId("provider123");
        request1.setServiceType("BADMINTON");
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        request1.setStartTime(futureTime);
        request1.setEndTime(futureTime.plusHours(1));

        // 创建第一个预约
        appointmentService.createAppointment("user123", request1);

        // 测试同一时间段的重复预约
        AppointmentRequest request2 = new AppointmentRequest();
        request2.setUserId("user456");
        request2.setProviderId("provider123");
        request2.setServiceType("BADMINTON");
        request2.setStartTime(futureTime);
        request2.setEndTime(futureTime.plusHours(1));

        assertThrows(RuntimeException.class, () -> 
            appointmentService.createAppointment("user456", request2));

        // 测试部分重叠的时间段
        request2.setStartTime(futureTime.minusMinutes(30));
        request2.setEndTime(futureTime.plusMinutes(30));
        assertThrows(RuntimeException.class, () -> 
            appointmentService.createAppointment("user456", request2));
    }
} 