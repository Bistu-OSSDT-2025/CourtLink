package com.example.appointment.repository;

import com.example.appointment.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 预约数据访问层接口
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * 根据用户ID查询预约列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 预约分页列表
     */
    Page<Appointment> findByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);

    /**
     * 根据服务提供者ID查询预约列表
     * 
     * @param providerId 服务提供者ID
     * @param pageable 分页参数
     * @return 预约分页列表
     */
    Page<Appointment> findByProviderIdOrderByStartTimeDesc(String providerId, Pageable pageable);

    /**
     * 根据状态查询预约列表
     * 
     * @param status 预约状态
     * @param pageable 分页参数
     * @return 预约分页列表
     */
    Page<Appointment> findByStatusOrderByStartTimeDesc(Appointment.AppointmentStatus status, Pageable pageable);

    /**
     * 查询指定时间范围内的预约
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 预约状态
     * @return 预约列表
     */
    @Query("SELECT a FROM Appointment a WHERE a.startTime >= :startTime AND a.endTime <= :endTime AND a.status = :status")
    List<Appointment> findByTimeRangeAndStatus(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("status") Appointment.AppointmentStatus status);

    /**
     * 检查时间冲突
     * 
     * @param providerId 服务提供者ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeId 排除的预约ID（用于更新时）
     * @return 冲突的预约列表
     */
    @Query("SELECT a FROM Appointment a WHERE a.providerId = :providerId " +
           "AND a.status NOT IN ('CANCELLED', 'EXPIRED') " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime) " +
           "OR (a.startTime = :startTime AND a.endTime = :endTime)) " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    List<Appointment> findConflictingAppointments(@Param("providerId") String providerId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("excludeId") Long excludeId);

    /**
     * 查询即将到期的预约（用于发送提醒）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 预约状态
     * @return 即将到期的预约列表
     */
    @Query("SELECT a FROM Appointment a WHERE a.startTime BETWEEN :startTime AND :endTime " +
           "AND a.status = :status")
    List<Appointment> findUpcomingAppointments(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("status") Appointment.AppointmentStatus status);

    /**
     * 查询过期的预约
     * 
     * @param currentTime 当前时间
     * @param status 预约状态
     * @return 过期的预约列表
     */
    @Query("SELECT a FROM Appointment a WHERE a.endTime < :currentTime AND a.status = :status")
    List<Appointment> findExpiredAppointments(@Param("currentTime") LocalDateTime currentTime,
                                             @Param("status") Appointment.AppointmentStatus status);

    /**
     * 根据支付ID查询预约
     * 
     * @param paymentId 支付ID
     * @return 预约信息
     */
    Optional<Appointment> findByPaymentId(String paymentId);

    /**
     * 统计用户预约数量
     * 
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约数量
     */
    long countByUserIdAndStatus(String userId, Appointment.AppointmentStatus status);

    /**
     * 统计服务提供者预约数量
     * 
     * @param providerId 服务提供者ID
     * @param status 预约状态
     * @return 预约数量
     */
    long countByProviderIdAndStatus(String providerId, Appointment.AppointmentStatus status);
} 