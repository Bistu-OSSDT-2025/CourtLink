package com.example.appointment.service;

import com.example.appointment.dto.AppointmentRequest;
import com.example.appointment.dto.AppointmentResponse;
import com.example.appointment.dto.AppointmentQuery;
import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.Payment;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.PaymentRepository;
import com.example.appointment.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentRequest validRequest;
    private Appointment existingAppointment;
    private Payment existingPayment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 设置有效的预约请求
        validRequest = new AppointmentRequest();
        validRequest.setUserId(1L);
        validRequest.setVenueId(1L);
        validRequest.setStartTime(LocalDateTime.now().plusHours(1));
        validRequest.setEndTime(LocalDateTime.now().plusHours(2));
        validRequest.setSportType("篮球");
        validRequest.setNotes("测试预约");

        // 设置现有预约
        existingAppointment = new Appointment();
        existingAppointment.setId(1L);
        existingAppointment.setUserId(1L);
        existingAppointment.setVenueId(1L);
        existingAppointment.setStartTime(LocalDateTime.now().plusHours(1));
        existingAppointment.setEndTime(LocalDateTime.now().plusHours(2));
        existingAppointment.setSportType("篮球");
        existingAppointment.setStatus("PENDING");
        existingAppointment.setCreatedAt(LocalDateTime.now());

        // 设置现有支付
        existingPayment = new Payment();
        existingPayment.setId(1L);
        existingPayment.setAppointmentId(1L);
        existingPayment.setAmount(new BigDecimal("100.00"));
        existingPayment.setStatus("PENDING");
    }

    @Test
    void testCreateAppointment_Success() {
        // Given
        when(appointmentRepository.findByVenueIdAndTimeConflict(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(existingAppointment);

        // When
        AppointmentResponse response = appointmentService.createAppointment(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getAppointmentId());
        assertEquals("PENDING", response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_TimeConflict() {
        // Given
        when(appointmentRepository.findByVenueIdAndTimeConflict(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(existingAppointment));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(validRequest);
        });
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_InvalidTime() {
        // Given
        validRequest.setStartTime(LocalDateTime.now().minusHours(1));
        validRequest.setEndTime(LocalDateTime.now());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(validRequest);
        });
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testGetAppointment_Success() {
        // Given
        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(existingAppointment));

        // When
        AppointmentResponse response = appointmentService.getAppointment(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getAppointmentId());
        assertEquals("篮球", response.getSportType());
    }

    @Test
    void testGetAppointment_NotFound() {
        // Given
        when(appointmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            appointmentService.getAppointment(999L);
        });
    }

    @Test
    void testUpdateAppointment_Success() {
        // Given
        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(existingAppointment));
        when(appointmentRepository.findByVenueIdAndTimeConflict(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(existingAppointment);

        // When
        AppointmentResponse response = appointmentService.updateAppointment(1L, validRequest);

        // Then
        assertNotNull(response);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testCancelAppointment_Success() {
        // Given
        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(existingAppointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(existingAppointment);

        // When
        AppointmentResponse response = appointmentService.cancelAppointment(1L);

        // Then
        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testConfirmAppointment_Success() {
        // Given
        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(existingAppointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(existingAppointment);

        // When
        AppointmentResponse response = appointmentService.confirmAppointment(1L);

        // Then
        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testCompleteAppointment_Success() {
        // Given
        existingAppointment.setStatus("CONFIRMED");
        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(existingAppointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenReturn(existingAppointment);

        // When
        AppointmentResponse response = appointmentService.completeAppointment(1L);

        // Then
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testQueryAppointments_Success() {
        // Given
        AppointmentQuery query = new AppointmentQuery();
        query.setUserId(1L);
        query.setStatus("PENDING");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> page = new PageImpl<>(Arrays.asList(existingAppointment));

        when(appointmentRepository.findByUserIdAndStatus(1L, "PENDING", pageable))
                .thenReturn(page);

        // When
        Page<AppointmentResponse> response = appointmentService.queryAppointments(query, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(appointmentRepository, times(1)).findByUserIdAndStatus(1L, "PENDING", pageable);
    }

    @Test
    void testGetAppointmentStatistics_Success() {
        // Given
        when(appointmentRepository.countByStatus("PENDING")).thenReturn(5L);
        when(appointmentRepository.countByStatus("CONFIRMED")).thenReturn(10L);
        when(appointmentRepository.countByStatus("COMPLETED")).thenReturn(20L);
        when(appointmentRepository.countByStatus("CANCELLED")).thenReturn(3L);

        // When
        var statistics = appointmentService.getAppointmentStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(5L, statistics.get("PENDING"));
        assertEquals(10L, statistics.get("CONFIRMED"));
        assertEquals(20L, statistics.get("COMPLETED"));
        assertEquals(3L, statistics.get("CANCELLED"));
    }

    @Test
    void testCancelAppointment_alreadyCancelled() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        Assertions.assertThrows(RuntimeException.class, () -> {
            appointmentService.cancelAppointment(1L, "重复取消");
        });
    }

    @Test
    void testCancelAppointment_success() {
        Appointment appointment = new Appointment();
        appointment.setId(2L);
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        when(appointmentRepository.findById(2L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(appointment);
        Appointment result = appointmentService.cancelAppointment(2L, "用户主动取消");
        Assertions.assertEquals(Appointment.AppointmentStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testGetAppointmentById_notFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        Assertions.assertThrows(RuntimeException.class, () -> {
            appointmentService.getAppointmentById(99L);
        });
    }
} 