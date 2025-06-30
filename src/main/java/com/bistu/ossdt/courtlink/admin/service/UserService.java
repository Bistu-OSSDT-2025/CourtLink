package com.bistu.ossdt.courtlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.dto.UserDTO;
import com.bistu.ossdt.courtlink.admin.model.User;

public interface UserService extends IService<User> {
    UserDTO createUser(CreateUserRequest request);
    UserDTO updateUser(Long id, CreateUserRequest request);
    void deleteUser(Long id);
    UserDTO getUserById(Long id);
    IPage<UserDTO> getUsers(int page, int size, String search);
    boolean toggleUserStatus(Long id);
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
} 