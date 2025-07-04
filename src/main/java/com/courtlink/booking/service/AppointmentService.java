package com.courtlink.booking.service;

import com.courtlink.booking.dto.AppointmentRequest;
import com.courtlink.booking.dto.AppointmentResponse;
import com.courtlink.booking.entity.Appointment;
import com.courtlink.user.entity.User;

import java.util.List;

public interface AppointmentService {

    /**
     * 创建预约
     * @param request 预约请求
     * @param user 用户
     * @return 预约响应
     */
    AppointmentResponse createAppointment(AppointmentRequest request, User user);

    /**
     * 获取用户的所有预约
     * @param user 用户
     * @return 预约列表
     */
    List<AppointmentResponse> getUserAppointments(User user);

    /**
     * 根据ID获取预约详情
     * @param id 预约ID
     * @param user 用户
     * @return 预约响应
     */
    AppointmentResponse getAppointmentById(Long id, User user);

    /**
     * 取消预约
     * @param id 预约ID
     * @param user 用户
     */
    void cancelAppointment(Long id, User user);

    /**
     * 验证预约请求是否符合限制规则
     * @param request 预约请求
     * @param user 用户
     * @throws IllegalArgumentException 如果违反限制规则
     */
    void validateAppointmentRequest(AppointmentRequest request, User user);

    /**
     * 将实体转换为响应DTO
     * @param appointment 预约实体
     * @return 预约响应
     */
    AppointmentResponse convertToResponse(Appointment appointment);
} 