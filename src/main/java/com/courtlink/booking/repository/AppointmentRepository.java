package com.courtlink.booking.repository;

import com.courtlink.booking.entity.Appointment;
import com.courtlink.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
<<<<<<< HEAD
import java.time.LocalDateTime;
=======
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 查找用户的所有预约
    List<Appointment> findByUserOrderByAppointmentDateDescCreatedAtDesc(User user);

    // 查找用户在特定日期的预约
    List<Appointment> findByUserAndAppointmentDate(User user, LocalDate date);

    // 查找特定场地在特定日期的预约
    @Query("SELECT a FROM Appointment a WHERE a.court.id = :courtId AND a.appointmentDate = :date")
    List<Appointment> findByCourtIdAndDate(@Param("courtId") Long courtId, @Param("date") LocalDate date);

    // 检查时间段冲突
    @Query("SELECT a FROM Appointment a WHERE a.court.id = :courtId AND a.appointmentDate = :date " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime) " +
           "AND a.status IN ('PENDING', 'PAID', 'CONFIRMED')")
    List<Appointment> findConflictingAppointments(@Param("courtId") Long courtId, 
                                                 @Param("date") LocalDate date,
                                                 @Param("startTime") LocalTime startTime, 
                                                 @Param("endTime") LocalTime endTime);

    // 查找用户在指定日期和场地的预约数量
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user = :user AND a.court.id = :courtId " +
           "AND a.appointmentDate = :date AND a.status IN ('PENDING', 'PAID', 'CONFIRMED')")
    long countUserAppointmentsOnCourtAndDate(@Param("user") User user, 
                                           @Param("courtId") Long courtId, 
                                           @Param("date") LocalDate date);

    // 统计用户活跃预约数量
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user = :user " +
           "AND a.status IN ('PENDING', 'PAID', 'CONFIRMED')")
    long countActiveAppointmentsByUser(@Param("user") User user);
<<<<<<< HEAD

    /**
     * 查找指定用户的预定
     */
    List<Appointment> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 查找指定状态和创建时间之前的预定
     */
    List<Appointment> findByStatusAndCreatedAtBefore(Appointment.AppointmentStatus status, LocalDateTime createdAt);
=======
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 