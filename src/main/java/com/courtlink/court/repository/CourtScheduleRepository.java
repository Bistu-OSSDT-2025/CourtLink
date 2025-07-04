package com.courtlink.court.repository;

import com.courtlink.court.entity.CourtSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourtScheduleRepository extends JpaRepository<CourtSchedule, Long> {
    
    /**
     * 根据场地ID查找所有时间表
     */
    List<CourtSchedule> findByCourtIdAndIsActiveTrue(Long courtId);
    
    /**
     * 根据场地ID和星期几查找时间表
     */
    Optional<CourtSchedule> findByCourtIdAndDayOfWeekAndIsActiveTrueAndScheduleType(
            Long courtId, DayOfWeek dayOfWeek, CourtSchedule.ScheduleType scheduleType);
    
    /**
     * 根据场地ID和特殊日期查找时间表
     */
    List<CourtSchedule> findByCourtIdAndSpecialDateAndIsActiveTrue(Long courtId, LocalDate specialDate);
    
    /**
     * 根据时间表类型查找
     */
    List<CourtSchedule> findByScheduleTypeAndIsActiveTrue(CourtSchedule.ScheduleType scheduleType);
    
    /**
     * 查找指定场地在指定日期的有效时间表
     */
    @Query("SELECT cs FROM CourtSchedule cs WHERE cs.courtId = :courtId " +
           "AND cs.isActive = true " +
           "AND ((cs.scheduleType = 'REGULAR' AND cs.dayOfWeek = :dayOfWeek) " +
           "OR (cs.scheduleType = 'SPECIAL' AND cs.specialDate = :date))")
    List<CourtSchedule> findEffectiveSchedules(@Param("courtId") Long courtId, 
                                             @Param("dayOfWeek") DayOfWeek dayOfWeek, 
                                             @Param("date") LocalDate date);
    
    /**
     * 检查场地在指定时间是否开放
     */
    @Query("SELECT COUNT(cs) > 0 FROM CourtSchedule cs WHERE cs.courtId = :courtId " +
           "AND cs.isActive = true " +
           "AND cs.openTime <= :time AND cs.closeTime >= :time " +
           "AND ((cs.scheduleType = 'REGULAR' AND cs.dayOfWeek = :dayOfWeek) " +
           "OR (cs.scheduleType = 'SPECIAL' AND cs.specialDate = :date))")
    boolean isCourtOpenAt(@Param("courtId") Long courtId, 
                         @Param("dayOfWeek") DayOfWeek dayOfWeek, 
                         @Param("date") LocalDate date, 
                         @Param("time") LocalTime time);
    
    /**
     * 批量删除指定场地的时间表
     */
    void deleteByCourtIdIn(List<Long> courtIds);
    
    /**
     * 查找有冲突的时间表
     */
    @Query("SELECT cs FROM CourtSchedule cs WHERE cs.courtId = :courtId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND cs.scheduleType = :scheduleType " +
           "AND cs.isActive = true " +
           "AND (:specialDate IS NULL OR cs.specialDate = :specialDate)")
    List<CourtSchedule> findConflictingSchedules(@Param("courtId") Long courtId,
                                                @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                @Param("scheduleType") CourtSchedule.ScheduleType scheduleType,
                                                @Param("specialDate") LocalDate specialDate);
    
    /**
     * 统计各种类型的时间表数量
     */
    @Query("SELECT cs.scheduleType, COUNT(cs) FROM CourtSchedule cs " +
           "WHERE cs.isActive = true GROUP BY cs.scheduleType")
    List<Object[]> getScheduleTypeStatistics();
    
    /**
     * 获取场地的总开放小时数
     */
    @Query("SELECT cs.courtId, SUM(HOUR(cs.closeTime) - HOUR(cs.openTime)) FROM CourtSchedule cs " +
           "WHERE cs.isActive = true AND cs.scheduleType = 'REGULAR' " +
           "GROUP BY cs.courtId")
    List<Object[]> getWeeklyOpenHoursByCourtId();
    
    /**
     * 查找即将过期的特殊时间表（过期前7天）
     */
    @Query("SELECT cs FROM CourtSchedule cs WHERE cs.scheduleType = 'SPECIAL' " +
           "AND cs.specialDate IS NOT NULL " +
           "AND cs.specialDate BETWEEN CURRENT_DATE AND :endDate " +
           "AND cs.isActive = true")
    List<CourtSchedule> findExpiringSpecialSchedules(@Param("endDate") LocalDate endDate);
    
    /**
     * 根据时间段查找时间表
     */
    List<CourtSchedule> findBySlotDurationAndIsActiveTrue(Integer slotDuration);
    
    /**
     * 查找开放时间最长的场地
     */
    @Query("SELECT cs FROM CourtSchedule cs WHERE cs.isActive = true " +
           "ORDER BY (HOUR(cs.closeTime) - HOUR(cs.openTime)) DESC")
    List<CourtSchedule> findSchedulesOrderByOpenHoursDesc();
    
    /**
     * 检查是否存在重复的时间表
     */
    boolean existsByCourtIdAndDayOfWeekAndScheduleTypeAndIsActiveTrue(
            Long courtId, DayOfWeek dayOfWeek, CourtSchedule.ScheduleType scheduleType);
    
    /**
     * 查找指定时间范围内的可用时间段
     */
    @Query("SELECT cs FROM CourtSchedule cs WHERE cs.courtId = :courtId " +
           "AND cs.isActive = true " +
           "AND cs.openTime <= :endTime AND cs.closeTime >= :startTime")
    List<CourtSchedule> findAvailableSchedulesInTimeRange(@Param("courtId") Long courtId,
                                                         @Param("startTime") LocalTime startTime,
                                                         @Param("endTime") LocalTime endTime);
} 