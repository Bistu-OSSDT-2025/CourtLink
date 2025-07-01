package com.example.appointment;

import com.example.appointment.dto.AppointmentRequest;
import com.example.appointment.entity.Appointment;
import com.example.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "ԤԼ����", description = "ԤԼ��ؽӿ�")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @Operation(summary = "����ԤԼ")
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setUserId(request.getUserId());
        appointment.setProviderId(request.getProviderId());
        appointment.setServiceType(request.getServiceType());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setAmount(request.getAmount());
        appointment.setNotes(request.getNotes());
        Appointment created = appointmentService.createAppointment(appointment);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "����ID��ѯԤԼ")
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @Operation(summary = "ȡ��ԤԼ")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, reason));
    }

    @Operation(summary = "�鿴�ҵ�ԤԼ�����û�ID��")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAppointmentsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(appointmentService.getAppointmentsByUserId(userId, pageable));
    }
} 