package com.courtlink.booking;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "ԤԼ����", description = "ԤԼ��ؽӿ�?)
public class AppointmentController {
    private final AppointmentService appointmentService;

    @Operation(summary = "����ԤԼ")
    @PostMapping
    public ResponseEntity<ApiResponse<Appointment>> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setUserId(request.getUserId());
        appointment.setProviderId(request.getProviderId());
        appointment.setServiceType(request.getServiceType());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setAmount(request.getAmount());
        appointment.setNotes(request.getNotes());
        Appointment created = appointmentService.createAppointment(appointment);
        return ResponseEntity.ok(new ApiResponse<>(true, "ԤԼ�ɹ�", created));
    }

    @Operation(summary = "����ID��ѯԤԼ")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Appointment>> getAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "��ѯ�ɹ�", appointment));
    }

    @Operation(summary = "ȡ��ԤԼ")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Appointment>> cancelAppointment(@PathVariable Long id, @RequestParam String reason) {
        Appointment cancelled = appointmentService.cancelAppointment(id, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "ȡ���ɹ�", cancelled));
    }

    @Operation(summary = "�鿴�ҵ�ԤԼ�����û�ID��")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getAppointmentsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, "��ѯ�ɹ�", appointmentService.getAppointmentsByUserId(userId, pageable)));
    }
} 
