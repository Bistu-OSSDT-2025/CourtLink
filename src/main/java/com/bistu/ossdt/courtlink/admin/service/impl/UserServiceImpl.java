package com.bistu.ossdt.courtlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.dto.UserDTO;
import com.bistu.ossdt.courtlink.admin.mapper.UserMapper;
import com.bistu.ossdt.courtlink.admin.model.User;
import com.bistu.ossdt.courtlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // 检查用户名和邮箱是否已存在
        if (isUsernameExists(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (isEmailExists(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        save(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, CreateUserRequest request) {
        User user = getById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 检查用户名和邮箱是否与其他用户重复
        if (!user.getUsername().equals(request.getUsername()) && isUsernameExists(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (!user.getEmail().equals(request.getEmail()) && isEmailExists(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        BeanUtils.copyProperties(request, user);
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setUpdateTime(LocalDateTime.now());

        updateById(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        removeById(id);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = getById(id);
        return user != null ? convertToDTO(user) : null;
    }

    @Override
    public IPage<UserDTO> getUsers(int page, int size, String search) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (search != null && !search.isEmpty()) {
            wrapper.like(User::getUsername, search)
                  .or()
                  .like(User::getRealName, search)
                  .or()
                  .like(User::getEmail, search);
        }

        return page(userPage, wrapper).convert(this::convertToDTO);
    }

    @Override
    @Transactional
    public boolean toggleUserStatus(Long id) {
        User user = getById(id);
        if (user == null) {
            return false;
        }
        user.setEnabled(!user.getEnabled());
        return updateById(user);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return baseMapper.countByUsername(username) > 0;
    }

    @Override
    public boolean isEmailExists(String email) {
        return baseMapper.countByEmail(email) > 0;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
} 