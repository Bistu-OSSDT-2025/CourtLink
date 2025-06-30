package com.courtlink.service.impl;

import com.courtlink.dto.CourtRequest;
import com.courtlink.dto.CourtResponse;
import com.courtlink.entity.Court;
import com.courtlink.enums.CourtStatus;
import com.courtlink.repository.CourtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
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
    private CourtRequest testRequest;

    @BeforeEach
    public void setUp() {
        // 准备测试数据
        testCourt = new Court();
        testCourt.setId(1L);
        testCourt.setName("Test Court");
        testCourt.setLocation("Test Location");
        testCourt.setDescription("Test Description");
        testCourt.setStatus(CourtStatus.AVAILABLE);
        testCourt.setCreatedAt(LocalDateTime.now());
        testCourt.setUpdatedAt(LocalDateTime.now());

        testRequest = new CourtRequest();
        testRequest.setName("Test Court");
        testRequest.setLocation("Test Location");
        testRequest.setDescription("Test Description");
        testRequest.setStatus(CourtStatus.AVAILABLE);
    }

    @Test
    public void testCreateCourt() {
        // Given
        when(courtRepository.save(any(Court.class))).thenReturn(testCourt);
        
        // When
        CourtResponse response = courtService.createCourt(testRequest);
        
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
    public void testGetCourt() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));
        
        // When
        CourtResponse response = courtService.getCourt(1L);
        
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
    public void testGetCourtNotFound() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            courtService.getCourt(1L);
        });
        
        verify(courtRepository).findById(1L);
    }

    @Test
    public void testGetAllCourts() {
        // Given
        List<Court> courts = Arrays.asList(testCourt);
        when(courtRepository.findAll()).thenReturn(courts);
        
        // When
        List<CourtResponse> responses = courtService.getAllCourts();
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        CourtResponse response = responses.get(0);
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
        CourtResponse response = courtService.updateCourt(1L, testRequest);
        
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
            courtService.updateCourt(1L, testRequest);
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
        when(courtRepository.searchCourts("Basketball", CourtStatus.AVAILABLE)).thenReturn(courts);
        
        // When
        List<CourtResponse> responses = courtService.searchCourts("Basketball", CourtStatus.AVAILABLE);
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        CourtResponse response = responses.get(0);
        assertEquals(testCourt.getId(), response.getId());
        assertEquals(testCourt.getName(), response.getName());
        
        verify(courtRepository).searchCourts("Basketball", CourtStatus.AVAILABLE);
    }

    @Test
    public void testChangeStatus() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.of(testCourt));
        when(courtRepository.save(any(Court.class))).thenReturn(testCourt);
        
        // When
        CourtResponse response = courtService.changeStatus(1L, CourtStatus.MAINTENANCE);
        
        // Then
        assertNotNull(response);
        assertEquals(testCourt.getId(), response.getId());
        
        verify(courtRepository).findById(1L);
        verify(courtRepository).save(any(Court.class));
    }

    @Test
    public void testChangeStatusNotFound() {
        // Given
        when(courtRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            courtService.changeStatus(1L, CourtStatus.MAINTENANCE);
        });
        
        verify(courtRepository).findById(1L);
        verify(courtRepository, never()).save(any(Court.class));
    }
} 