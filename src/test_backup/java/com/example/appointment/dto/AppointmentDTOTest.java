package com.example.appointment.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentDTOTest {

    private AppointmentRequest request;
    private AppointmentResponse response;
    private AppointmentQuery query;

    @BeforeEach
    void setUp() {
        // ����AppointmentRequest
        request = new AppointmentRequest();
        request.setUserId(1L);
        request.setVenueId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        request.setSportType("����");
        request.setNotes("����ԤԼ");

        // ����AppointmentResponse
        response = new AppointmentResponse();
        response.setAppointmentId(1L);
        response.setUserId(1L);
        response.setVenueId(1L);
        response.setStartTime(LocalDateTime.now().plusHours(1));
        response.setEndTime(LocalDateTime.now().plusHours(2));
        response.setSportType("����");
        response.setStatus("PENDING");
        response.setAmount(new BigDecimal("100.00"));
        response.setNotes("����ԤԼ");

        // ����AppointmentQuery
        query = new AppointmentQuery();
        query.setUserId(1L);
        query.setVenueId(1L);
        query.setStatus("PENDING");
        query.setStartDate(LocalDateTime.now().toLocalDate());
        query.setEndDate(LocalDateTime.now().plusDays(7).toLocalDate());
    }

    @Test
    void testAppointmentRequest() {
        assertNotNull(request);
        assertEquals(1L, request.getUserId());
        assertEquals(1L, request.getVenueId());
        assertEquals("����", request.getSportType());
        assertEquals("����ԤԼ", request.getNotes());
        assertNotNull(request.getStartTime());
        assertNotNull(request.getEndTime());
        assertTrue(request.getEndTime().isAfter(request.getStartTime()));
    }

    @Test
    void testAppointmentRequestSettersAndGetters() {
        request.setUserId(2L);
        request.setVenueId(3L);
        request.setSportType("����");
        request.setNotes("���º��ԤԼ");

        assertEquals(2L, request.getUserId());
        assertEquals(3L, request.getVenueId());
        assertEquals("����", request.getSportType());
        assertEquals("���º��ԤԼ", request.getNotes());
    }

    @Test
    void testAppointmentResponse() {
        assertNotNull(response);
        assertEquals(1L, response.getAppointmentId());
        assertEquals(1L, response.getUserId());
        assertEquals(1L, response.getVenueId());
        assertEquals("����", response.getSportType());
        assertEquals("PENDING", response.getStatus());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("����ԤԼ", response.getNotes());
    }

    @Test
    void testAppointmentResponseSettersAndGetters() {
        response.setAppointmentId(2L);
        response.setUserId(3L);
        response.setVenueId(4L);
        response.setSportType("����");
        response.setStatus("CONFIRMED");
        response.setAmount(new BigDecimal("150.00"));
        response.setNotes("���º����Ӧ");

        assertEquals(2L, response.getAppointmentId());
        assertEquals(3L, response.getUserId());
        assertEquals(4L, response.getVenueId());
        assertEquals("����", response.getSportType());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(new BigDecimal("150.00"), response.getAmount());
        assertEquals("���º����Ӧ", response.getNotes());
    }

    @Test
    void testAppointmentQuery() {
        assertNotNull(query);
        assertEquals(1L, query.getUserId());
        assertEquals(1L, query.getVenueId());
        assertEquals("PENDING", query.getStatus());
        assertNotNull(query.getStartDate());
        assertNotNull(query.getEndDate());
    }

    @Test
    void testAppointmentQuerySettersAndGetters() {
        query.setUserId(2L);
        query.setVenueId(3L);
        query.setStatus("CONFIRMED");
        query.setStartDate(LocalDateTime.now().plusDays(1).toLocalDate());
        query.setEndDate(LocalDateTime.now().plusDays(14).toLocalDate());

        assertEquals(2L, query.getUserId());
        assertEquals(3L, query.getVenueId());
        assertEquals("CONFIRMED", query.getStatus());
        assertNotNull(query.getStartDate());
        assertNotNull(query.getEndDate());
    }

    @Test
    void testTimeValidation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);

        request.setStartTime(startTime);
        request.setEndTime(endTime);

        assertEquals(startTime, request.getStartTime());
        assertEquals(endTime, request.getEndTime());
        assertTrue(request.getEndTime().isAfter(request.getStartTime()));
    }

    @Test
    void testAmountValidation() {
        response.setAmount(new BigDecimal("0.00"));
        assertEquals(new BigDecimal("0.00"), response.getAmount());

        response.setAmount(new BigDecimal("999.99"));
        assertEquals(new BigDecimal("999.99"), response.getAmount());

        response.setAmount(new BigDecimal("-50.00"));
        assertEquals(new BigDecimal("-50.00"), response.getAmount());
    }

    @Test
    void testToString() {
        String requestToString = request.toString();
        String responseToString = response.toString();
        String queryToString = query.toString();

        assertNotNull(requestToString);
        assertNotNull(responseToString);
        assertNotNull(queryToString);

        assertTrue(requestToString.contains("AppointmentRequest"));
        assertTrue(responseToString.contains("AppointmentResponse"));
        assertTrue(queryToString.contains("AppointmentQuery"));
    }

    @Test
    void testEqualsAndHashCode() {
        AppointmentRequest request1 = new AppointmentRequest();
        request1.setUserId(1L);
        request1.setVenueId(1L);

        AppointmentRequest request2 = new AppointmentRequest();
        request2.setUserId(1L);
        request2.setVenueId(1L);

        AppointmentRequest request3 = new AppointmentRequest();
        request3.setUserId(2L);
        request3.setVenueId(1L);

        // ����equals����
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);

        // ����hashCode����
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }
} 