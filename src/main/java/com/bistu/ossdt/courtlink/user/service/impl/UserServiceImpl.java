package com.bistu.ossdt.courtlink.user.service.impl;

import com.bistu.ossdt.courtlink.user.dto.LoginRequest;
import com.bistu.ossdt.courtlink.user.dto.LoginResponse;
import com.bistu.ossdt.courtlink.user.dto.UserDTO;
import com.bistu.ossdt.courtlink.user.entity.User;
import com.bistu.ossdt.courtlink.user.exception.InvalidPasswordException;
import com.bistu.ossdt.courtlink.user.exception.UserAlreadyExistsException;
import com.bistu.ossdt.courtlink.user.exception.UserNotFoundException;
import com.bistu.ossdt.courtlink.user.repository.UserRepository;
import com.bistu.ossdt.courtlink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bistu.ossdt.courtlink.user.util.SimplePasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SimplePasswordEncoder passwordEncoder;

    @Override
    public UserDTO register(UserDTO userDTO) {
        log.info("开始注册用户: {}", userDTO.getUsername());
        
        // 参数验证
        if (!StringUtils.hasText(userDTO.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(userDTO.getEmail())) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (!StringUtils.hasText(userDTO.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 使用重试机制进行用户注册
        return executeWithRetry(() -> {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                log.warn("用户名已存在: {}", userDTO.getUsername());
                throw new UserAlreadyExistsException("用户名", userDTO.getUsername());
            }
            
            // 检查邮箱是否已存在
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                log.warn("邮箱已存在: {}", userDTO.getEmail());
                throw new UserAlreadyExistsException("邮箱", userDTO.getEmail());
            }

            User user = new User();
            BeanUtils.copyProperties(userDTO, user, "id", "createdAt", "updatedAt");
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setStatus(User.UserStatus.ACTIVE);
            user.setRole(User.UserRole.USER);
            
            user = userRepository.save(user);
            log.info("用户注册成功: {}", user.getUsername());
            
            return convertToDTO(user);
        }, "用户注册", 3);
    }
    
    /**
     * 执行带重试机制的操作
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation, String operationName, int maxRetries) {
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount <= maxRetries) {
            try {
                return operation.get();
            } catch (UserAlreadyExistsException | IllegalArgumentException e) {
                // 业务异常，不进行重试
                throw e;
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                
                if (retryCount <= maxRetries) {
                    long waitTime = 100L * retryCount; // 递增等待时间: 100ms, 200ms, 300ms
                    log.warn("{}失败，第{}次重试，等待{}ms后重试: {}", operationName, retryCount, waitTime, e.getMessage());
                    
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("操作被中断", ie);
                    }
                } else {
                    log.error("{}失败，已达到最大重试次数{}", operationName, maxRetries, e);
                    break;
                }
            }
        }
        
        throw new RuntimeException(operationName + "失败，已重试" + maxRetries + "次", lastException);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录尝试: {}", loginRequest.getUsername());
        
        // 参数验证
        if (!StringUtils.hasText(loginRequest.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(loginRequest.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 查找用户
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidPasswordException("用户名或密码错误"));
        
        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("用户登录失败，密码错误: {}", loginRequest.getUsername());
            throw new InvalidPasswordException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            log.warn("用户账户已被禁用: {}", loginRequest.getUsername());
            throw new RuntimeException("账户已被禁用");
        }
        
        // 生成简单的token（实际项目中应使用JWT）
        String token = "simple-token-" + user.getId() + "-" + System.currentTimeMillis();
        
        log.info("用户登录成功: {}", loginRequest.getUsername());
        return new LoginResponse(token, convertToDTO(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("用户名", username));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("邮箱", email));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return convertToDTO(user);
    }

    @Override
    public UserDTO update(Long id, UserDTO userDTO) {
        log.info("更新用户信息: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        if (userDTO == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // 检查用户名是否被其他用户使用
        if (StringUtils.hasText(userDTO.getUsername()) && 
            !user.getUsername().equals(userDTO.getUsername()) && 
            userRepository.existsByUsernameAndIdNot(userDTO.getUsername(), id)) {
            throw new UserAlreadyExistsException("用户名", userDTO.getUsername());
        }
        
        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(userDTO.getEmail()) && 
            !user.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmailAndIdNot(userDTO.getEmail(), id)) {
            throw new UserAlreadyExistsException("邮箱", userDTO.getEmail());
        }

        // 更新用户信息（排除密码、ID、时间戳、角色、状态）
        BeanUtils.copyProperties(userDTO, user, "id", "password", "createdAt", "updatedAt", "role", "status");
        
        // 如果提供了新密码，则更新密码
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        // 如果提供了角色，则更新角色
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        
        // 如果提供了状态，则更新状态
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        user = userRepository.save(user);
        log.info("用户信息更新成功: {}", user.getUsername());
        
        return convertToDTO(user);
    }

    @Override
    public void delete(Long id) {
        log.info("删除用户: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        
        userRepository.deleteById(id);
        log.info("用户删除成功: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(Long id, String password) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("更改用户密码: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        if (!StringUtils.hasText(oldPassword)) {
            throw new IllegalArgumentException("原密码不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("原密码不正确");
        }
        
        // 设置新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户密码更改成功: {}", id);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        log.info("重置用户密码: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户密码重置成功: {}", id);
    }

    @Override
    public void toggleUserStatus(Long id) {
        log.info("切换用户状态: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        // 切换用户状态
        if (user.getStatus() == User.UserStatus.ACTIVE) {
            user.setStatus(User.UserStatus.DISABLED);
        } else {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        
        userRepository.save(user);
        log.info("用户状态切换成功: {}, 新状态: {}", id, user.getStatus());
    }

    @Override
    public void activateUser(Long id) {
        log.info("激活用户: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);
        
        log.info("用户激活成功: {}", id);
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("停用用户: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        user.setStatus(User.UserStatus.DISABLED);
        userRepository.save(user);
        
        log.info("用户停用成功: {}", id);
    }

    /**
     * 将User实体转换为UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        // 不返回密码
        userDTO.setPassword(null);
        return userDTO;
    }
} 