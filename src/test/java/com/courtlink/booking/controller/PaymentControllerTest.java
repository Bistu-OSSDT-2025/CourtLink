package com.courtlink.booking.controller;

import com.courtlink.booking.ApiResponse;
import com.courtlink.booking.BookingApplication;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.entity.Payment;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.repository.PaymentRepository;
import com.courtlink.booking.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PaymentController Integration Tests
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@SpringBootTest(classes = BookingApplication.class)
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    private Appointment testAppointment;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // 设置测试预约
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setUserId("test123");
        testAppointment.setProviderId("provider123");
        testAppointment.setServiceType("BADMINTON");
        testAppointment.setStartTime(LocalDateTime.now().plusDays(1));
        testAppointment.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        testAppointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        testAppointment.setAmount(new BigDecimal("100.00"));

        // 设置测试支付
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setPaymentNo("PAY123");
        testPayment.setUserId("test123");
        testPayment.setAppointmentId("1");
        testPayment.setAmount(new BigDecimal("100.00"));
        testPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        testPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        testPayment.setCreatedAt(LocalDateTime.now());
        testPayment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testProcessPayment() throws Exception {
        // 模拟依赖服务
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(paymentService.createPayment(any(Payment.class))).thenReturn(testPayment);
        when(paymentService.processPayment(any(), any())).thenReturn(testPayment);

        // 执行测试
        ResultActions result = mockMvc.perform(post("/api/payments/pay")
                .param("appointmentId", "1")
                .param("amount", "100.00")
                .param("paymentMethod", "CREDIT_CARD")
                .contentType(MediaType.APPLICATION_JSON));

        // 验证结果
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment successful, appointment completed"))
                .andExpect(jsonPath("$.data").value("PAY123"));
    }

    @Test
    void testProcessPaymentWithInvalidAppointment() throws Exception {
        // 模拟预约不存在
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试
        ResultActions result = mockMvc.perform(post("/api/payments/pay")
                .param("appointmentId", "1")
                .param("amount", "100.00")
                .param("paymentMethod", "CREDIT_CARD")
                .contentType(MediaType.APPLICATION_JSON));

        // 验证结果
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Appointment not found"));
    }

    @Test
    void testProcessPaymentWithUnconfirmedAppointment() throws Exception {
        // 设置预约状态为未确认
        testAppointment.setStatus(Appointment.AppointmentStatus.PENDING);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        // 执行测试
        ResultActions result = mockMvc.perform(post("/api/payments/pay")
                .param("appointmentId", "1")
                .param("amount", "100.00")
                .param("paymentMethod", "CREDIT_CARD")
                .contentType(MediaType.APPLICATION_JSON));

        // 验证结果
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Appointment not confirmed, cannot process payment"));
    }

    @Test
    void testProcessPaymentFailure() throws Exception {
        // 设置支付失败状态
        testPayment.setStatus(Payment.PaymentStatus.FAILED);
        
        // 模拟依赖服务
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(paymentService.createPayment(any(Payment.class))).thenReturn(testPayment);
        when(paymentService.processPayment(any(), any())).thenReturn(testPayment);

        // 执行测试
        ResultActions result = mockMvc.perform(post("/api/payments/pay")
                .param("appointmentId", "1")
                .param("amount", "100.00")
                .param("paymentMethod", "CREDIT_CARD")
                .contentType(MediaType.APPLICATION_JSON));

        // 验证结果
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payment failed"));
    }
} 