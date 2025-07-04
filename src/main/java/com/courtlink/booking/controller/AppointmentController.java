package com.courtlink.booking.controller;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.service.AppointmentService;
import com.courtlink.user.entity.User;
import com.courtlink.user.repository.UserRepository;
import com.courtlink.security.JwtService;
import com.courtlink.payment.service.impl.PaymentServiceImpl;
import com.courtlink.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PaymentServiceImpl paymentService;

    /**
     * 创建预约
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(
            @RequestBody AppointmentRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // 1. 验证用户身份
            String jwt = token.substring(7);
            String username = jwtService.extractUsername(jwt);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 2. 创建预定
            AppointmentResponse appointmentResponse = appointmentService.createAppointment(request, user);
            
            // 3. 创建支付记录
            Payment payment = paymentService.createPayment(
                    user.getId(),
                    BigDecimal.valueOf(appointmentResponse.getTotalPrice()),
                    Payment.PaymentMethod.WECHAT,
                    List.of(Map.of(
                            "appointmentId", appointmentResponse.getId(),
                            "courtName", appointmentResponse.getCourtName(),
                            "date", appointmentResponse.getAppointmentDate(),
                            "startTime", appointmentResponse.getStartTime(),
                            "endTime", appointmentResponse.getEndTime()
                    )),
                    "法院预定费用"
            );
            
            // 4. 返回预定信息和支付信息
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "预定成功，请在10分钟内完成支付");
            response.put("appointment", appointmentResponse);
            response.put("payment", Map.of(
                "paymentId", payment.getPaymentId(),
                "amount", payment.getAmount(),
                "status", payment.getStatus(),
                "expireTime", payment.getCreatedAt().plusMinutes(10)
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建预定失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "预定失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取我的预约列表
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getCurrentUser(userDetails);
            List<AppointmentResponse> appointments = appointmentService.getUserAppointments(user);
            
            response.put("success", true);
            response.put("data", appointments);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取预约列表出现错误", e);
            response.put("success", false);
            response.put("message", "获取预约列表失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据ID获取预约详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAppointmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getCurrentUser(userDetails);
            AppointmentResponse appointment = appointmentService.getAppointmentById(id, user);
            
            response.put("success", true);
            response.put("data", appointment);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("获取预约详情出现错误", e);
            response.put("success", false);
            response.put("message", "获取预约详情失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 取消预约
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getCurrentUser(userDetails);
            appointmentService.cancelAppointment(id, user);
            
            response.put("success", true);
            response.put("message", "预约取消成功");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("取消预约出现错误", e);
            response.put("success", false);
            response.put("message", "取消预约失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 验证预约请求（用于前端实时验证）
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = getCurrentUser(userDetails);
            appointmentService.validateAppointmentRequest(request, user);
            
            response.put("success", true);
            response.put("message", "预约请求有效");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("验证预约请求出现错误", e);
            response.put("success", false);
            response.put("message", "验证失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 从UserDetails获取当前用户
     */
    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("用户未认证");
        }
        
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userDetails.getUsername()));
    }
} 