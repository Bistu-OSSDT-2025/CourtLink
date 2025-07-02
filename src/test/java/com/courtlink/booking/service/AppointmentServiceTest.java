package com.courtlink.booking.service;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AppointmentService Unit Tests
 * 
 * @author CourtLink Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Appointment Service Tests")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentRequest appointmentRequest;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        // Prepare test data
        appointmentRequest = new AppointmentRequest();
        appointmentRequest.setProviderId("provider123");
        appointmentRequest.setServiceType("court_booking");
        // 设置为明天的整点时间
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1)
            .withHour(14)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        appointmentRequest.setStartTime(tomorrow);
        appointmentRequest.setEndTime(tomorrow.plusHours(1));
        appointmentRequest.setAmount(new BigDecimal("50.00"));
        appointmentRequest.setNotes("Test appointment");

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setUserId("user123");
        appointment.setProviderId("provider123");
        appointment.setServiceType("court_booking");
        appointment.setStartTime(tomorrow);
        appointment.setEndTime(tomorrow.plusHours(1));
        appointment.setAmount(new BigDecimal("50.00"));
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        appointment.setNotes("Test appointment");
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create appointment successfully")
    void shouldCreateAppointmentSuccessfully() {
        // Given
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        AppointmentResponse response = appointmentService.createAppointment("user123", appointmentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo("user123");
        assertThat(response.getStatus()).isEqualTo(Appointment.AppointmentStatus.PENDING.name());
        
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should check for conflicts before creating appointment")
    void shouldCheckForConflictsBeforeCreatingAppointment() {
        // Given
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        AppointmentResponse response = appointmentService.createAppointment("user123", appointmentRequest);

        // Then
        assertThat(response).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should get appointment by ID successfully")
    void shouldGetAppointmentByIdSuccessfully() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        // When
        AppointmentResponse response = appointmentService.getAppointmentById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo("user123");
        
        verify(appointmentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when appointment not found")
    void shouldThrowExceptionWhenAppointmentNotFound() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.getAppointmentById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(appointmentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get appointments by user ID")
    void shouldGetAppointmentsByUserId() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        Page<Appointment> page = new PageImpl<>(appointments);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(appointmentRepository.findByUserId("user123", pageable)).thenReturn(page);

        // When
        Page<AppointmentResponse> response = appointmentService.getAppointmentsByUserId("user123", pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getUserId()).isEqualTo("user123");
        
        verify(appointmentRepository).findByUserId("user123", pageable);
    }

    @Test
    @DisplayName("Should cancel appointment successfully")
    void shouldCancelAppointmentSuccessfully() {
        // Given
        Appointment cancelledAppointment = new Appointment();
        BeanUtils.copyProperties(appointment, cancelledAppointment);
        cancelledAppointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(cancelledAppointment);

        // When
        AppointmentResponse response = appointmentService.cancelAppointment(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Appointment.AppointmentStatus.CANCELLED.name());
        
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should complete appointment successfully")
    void shouldCompleteAppointmentSuccessfully() {
        // Given
        Appointment completedAppointment = new Appointment();
        BeanUtils.copyProperties(appointment, completedAppointment);
        completedAppointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(completedAppointment);

        // When
        AppointmentResponse response = appointmentService.completeAppointment(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Appointment.AppointmentStatus.COMPLETED.name());
        
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should update appointment successfully")
    void shouldUpdateAppointmentSuccessfully() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        AppointmentResponse response = appointmentService.updateAppointment(1L, appointmentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProviderId()).isEqualTo(appointmentRequest.getProviderId());
        assertThat(response.getServiceType()).isEqualTo(appointmentRequest.getServiceType());
        
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should get appointments by provider ID")
    void shouldGetAppointmentsByProviderId() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        Page<Appointment> page = new PageImpl<>(appointments);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(appointmentRepository.findByProviderId("provider123", pageable)).thenReturn(page);

        // When
        Page<AppointmentResponse> response = appointmentService.getAppointmentsByProviderId("provider123", pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getProviderId()).isEqualTo("provider123");
        
        verify(appointmentRepository).findByProviderId("provider123", pageable);
    }

    @Test
    @DisplayName("Should get appointments by status")
    void shouldGetAppointmentsByStatus() {
        // Given
        List<Appointment> appointments = Arrays.asList(appointment);
        Page<Appointment> page = new PageImpl<>(appointments);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(appointmentRepository.findByStatus(Appointment.AppointmentStatus.PENDING, pageable)).thenReturn(page);

        // When
        Page<AppointmentResponse> response = appointmentService.getAppointmentsByStatus(Appointment.AppointmentStatus.PENDING, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getStatus()).isEqualTo(Appointment.AppointmentStatus.PENDING.name());
        
        verify(appointmentRepository).findByStatus(Appointment.AppointmentStatus.PENDING, pageable);
    }

    @Test
    @DisplayName("Should throw exception when start time is not on the hour")
    void shouldThrowExceptionWhenStartTimeIsNotOnTheHour() {
        // Given
        LocalDateTime invalidStartTime = LocalDateTime.now().plusDays(1)
            .withHour(14)
            .withMinute(30)
            .withSecond(0);
        appointmentRequest.setStartTime(invalidStartTime);
        appointmentRequest.setEndTime(invalidStartTime.plusHours(1));

        // When & Then
        assertThatThrownBy(() -> appointmentService.createAppointment("user123", appointmentRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("预约开始时间必须是整点（如：14:00）");
    }

    @Test
    @DisplayName("Should throw exception when end time is not on the hour")
    void shouldThrowExceptionWhenEndTimeIsNotOnTheHour() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1)
            .withHour(14)
            .withMinute(0)
            .withSecond(0);
        LocalDateTime invalidEndTime = startTime.plusMinutes(90);
        appointmentRequest.setStartTime(startTime);
        appointmentRequest.setEndTime(invalidEndTime);

        // When & Then
        assertThatThrownBy(() -> appointmentService.createAppointment("user123", appointmentRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("预约结束时间必须是整点（如：15:00）");
    }

    @Test
    @DisplayName("Should throw exception when booking duration is less than 1 hour")
    void shouldThrowExceptionWhenBookingDurationIsLessThanOneHour() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1)
            .withHour(14)
            .withMinute(0)
            .withSecond(0);
        LocalDateTime endTime = startTime.plusMinutes(30);
        appointmentRequest.setStartTime(startTime);
        appointmentRequest.setEndTime(endTime);

        // When & Then
        assertThatThrownBy(() -> appointmentService.createAppointment("user123", appointmentRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("预约时长必须在1-2小时之间");
    }

    @Test
    @DisplayName("Should throw exception when booking duration is more than 2 hours")
    void shouldThrowExceptionWhenBookingDurationIsMoreThanTwoHours() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1)
            .withHour(14)
            .withMinute(0)
            .withSecond(0);
        LocalDateTime endTime = startTime.plusHours(3);
        appointmentRequest.setStartTime(startTime);
        appointmentRequest.setEndTime(endTime);

        // When & Then
        assertThatThrownBy(() -> appointmentService.createAppointment("user123", appointmentRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("预约时长必须在1-2小时之间");
    }

    @Test
    @DisplayName("Should create appointment successfully with valid time range")
    void shouldCreateAppointmentSuccessfullyWithValidTimeRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1)
            .withHour(14)
            .withMinute(0)
            .withSecond(0);
        LocalDateTime endTime = startTime.plusHours(2);
        appointmentRequest.setStartTime(startTime);
        appointmentRequest.setEndTime(endTime);
        
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // When
        AppointmentResponse response = appointmentService.createAppointment("user123", appointmentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo("user123");
        assertThat(response.getStatus()).isEqualTo(Appointment.AppointmentStatus.PENDING.name());
        
        verify(appointmentRepository).save(any(Appointment.class));
    }
} 