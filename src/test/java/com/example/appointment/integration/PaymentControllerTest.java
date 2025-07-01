package com.example.appointment.integration;

import com.example.appointment.AppointmentApplication;
import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.Payment;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AppointmentApplication.class)
@AutoConfigureMockMvc
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        paymentRepository.deleteAll();
        appointment = new Appointment();
        appointment.setUserId("user1");
        appointment.setProviderId("provider1");
        appointment.setServiceType("ÓðÃ«Çò³¡");
        appointment.setStartTime(LocalDateTime.now().plusDays(1));
        appointment.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        appointment.setAmount(new BigDecimal("100.00"));
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    @Test
    void testPay_success() throws Exception {
        mockMvc.perform(post("/api/payments/pay")
                .param("appointmentId", appointment.getId().toString())
                .param("amount", "100.00"))
                .andExpect(status().isOk());
        Appointment updated = appointmentRepository.findById(appointment.getId()).orElseThrow();
        Assertions.assertEquals(Appointment.AppointmentStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void testPay_notConfirmed() throws Exception {
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        appointmentRepository.save(appointment);
        mockMvc.perform(post("/api/payments/pay")
                .param("appointmentId", appointment.getId().toString())
                .param("amount", "100.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRefund_success() throws Exception {
        // ÏÈÖ§¸¶
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        Payment payment = new Payment();
        payment.setAppointmentId(appointment.getId());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        mockMvc.perform(post("/api/payments/refund")
                .param("appointmentId", appointment.getId().toString()))
                .andExpect(status().isOk());
        Appointment updated = appointmentRepository.findById(appointment.getId()).orElseThrow();
        Assertions.assertEquals(Appointment.AppointmentStatus.CANCELLED, updated.getStatus());
    }

    @Test
    void testRefund_notCompleted() throws Exception {
        mockMvc.perform(post("/api/payments/refund")
                .param("appointmentId", appointment.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCallback_success() throws Exception {
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        mockMvc.perform(post("/api/payments/callback")
                .param("appointmentId", appointment.getId().toString()))
                .andExpect(status().isOk());
        Appointment updated = appointmentRepository.findById(appointment.getId()).orElseThrow();
        Assertions.assertEquals(Appointment.AppointmentStatus.COMPLETED, updated.getStatus());
    }
} 