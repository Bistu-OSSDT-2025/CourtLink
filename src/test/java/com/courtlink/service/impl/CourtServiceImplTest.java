package com.courtlink.service.impl;

import com.courtlink.dto.CourtDTO;
import com.courtlink.entity.Court;
import com.courtlink.repository.CourtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourtServiceImplTest {

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtServiceImpl courtService;

    private Court testCourt;
    private CourtDTO testDTO;

    @BeforeEach
    public void setUp() {
        // 准备测试数据
        testCourt = new Court();
        testCourt.setId(1L);
        testCourt.setName("Test Court");
        testCourt.setLocation("Test Location");
        testCourt.setDescription("Test Description");
        testCourt.setStatus("AVAILABLE");
        testCourt.setPricePerHour(100.0);
        testCourt.setFacilities("Lighting, Shower");
        testCourt.setOpeningHours("09:00-22:00");
        testCourt.setMaintenanceSchedule("Monday 08:00-09:00");
        testCourt.setIsActive(true);

        testDTO = new CourtDTO();
        testDTO.setName("Test Court");
        testDTO.setLocation("Test Location");
        testDTO.setDescription("Test Description");
        testDTO.setStatus("AVAILABLE");
        testDTO.setPricePerHour(100.0);
        testDTO.setFacilities("Lighting, Shower");
        testDTO.setOpeningHours("09:00-22:00");
        testDTO.setMaintenanceSchedule("Monday 08:00-09:00");
        testDTO.setIsActive(true);
    }

    @Test
    public void testCreateCourt() {
        // Given
        when(courtRepository.save(any(Court.class))).thenReturn(testCourt);
        
        // When
        CourtDTO response = courtService.createCourt(testDTO);
        
        // Then
        assertNotNull(response);
        assertEquals(testCourt.getId(), response.getId());
        assertEquals(testCourt.getName(), response.getName());
        assertEquals(testCourt.getLocation(), response.getLocation());
        assertEquals(testCourt.getDescription(), response.getDescription());
        assertEquals(testCourt.getStatus(), response.getStatus());
        
        verify(courtRepository).save(any(Court.class));
    }

    @Test
    public void testGetCourtById() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));
        
        // When
        CourtDTO response = courtService.getCourtById(1L);
        
        // Then
        assertNotNull(response);
        assertEquals(testCourt.getId(), response.getId());
        assertEquals(testCourt.getName(), response.getName());
        assertEquals(testCourt.getLocation(), response.getLocation());
        assertEquals(testCourt.getDescription(), response.getDescription());
        assertEquals(testCourt.getStatus(), response.getStatus());
        
        verify(courtRepository).findById(1L);
    }

    @Test
    public void testGetCourtByIdNotFound() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            courtService.getCourtById(1L);
        });
        
        verify(courtRepository).findById(1L);
    }

    @Test
    public void testGetAllCourts() {
        // Given
        List<Court> courts = Arrays.asList(testCourt);
        when(courtRepository.findAll()).thenReturn(courts);
        
        // When
        List<CourtDTO> responses = courtService.getAllCourts();
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        CourtDTO response = responses.get(0);
        assertEquals(testCourt.getId(), response.getId());
        assertEquals(testCourt.getName(), response.getName());
        assertEquals(testCourt.getLocation(), response.getLocation());
        assertEquals(testCourt.getDescription(), response.getDescription());
        assertEquals(testCourt.getStatus(), response.getStatus());
        
        verify(courtRepository).findAll();
    }

    @Test
    public void testUpdateCourt() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));
        when(courtRepository.save(any(Court.class))).thenReturn(testCourt);
        
        // When
        CourtDTO response = courtService.updateCourt(1L, testDTO);
        
        // Then
        assertNotNull(response);
        assertEquals(testCourt.getId(), response.getId());
        assertEquals(testCourt.getName(), response.getName());
        assertEquals(testCourt.getLocation(), response.getLocation());
        assertEquals(testCourt.getDescription(), response.getDescription());
        assertEquals(testCourt.getStatus(), response.getStatus());
        
        verify(courtRepository).findById(1L);
        verify(courtRepository).save(any(Court.class));
    }

    @Test
    public void testUpdateCourtNotFound() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            courtService.updateCourt(1L, testDTO);
        });
        
        verify(courtRepository).findById(1L);
        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    public void testDeleteCourt() {
        // Given
        when(courtRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courtRepository).deleteById(1L);
        
        // When
        courtService.deleteCourt(1L);
        
        // Then
        verify(courtRepository).existsById(1L);
        verify(courtRepository).deleteById(1L);
    }

    @Test
    public void testDeleteCourtNotFound() {
        // Given
        when(courtRepository.existsById(1L)).thenReturn(false);
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            courtService.deleteCourt(1L);
        });
        
        verify(courtRepository).existsById(1L);
        verify(courtRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testSearchCourts() {
        // Given
        List<Court> courts = Arrays.asList(testCourt);
        when(courtRepository.searchCourts("Basketball", "AVAILABLE")).thenReturn(courts);
        
        // When
        List<CourtDTO> responses = courtService.searchCourts("Basketball", "AVAILABLE");
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        CourtDTO response = responses.get(0);
        assertEquals(testCourt.getId(), response.getId());
        assertEquals(testCourt.getName(), response.getName());
        assertEquals(testCourt.getLocation(), response.getLocation());
        assertEquals(testCourt.getDescription(), response.getDescription());
        assertEquals(testCourt.getStatus(), response.getStatus());
        
        verify(courtRepository).searchCourts("Basketball", "AVAILABLE");
    }

    @Test
    public void testUpdateCourtStatus() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));
        when(courtRepository.save(any(Court.class))).thenReturn(testCourt);
        
        // When
        CourtDTO response = courtService.updateCourtStatus(1L, "MAINTENANCE");
        
        // Then
        assertNotNull(response);
        assertEquals("MAINTENANCE", response.getStatus());
        
        verify(courtRepository).findById(1L);
        verify(courtRepository).save(any(Court.class));
    }

    @Test
    public void testUpdateCourtStatusNotFound() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            courtService.updateCourtStatus(1L, "MAINTENANCE");
        });
        
        verify(courtRepository).findById(1L);
        verify(courtRepository, never()).save(any(Court.class));
    }
} 