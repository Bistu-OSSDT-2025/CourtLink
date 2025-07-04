package com.courtlink.booking.service.impl;

import com.courtlink.booking.entity.Appointment;
import com.courtlink.booking.entity.CourtTimeSlot;
import com.courtlink.booking.repository.AppointmentRepository;
import com.courtlink.booking.repository.CourtTimeSlotRepository;
import com.courtlink.payment.entity.Payment;
import com.courtlink.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentScheduledService {

    private final AppointmentRepository appointmentRepository;
    private final CourtTimeSlotRepository courtTimeSlotRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 每分钟检查一次过期的预定
     * 取消10分钟内未支付的预定
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    @Transactional
    public void cancelExpiredAppointments() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        
        // 查找10分钟前创建的、状态为CONFIRMED但未支付的预定
        List<Appointment> expiredAppointments = appointmentRepository
                .findByStatusAndCreatedAtBefore(Appointment.AppointmentStatus.CONFIRMED, tenMinutesAgo);
        
        for (Appointment appointment : expiredAppointments) {
            try {
                // 检查是否已经支付
                List<Payment> payments = paymentRepository.findByAppointmentIdContaining(appointment.getId().toString());
                boolean isPaid = payments.stream()
                        .anyMatch(payment -> payment.getStatus() == Payment.PaymentStatus.COMPLETED);
                
                if (!isPaid) {
                    // 取消预定
                    appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                    appointmentRepository.save(appointment);
                    
                    // 释放时间段
                    List<CourtTimeSlot> timeSlots = appointment.getTimeSlots();
                    if (timeSlots != null && !timeSlots.isEmpty()) {
                        for (CourtTimeSlot timeSlot : timeSlots) {
                            timeSlot.setAvailable(true);
                            courtTimeSlotRepository.save(timeSlot);
                        }
                    }
                    
                    // 取消相关的待支付订单
                    payments.stream()
                            .filter(payment -> payment.getStatus() == Payment.PaymentStatus.PENDING)
                            .forEach(payment -> {
                                payment.setStatus(Payment.PaymentStatus.CANCELLED);
                                paymentRepository.save(payment);
                            });
                    
                    log.info("已取消过期预定: appointmentId={}, userId={}", 
                            appointment.getId(), appointment.getUser().getId());
                } else {
                    // 已支付，更新预定状态为已完成
                    appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
                    appointmentRepository.save(appointment);
                    log.info("预定已支付，更新状态为已完成: appointmentId={}", appointment.getId());
                }
            } catch (Exception e) {
                log.error("处理过期预定时发生错误: appointmentId={}, error={}", 
                        appointment.getId(), e.getMessage(), e);
            }
        }
        
        if (!expiredAppointments.isEmpty()) {
            log.info("处理了 {} 个过期预定", expiredAppointments.size());
        }
    }
} 