package com.courtlink.controller;

import com.courtlink.dto.CourtRequest;
import com.courtlink.dto.CourtResponse;
import com.courtlink.enums.CourtStatus;
import com.courtlink.service.CourtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtControllerTest {

    @Mock
    private CourtService courtService;

    @InjectMocks
    private CourtController courtController;

    private CourtRequest testRequest;
    private CourtResponse testResponse;

    @BeforeEach
    void setUp() {
        // ׼����������
        testRequest = new CourtRequest();
        testRequest.setName("Test Court");
        testRequest.setLocation("Test Location");
        testRequest.setDescription("Test Description");
        testRequest.setStatus(CourtStatus.AVAILABLE);

        testResponse = new CourtResponse();
        testResponse.setId(1L);
        testResponse.setName("Test Court");
        testResponse.setLocation("Test Location");
        testResponse.setDescription("Test Description");
        testResponse.setStatus(CourtStatus.AVAILABLE);
    }

    @Test
    void createCourt_ShouldReturnCreatedResponse() {
        // ׼����������
        when(courtService.createCourt(any(CourtRequest.class))).thenReturn(testResponse);

        // ִ�в���
        ResponseEntity<CourtResponse> response = courtController.createCourt(testRequest);

        // ��֤���
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testResponse, response.getBody());
        verify(courtService, times(1)).createCourt(any(CourtRequest.class));
    }

    @Test
    void updateCourt_WhenCourtExists_ShouldReturnUpdatedResponse() {
        // ׼����������
        when(courtService.updateCourt(eq(1L), any(CourtRequest.class))).thenReturn(testResponse);

        // ִ�в���
        ResponseEntity<CourtResponse> response = courtController.updateCourt(1L, testRequest);

        // ��֤���
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testResponse, response.getBody());
        verify(courtService, times(1)).updateCourt(eq(1L), any(CourtRequest.class));
    }

    @Test
    void updateCourt_WhenCourtNotExists_ShouldThrowException() {
        // ׼����������
        when(courtService.updateCourt(eq(1L), any(CourtRequest.class)))
            .thenThrow(new EntityNotFoundException("Court not found"));

        // ִ�в��Բ���֤�쳣
        assertThrows(EntityNotFoundException.class, () -> 
            courtController.updateCourt(1L, testRequest)
        );

        verify(courtService, times(1)).updateCourt(eq(1L), any(CourtRequest.class));
    }

    @Test
    void deleteCourt_WhenCourtExists_ShouldReturnNoContent() {
        // ִ�в���
        ResponseEntity<Void> response = courtController.deleteCourt(1L);

        // ��֤���
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(courtService, times(1)).deleteCourt(1L);
    }

    @Test
    void deleteCourt_WhenCourtNotExists_ShouldThrowException() {
        // ׼����������
        doThrow(new EntityNotFoundException("Court not found"))
            .when(courtService).deleteCourt(1L);

        // ִ�в��Բ���֤�쳣
        assertThrows(EntityNotFoundException.class, () -> 
            courtController.deleteCourt(1L)
        );

        verify(courtService, times(1)).deleteCourt(1L);
    }

    @Test
    void getCourt_WhenCourtExists_ShouldReturnCourt() {
        // ׼����������
        when(courtService.getCourt(1L)).thenReturn(testResponse);

        // ִ�в���
        ResponseEntity<CourtResponse> response = courtController.getCourt(1L);

        // ��֤���
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testResponse, response.getBody());
        verify(courtService, times(1)).getCourt(1L);
    }

    @Test
    void getCourt_WhenCourtNotExists_ShouldThrowException() {
        // ׼����������
        when(courtService.getCourt(1L))
            .thenThrow(new EntityNotFoundException("Court not found"));

        // ִ�в��Բ���֤�쳣
        assertThrows(EntityNotFoundException.class, () -> 
            courtController.getCourt(1L)
        );

        verify(courtService, times(1)).getCourt(1L);
    }

    @Test
    void getAllCourts_ShouldReturnListOfCourts() {
        // ׼����������
        List<CourtResponse> testResponses = Arrays.asList(testResponse);
        when(courtService.getAllCourts()).thenReturn(testResponses);

        // ִ�в���
        ResponseEntity<List<CourtResponse>> response = courtController.getAllCourts();

        // ��֤���
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testResponses, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(courtService, times(1)).getAllCourts();
    }
} 