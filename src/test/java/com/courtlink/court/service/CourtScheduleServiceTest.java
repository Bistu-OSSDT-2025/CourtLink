package com.courtlink.court.service;

import com.courtlink.court.dto.*;
import com.courtlink.court.entity.CourtSchedule;
import com.courtlink.court.repository.CourtRepository;
import com.courtlink.court.repository.CourtScheduleRepository;
import com.courtlink.court.service.impl.CourtScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtScheduleServiceTest {
    
    @Mock
    private CourtScheduleRepository scheduleRepository;
    
    @Mock
    private CourtRepository courtRepository;
    
    @InjectMocks
    private CourtScheduleServiceImpl scheduleService;
    
    private CourtScheduleRequest validRequest;
    private CourtSchedule testSchedule;
    
    @BeforeEach
    void setUp() {
        validRequest = new CourtScheduleRequest();
        validRequest.setCourtId(1L);
        validRequest.setDayOfWeek(DayOfWeek.MONDAY);
        validRequest.setOpenTime(LocalTime.of(8, 0));
        validRequest.setCloseTime(LocalTime.of(22, 0));
        validRequest.setIsActive(true);
        validRequest.setScheduleType(CourtSchedule.ScheduleType.REGULAR);
        validRequest.setSlotDuration(60);
        validRequest.setAdvanceBookingHours(24);
        validRequest.setCancellationDeadlineHours(2);
        validRequest.setDescription("Test schedule");
        
        testSchedule = new CourtSchedule();
        testSchedule.setId(1L);
        testSchedule.setCourtId(1L);
        testSchedule.setDayOfWeek(DayOfWeek.MONDAY);
        testSchedule.setOpenTime(LocalTime.of(8, 0));
        testSchedule.setCloseTime(LocalTime.of(22, 0));
        testSchedule.setIsActive(true);
        testSchedule.setScheduleType(CourtSchedule.ScheduleType.REGULAR);
        testSchedule.setSlotDuration(60);
        testSchedule.setAdvanceBookingHours(24);
        testSchedule.setCancellationDeadlineHours(2);
        testSchedule.setDescription("Test schedule");
    }
    
    @Test
    void createSchedule_Success() {
        // Arrange
        when(courtRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.findConflictingSchedules(any(), any(), any(), any())).thenReturn(Arrays.asList());
        when(scheduleRepository.save(any(CourtSchedule.class))).thenReturn(testSchedule);
        
        // Act
        CourtScheduleDTO result = scheduleService.createSchedule(validRequest, "testAdmin");
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getCourtId());
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
        assertEquals(LocalTime.of(8, 0), result.getOpenTime());
        assertEquals(LocalTime.of(22, 0), result.getCloseTime());
        
        verify(courtRepository).existsById(1L);
        verify(scheduleRepository).save(any(CourtSchedule.class));
    }
    
    @Test
    void createSchedule_CourtNotFound() {
        // Arrange
        when(courtRepository.existsById(1L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.createSchedule(validRequest, "testAdmin");
        });
        
        verify(courtRepository).existsById(1L);
        verify(scheduleRepository, never()).save(any(CourtSchedule.class));
    }
    
    @Test
    void createSchedule_InvalidRequest() {
        // Arrange
        validRequest.setOpenTime(LocalTime.of(22, 0));
        validRequest.setCloseTime(LocalTime.of(8, 0)); // Invalid: close before open
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.createSchedule(validRequest, "testAdmin");
        });
        
        verify(scheduleRepository, never()).save(any(CourtSchedule.class));
    }
    
    @Test
    void createSchedulesBatch_Success() {
        // Arrange
        CourtScheduleBatchRequest batchRequest = new CourtScheduleBatchRequest();
        batchRequest.setCourtIds(Arrays.asList(1L, 2L));
        batchRequest.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
        batchRequest.setOpenTime(LocalTime.of(8, 0));
        batchRequest.setCloseTime(LocalTime.of(22, 0));
        batchRequest.setIsActive(true);
        batchRequest.setScheduleType(CourtSchedule.ScheduleType.REGULAR);
        batchRequest.setSlotDuration(60);
        batchRequest.setAdvanceBookingHours(24);
        batchRequest.setCancellationDeadlineHours(2);
        
        List<CourtSchedule> savedSchedules = Arrays.asList(testSchedule);
        when(scheduleRepository.saveAll(anyList())).thenReturn(savedSchedules);
        
        // Act
        List<CourtScheduleDTO> result = scheduleService.createSchedulesBatch(batchRequest, "testAdmin");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(scheduleRepository).saveAll(anyList());
    }
    
    @Test
    void getSchedulesByCourtId_Success() {
        // Arrange
        List<CourtSchedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findByCourtIdAndIsActiveTrue(1L)).thenReturn(schedules);
        
        // Act
        List<CourtScheduleDTO> result = scheduleService.getSchedulesByCourtId(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        
        verify(scheduleRepository).findByCourtIdAndIsActiveTrue(1L);
    }
    
    @Test
    void isCourtOpenAt_Success() {
        // Arrange
        LocalDate testDate = LocalDate.now();
        LocalTime testTime = LocalTime.of(10, 0);
        DayOfWeek dayOfWeek = testDate.getDayOfWeek();
        
        when(scheduleRepository.isCourtOpenAt(1L, dayOfWeek, testDate, testTime)).thenReturn(true);
        
        // Act
        boolean result = scheduleService.isCourtOpenAt(1L, testDate, testTime);
        
        // Assert
        assertTrue(result);
        
        verify(scheduleRepository).isCourtOpenAt(1L, dayOfWeek, testDate, testTime);
    }
    
    @Test
    void getAvailableTimeSlots_Success() {
        // Arrange
        LocalDate testDate = LocalDate.now();
        DayOfWeek dayOfWeek = testDate.getDayOfWeek();
        
        List<CourtSchedule> schedules = Arrays.asList(testSchedule);
        when(scheduleRepository.findEffectiveSchedules(1L, dayOfWeek, testDate)).thenReturn(schedules);
        
        // Act
        var result = scheduleService.getAvailableTimeSlots(1L, testDate);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        verify(scheduleRepository).findEffectiveSchedules(1L, dayOfWeek, testDate);
    }
    
    @Test
    void setStandardWorkingHours_Success() {
        // Arrange
        LocalTime openTime = LocalTime.of(8, 0);
        LocalTime closeTime = LocalTime.of(22, 0);
        
        when(courtRepository.existsById(1L)).thenReturn(true);
        when(scheduleRepository.saveAll(anyList())).thenReturn(Arrays.asList(testSchedule));
        
        // Act
        List<CourtScheduleDTO> result = scheduleService.setStandardWorkingHours(1L, openTime, closeTime, "testAdmin");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(courtRepository).existsById(1L);
        verify(scheduleRepository).saveAll(anyList());
    }
    
    @Test
    void updateSchedule_Success() {
        // Arrange
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(any(CourtSchedule.class))).thenReturn(testSchedule);
        
        validRequest.setDescription("Updated description");
        
        // Act
        CourtScheduleDTO result = scheduleService.updateSchedule(1L, validRequest, "testAdmin");
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        
        verify(scheduleRepository).findById(1L);
        verify(scheduleRepository).save(any(CourtSchedule.class));
    }
    
    @Test
    void updateSchedule_ScheduleNotFound() {
        // Arrange
        when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.updateSchedule(1L, validRequest, "testAdmin");
        });
        
        verify(scheduleRepository).findById(1L);
        verify(scheduleRepository, never()).save(any(CourtSchedule.class));
    }
    
    @Test
    void deleteSchedule_Success() {
        // Arrange
        when(scheduleRepository.existsById(1L)).thenReturn(true);
        
        // Act
        scheduleService.deleteSchedule(1L, "testAdmin");
        
        // Assert
        verify(scheduleRepository).existsById(1L);
        verify(scheduleRepository).deleteById(1L);
    }
    
    @Test
    void deleteSchedule_ScheduleNotFound() {
        // Arrange
        when(scheduleRepository.existsById(1L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.deleteSchedule(1L, "testAdmin");
        });
        
        verify(scheduleRepository).existsById(1L);
        verify(scheduleRepository, never()).deleteById(1L);
    }
    
    @Test
    void validateScheduleRequest_ValidRequest() {
        // Act
        boolean result = scheduleService.validateScheduleRequest(validRequest);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void validateScheduleRequest_InvalidTimeRange() {
        // Arrange
        validRequest.setOpenTime(LocalTime.of(22, 0));
        validRequest.setCloseTime(LocalTime.of(8, 0));
        
        // Act
        boolean result = scheduleService.validateScheduleRequest(validRequest);
        
        // Assert
        assertFalse(result);
    }
} 