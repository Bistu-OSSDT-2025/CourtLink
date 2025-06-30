package com.bistu.ossdt.courtlink.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "�û�������Ϊ��")
    private String username;

    @NotBlank(message = "���벻��Ϊ��")
    private String password;

    @NotBlank(message = "���䲻��Ϊ��")
    @Email(message = "�����ʽ����ȷ")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "�ֻ��Ÿ�ʽ����ȷ")
    private String phone;

    @NotBlank(message = "��ʵ��������Ϊ��")
    private String realName;

    @NotBlank(message = "��ɫ����Ϊ��")
    private String role;
} 