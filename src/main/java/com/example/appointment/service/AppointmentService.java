package com.example.appointment.service;

import com.example.appointment.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约管理服务接口
 * 
 * @author Your Name
 * @version 1.0.0
 */
public interface AppointmentService {

    /**
     * 创建预约
     * 
     * @param appointment 预约信息
     * @return 创建的预约
     */
    Appointment createAppointment(Appointment appointment);

    /**
     * 更新预约
     * 
     * @param id 预约ID
     * @param appointment 预约信息
     * @return 更新后的预约
     */
    Appointment updateAppointment(Long id, Appointment appointment);

    /**
     * 取消预约
     * 
     * @param id 预约ID
     * @param reason 取消原因
     * @return 取消后的预约
     */
    Appointment cancelAppointment(Long id, String reason);

    /**
     * 确认预约
     * 
     * @param id 预约ID
     * @return 确认后的预约
     */
    Appointment confirmAppointment(Long id);

    /**
     * 完成预约
     * 
     * @param id 预约ID
     * @return 完成后的预约
     */
    Appointment completeAppointment(Long id);

    /**
     * 根据ID查询预约
     * 
     * @param id 预约ID
     * @return 预约信息
     */
    Appointment getAppointmentById(Long id);

    /**
     * 根据用户ID查询预约列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 预约分页列表
     */
    Page<Appointment> getAppointmentsByUserId(String userId, Pageable pageable);

    /**
     * 根据服务提供者ID查询预约列表
     * 
     * @param providerId 服务提供者ID
     * @param pageable 分页参数
     * @return 预约分页列表
     */
    Page<Appointment> getAppointmentsByProviderId(String providerId, Pageable pageable);

    /**
     * 根据状态查询预约列表
     * 
     * @param status 预约状态
     * @param pageable 分页参数
     * @return 预约分页列表
     */
    Page<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status, Pageable pageable);

    /**
     * 检查时间冲突
     * 
     * @param providerId 服务提供者ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeId 排除的预约ID
     * @return 是否存在冲突
     */
    boolean hasTimeConflict(String providerId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId);

    /**
     * 查询指定时间范围内的预约
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 预约状态
     * @return 预约列表
     */
    List<Appointment> getAppointmentsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, 
                                                Appointment.AppointmentStatus status);

    /**
     * 查询即将到期的预约（用于发送提醒）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 预约状态
     * @return 即将到期的预约列表
     */
    List<Appointment> getUpcomingAppointments(LocalDateTime startTime, LocalDateTime endTime, 
                                             Appointment.AppointmentStatus status);

    /**
     * 清理过期预约
     * 
     * @return 清理的预约数量
     */
    int cleanupExpiredAppointments();

    /**
     * 发送预约提醒
     * 
     * @param appointment 预约信息
     */
    void sendAppointmentReminder(Appointment appointment);

    /**
     * 发送预约通知
     * 
     * @param appointment 预约信息
     * @param notificationType 通知类型
     */
    void sendAppointmentNotification(Appointment appointment, String notificationType);

    /**
     * 统计用户预约数量
     * 
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约数量
     */
    long countAppointmentsByUserIdAndStatus(String userId, Appointment.AppointmentStatus status);

    /**
     * 统计服务提供者预约数量
     * 
     * @param providerId 服务提供者ID
     * @param status 预约状态
     * @return 预约数量
     */
    long countAppointmentsByProviderIdAndStatus(String providerId, Appointment.AppointmentStatus status);
} 