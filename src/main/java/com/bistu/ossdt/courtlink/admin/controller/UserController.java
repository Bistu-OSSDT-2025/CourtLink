package com.bistu.ossdt.courtlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bistu.ossdt.courtlink.admin.dto.CreateUserRequest;
import com.bistu.ossdt.courtlink.admin.dto.UserDTO;
import com.bistu.ossdt.courtlink.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "�û�����", description = "�û�������ؽӿ�")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "�����û�")
    public UserDTO createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "�����û�")
    public UserDTO updateUser(
            @Parameter(description = "�û�ID") @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ɾ���û�")
    public void deleteUser(@Parameter(description = "�û�ID") @PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "��ȡ�û�����")
    public UserDTO getUser(@Parameter(description = "�û�ID") @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    @Operation(summary = "��ȡ�û��б�")
    public IPage<UserDTO> getUsers(
            @Parameter(description = "ҳ��") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "ÿҳ��С") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "�����ؼ���") @RequestParam(required = false) String search) {
        return userService.getUsers(page, size, search);
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "�л��û�״̬")
    public boolean toggleUserStatus(@Parameter(description = "�û�ID") @PathVariable Long id) {
        return userService.toggleUserStatus(id);
    }

    @GetMapping("/check-username")
    @Operation(summary = "����û����Ƿ����")
    public boolean checkUsername(@Parameter(description = "�û���") @RequestParam String username) {
        return userService.isUsernameExists(username);
    }

    @GetMapping("/check-email")
    @Operation(summary = "��������Ƿ����")
    public boolean checkEmail(@Parameter(description = "����") @RequestParam String email) {
        return userService.isEmailExists(email);
    }
} 