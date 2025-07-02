package com.courtlink.user.service.impl;

import com.courtlink.user.dto.LoginRequest;
import com.courtlink.user.dto.LoginResponse;
import com.courtlink.user.dto.UserDTO;
import com.courtlink.user.entity.User;
import com.courtlink.user.exception.UserNotFoundException;
import com.courtlink.user.repository.UserRepository;
import com.courtlink.user.service.UserService;
import com.courtlink.user.util.SimplePasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.info("注册新用户: {}", userDTO.getUsername());
        
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user, "password");
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setActive(true);
        user = userRepository.save(user);

        UserDTO result = convertToDTO(user);
        log.info("用户注册成功: {}", result.getUsername());
        return result;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录尝试: {}", loginRequest.getUsername());
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!user.getActive()) {
            throw new RuntimeException("账户已被禁用");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 这里应该生成JWT令牌，目前简化处理
        String token = "simple_token_" + user.getId() + "_" + System.currentTimeMillis();
        
        UserDTO userDTO = convertToDTO(user);
        LoginResponse response = new LoginResponse(token, "Bearer", userDTO);
        
        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("用户名", username));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("邮箱", email));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return convertToDTO(user);
    }

    @Override
    public UserDTO update(Long id, UserDTO userDTO) {
        log.info("更新用户信息: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmailAndIdNot(userDTO.getEmail(), id)) {
            throw new RuntimeException("邮箱已存在");
        }

        BeanUtils.copyProperties(userDTO, user, "id", "password");
        
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        user = userRepository.save(user);
        UserDTO result = convertToDTO(user);
        
        log.info("用户信息更新成功: {}", result.getUsername());
        return result;
    }

    @Override
    public void delete(Long id) {
        log.info("删除用户: {}", id);
        
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("修改用户密码: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户密码修改成功: {}", id);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        log.info("重置用户密码: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("用户密码重置成功: {}", id);
    }

    @Override
    public void toggleUserStatus(Long id) {
        log.info("切换用户状态: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setActive(!user.getActive());
        userRepository.save(user);
        
        log.info("用户状态切换成功: {} -> {}", id, user.getActive() ? "激活" : "禁用");
    }

    @Override
    public void activateUser(Long id) {
        log.info("激活用户: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setActive(true);
        userRepository.save(user);
        
        log.info("用户激活成功: {}", id);
    }

    @Override
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("No authenticated user found");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found: " + username));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO, "password");
        return userDTO;
    }
} 