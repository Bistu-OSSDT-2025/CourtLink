<<<<<<< HEAD
package com.courtlink.user.service;

import com.courtlink.user.entity.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
=======
package com.courtlink.user.service;

import com.courtlink.user.dto.LoginRequest;
import com.courtlink.user.dto.LoginResponse;
import com.courtlink.user.dto.UserDTO;
import com.courtlink.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    // 基础CRUD操作
    UserDTO register(UserDTO userDTO);
    UserDTO findByUsername(String username);
    UserDTO findByEmail(String email);
    UserDTO findById(Long id);
    UserDTO update(Long id, UserDTO userDTO);
    void delete(Long id);
    
    // 查询操作
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<UserDTO> findAll();
    Page<UserDTO> findAll(Pageable pageable);
    
    // 认证相关
    LoginResponse login(LoginRequest loginRequest);
    boolean validatePassword(Long id, String password);
    
    // 密码管理
    void changePassword(Long id, String oldPassword, String newPassword);
    void resetPassword(Long id, String newPassword);
    
    // 用户状态管理
    void toggleUserStatus(Long id);
    void activateUser(Long id);
    void deactivateUser(Long id);

    // 获取当前用户
    User getCurrentUser();
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 