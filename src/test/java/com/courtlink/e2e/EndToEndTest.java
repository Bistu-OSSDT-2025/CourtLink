package com.courtlink.e2e;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EndToEndTest {

    @Autowired
    private CourtService courtService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PaymentService paymentService;

    @Test
    @DisplayName("测试完整的预约和支付流程")
    @Transactional
    public void testCompleteBookingFlow() {
        // 1. 创建场地
        CourtDTO courtDTO = new CourtDTO();
        courtDTO.setName("测试场地");
        courtDTO.setStatus("AVAILABLE");
        courtDTO.setPricePerHour(100.00);
        CourtDTO createdCourt = courtService.createCourt(courtDTO);
        assertNotNull(createdCourt.getId(), "场地创建失败");

        // 2. 创建预约
        String userId = "user123";
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setProviderId(createdCourt.getId().toString());
        appointmentRequest.setStartTime(LocalDateTime.now().plusHours(1));
        appointmentRequest.setEndTime(LocalDateTime.now().plusHours(2));
        appointmentRequest.setServiceType("BADMINTON");
        
        AppointmentResponse appointment = appointmentService.createAppointment(userId, appointmentRequest);
        assertNotNull(appointment.getId(), "预约创建失败");
        assertEquals("PENDING", appointment.getStatus(), "预约状态不正确");

        // 3. 创建支付
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAppointmentId(appointment.getId().toString());
        payment.setPaymentNo("PAY" + System.currentTimeMillis());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment createdPayment = paymentService.createPayment(payment);
        assertNotNull(createdPayment.getId(), "支付创建失败");
        assertEquals(Payment.PaymentStatus.PENDING, createdPayment.getStatus(), "支付状态不正确");

        // 4. 处理支付
        Payment processedPayment = paymentService.processPayment(createdPayment.getPaymentNo(), Payment.PaymentMethod.CREDIT_CARD);
        assertEquals(Payment.PaymentStatus.SUCCESS, processedPayment.getStatus(), "支付处理失败");
        assertNotNull(processedPayment.getPaidAt(), "支付时间未记录");

        // 5. 确认预约
        AppointmentResponse confirmedAppointment = appointmentService.confirmAppointment(appointment.getId());
        assertEquals("CONFIRMED", confirmedAppointment.getStatus(), "预约确认失败");
        assertEquals(processedPayment.getId().toString(), confirmedAppointment.getPaymentId(), "预约未关联支付");

        // 6. 取消预约和退款
        AppointmentResponse cancelledAppointment = appointmentService.cancelAppointment(appointment.getId());
        assertEquals("CANCELLED", cancelledAppointment.getStatus(), "预约取消失败");

        Payment refundedPayment = paymentService.processRefund(processedPayment.getPaymentNo(), processedPayment.getAmount(), "预约取消退款");
        assertEquals(Payment.PaymentStatus.REFUNDED, refundedPayment.getStatus(), "退款失败");
        assertNotNull(refundedPayment.getRefundAt(), "退款时间未记录");
        assertEquals(processedPayment.getAmount(), refundedPayment.getRefundAmount(), "退款金额不正确");
    }

    @Test
    @DisplayName("测试并发预约场景")
    @Transactional
    public void testConcurrentBooking() {
        // 1. 创建场地
        CourtDTO courtDTO = new CourtDTO();
        courtDTO.setName("测试场地");
        courtDTO.setStatus("AVAILABLE");
        courtDTO.setPricePerHour(100.00);
        CourtDTO createdCourt = courtService.createCourt(courtDTO);

        // 2. 第一个用户预约
        String user1Id = "user1";
        AppointmentRequest request1 = new AppointmentRequest();
        request1.setProviderId(createdCourt.getId().toString());
        request1.setStartTime(LocalDateTime.now().plusHours(2));
        request1.setEndTime(LocalDateTime.now().plusHours(3));
        request1.setServiceType("BADMINTON");
        
        AppointmentResponse appointment1 = appointmentService.createAppointment(user1Id, request1);
        assertNotNull(appointment1.getId(), "第一个预约创建失败");

        // 3. 第二个用户尝试同一时间段预约
        String user2Id = "user2";
        AppointmentRequest request2 = new AppointmentRequest();
        request2.setProviderId(createdCourt.getId().toString());
        request2.setStartTime(request1.getStartTime());
        request2.setEndTime(request1.getEndTime());
        request2.setServiceType("BADMINTON");

        assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(user2Id, request2);
        }, "应该阻止重复预约");

        // 4. 第二个用户预约不同时间段
        request2.setStartTime(LocalDateTime.now().plusHours(4));
        request2.setEndTime(LocalDateTime.now().plusHours(5));
        
        AppointmentResponse appointment2 = appointmentService.createAppointment(user2Id, request2);
        assertNotNull(appointment2.getId(), "第二个预约创建失败");
        assertNotEquals(appointment1.getId(), appointment2.getId(), "两个预约不应该相同");
    }

    @Test
    @DisplayName("测试预约状态流转")
    @Transactional
    public void testAppointmentStatusTransition() {
        // 1. 创建场地
        CourtDTO courtDTO = new CourtDTO();
        courtDTO.setName("测试场地");
        courtDTO.setStatus("AVAILABLE");
        courtDTO.setPricePerHour(100.00);
        CourtDTO createdCourt = courtService.createCourt(courtDTO);

        // 2. 创建预约
        String userId = "user123";
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setProviderId(createdCourt.getId().toString());
        appointmentRequest.setStartTime(LocalDateTime.now().plusHours(1));
        appointmentRequest.setEndTime(LocalDateTime.now().plusHours(2));
        appointmentRequest.setServiceType("BADMINTON");
        
        final AppointmentResponse initialAppointment = appointmentService.createAppointment(userId, appointmentRequest);
        assertEquals("PENDING", initialAppointment.getStatus(), "初始状态应为PENDING");

        // 3. 创建支付
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAppointmentId(initialAppointment.getId().toString());
        payment.setPaymentNo("PAY" + System.currentTimeMillis());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment createdPayment = paymentService.createPayment(payment);
        assertNotNull(createdPayment.getId(), "支付创建失败");

        // 4. 处理支付
        Payment processedPayment = paymentService.processPayment(createdPayment.getPaymentNo(), Payment.PaymentMethod.CREDIT_CARD);
        assertEquals(Payment.PaymentStatus.SUCCESS, processedPayment.getStatus(), "支付处理失败");

        // 5. 确认预约
        final AppointmentResponse confirmedAppointment = appointmentService.confirmAppointment(initialAppointment.getId());
        assertEquals("CONFIRMED", confirmedAppointment.getStatus(), "确认后状态应为CONFIRMED");

        // 6. 完成预约
        final AppointmentResponse completedAppointment = appointmentService.completeAppointment(confirmedAppointment.getId());
        assertEquals("COMPLETED", completedAppointment.getStatus(), "完成后状态应为COMPLETED");

        // 7. 尝试取消已完成的预约
        final Long appointmentId = completedAppointment.getId();
        assertThrows(RuntimeException.class, () -> {
            appointmentService.cancelAppointment(appointmentId);
        }, "不应允许取消已完成的预约");
    }
} 